package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fireworks.model.constant.OrderOperateTypeEnum;
import com.fireworks.model.constant.OrderStatusEnum;
import com.fireworks.model.constant.PayStatusEnum;
import com.fireworks.model.constant.StockLockStatusEnum;
import com.fireworks.model.dto.MockPayParam;
import com.fireworks.model.pojo.OmsOrder;
import com.fireworks.model.pojo.OmsOrderOperateLog;
import com.fireworks.model.pojo.OmsOrderPay;
import com.fireworks.model.pojo.OmsOrderStockLock;
import com.fireworks.service.OmsPayService;
import com.fireworks.service.exception.OrderException;
import com.fireworks.service.exception.PayException;
import com.fireworks.service.mapper.OmsOrderMapper;
import com.fireworks.service.mapper.OmsOrderOperateLogMapper;
import com.fireworks.service.mapper.OmsOrderPayMapper;
import com.fireworks.service.mapper.OmsOrderStockLockMapper;
import com.fireworks.service.mapper.PmsProductMapper;
import com.fireworks.service.order.OrderRedisHelper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 支付业务实现。
 */
@Service
public class OmsPayServiceImpl implements OmsPayService {

    private static final Logger log = LoggerFactory.getLogger(OmsPayServiceImpl.class);

    private final OmsOrderMapper orderMapper;
    private final OmsOrderPayMapper orderPayMapper;
    private final OmsOrderStockLockMapper stockLockMapper;
    private final OmsOrderOperateLogMapper operateLogMapper;
    private final PmsProductMapper productMapper;
    private final OrderRedisHelper orderRedisHelper;
    private final RedissonClient redissonClient;

    public OmsPayServiceImpl(OmsOrderMapper orderMapper,
                             OmsOrderPayMapper orderPayMapper,
                             OmsOrderStockLockMapper stockLockMapper,
                             OmsOrderOperateLogMapper operateLogMapper,
                             PmsProductMapper productMapper,
                             OrderRedisHelper orderRedisHelper,
                             RedissonClient redissonClient) {
        this.orderMapper = orderMapper;
        this.orderPayMapper = orderPayMapper;
        this.stockLockMapper = stockLockMapper;
        this.operateLogMapper = operateLogMapper;
        this.productMapper = productMapper;
        this.orderRedisHelper = orderRedisHelper;
        this.redissonClient = redissonClient;
    }

    @Override
    public void mockPay(Long userId, MockPayParam param) {
        OmsOrder order = getOrderByNo(param.getOrderNo());
        if (!order.getUserId().equals(userId)) {
            throw new OrderException("无权操作该订单");
        }
        doPay(order, param.getRequestNo(), userId);
    }

    @Override
    public void adminMockPaySuccess(MockPayParam param) {
        OmsOrder order = getOrderByNo(param.getOrderNo());
        doPay(order, param.getRequestNo(), null);
    }

    private void doPay(OmsOrder order, String requestNo, Long operatorId) {
        // 幂等：requestNo 已处理过直接返回
        OmsOrderPay existPay = orderPayMapper.selectOne(
                new LambdaQueryWrapper<OmsOrderPay>()
                        .eq(OmsOrderPay::getRequestNo, requestNo));
        if (existPay != null && PayStatusEnum.PAY_SUCCESS.getCode().equals(existPay.getPayStatus())) {
            log.info("支付幂等命中，requestNo={}", requestNo);
            return;
        }
        // 订单已支付，幂等返回
        if (OrderStatusEnum.PAID.getCode().equals(order.getOrderStatus())) {
            return;
        }
        if (!OrderStatusEnum.WAIT_PAY.getCode().equals(order.getOrderStatus())) {
            throw new PayException("订单状态不允许支付，当前状态：" + order.getOrderStatus());
        }
        String payLockKey = orderRedisHelper.buildPayLockKey(order.getOrderNo());
        RLock payLock = redissonClient.getLock(payLockKey);
        boolean locked = false;
        try {
            locked = payLock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!locked) throw new PayException("支付处理中，请勿重复提交");
            doPayInTx(order, requestNo, operatorId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PayException("支付被中断，请重试");
        } finally {
            if (locked && payLock.isHeldByCurrentThread()) payLock.unlock();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void doPayInTx(OmsOrder order, String requestNo, Long operatorId) {
        Date now = new Date();
        OmsOrderPay pay = orderPayMapper.selectOne(
                new LambdaQueryWrapper<OmsOrderPay>()
                        .eq(OmsOrderPay::getOrderNo, order.getOrderNo()));
        if (pay == null) throw new PayException("支付单不存在");
        if (PayStatusEnum.PAY_SUCCESS.getCode().equals(pay.getPayStatus())) {
            return;
        }
        orderPayMapper.update(null, new LambdaUpdateWrapper<OmsOrderPay>()
                .eq(OmsOrderPay::getId, pay.getId())
                .set(OmsOrderPay::getPayStatus, PayStatusEnum.PAY_SUCCESS.getCode())
                .set(OmsOrderPay::getRequestNo, requestNo)
                .set(OmsOrderPay::getThirdPartyTradeNo, "MOCK" + System.currentTimeMillis())
                .set(OmsOrderPay::getPayTime, now)
                .set(OmsOrderPay::getNotifyStatus, 1));
        orderMapper.update(null, new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, order.getId())
                .set(OmsOrder::getOrderStatus, OrderStatusEnum.PAID.getCode())
                .set(OmsOrder::getPayStatus, PayStatusEnum.PAY_SUCCESS.getCode())
                .set(OmsOrder::getPayTime, now));
        List<OmsOrderStockLock> locks = stockLockMapper.selectList(
                new LambdaQueryWrapper<OmsOrderStockLock>()
                        .eq(OmsOrderStockLock::getOrderNo, order.getOrderNo())
                        .eq(OmsOrderStockLock::getLockStatus, StockLockStatusEnum.LOCKED.getCode()));
        for (OmsOrderStockLock sl : locks) {
            productMapper.confirmStock(sl.getProductId(), sl.getLockQuantity());
            stockLockMapper.update(null, new LambdaUpdateWrapper<OmsOrderStockLock>()
                    .eq(OmsOrderStockLock::getId, sl.getId())
                    .set(OmsOrderStockLock::getLockStatus, StockLockStatusEnum.DEDUCTED.getCode()));
        }
        OmsOrderOperateLog opLog = new OmsOrderOperateLog();
        opLog.setOrderId(order.getId());
        opLog.setOrderNo(order.getOrderNo());
        opLog.setOperateType(OrderOperateTypeEnum.PAY_SUCCESS.getCode());
        opLog.setPreStatus(OrderStatusEnum.WAIT_PAY.getDesc());
        opLog.setPostStatus(OrderStatusEnum.PAID.getDesc());
        opLog.setNote("支付成功");
        opLog.setOperatorId(operatorId);
        opLog.setOperatorType(operatorId == null ? 2 : 1);
        operateLogMapper.insert(opLog);
        orderRedisHelper.removeExpireOrder(order.getOrderNo());
    }

    private OmsOrder getOrderByNo(String orderNo) {
        OmsOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getOrderNo, orderNo)
                        .eq(OmsOrder::getDeleted, 0));
        if (order == null) throw new OrderException("订单不存在：" + orderNo);
        return order;
    }
}
