package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireworks.model.constant.OrderOperateTypeEnum;
import com.fireworks.model.constant.OrderSourceTypeEnum;
import com.fireworks.model.constant.OrderStatusEnum;
import com.fireworks.model.constant.PayStatusEnum;
import com.fireworks.model.constant.PayTypeEnum;
import com.fireworks.model.constant.StockLockStatusEnum;
import com.fireworks.model.dto.OmsOrderQueryParam;
import com.fireworks.model.dto.OrderCancelParam;
import com.fireworks.model.dto.OrderItemSubmitParam;
import com.fireworks.model.dto.OrderSubmitParam;
import com.fireworks.model.pojo.OmsOrder;
import com.fireworks.model.pojo.OmsOrderItem;
import com.fireworks.model.pojo.OmsOrderOperateLog;
import com.fireworks.model.pojo.OmsOrderPay;
import com.fireworks.model.pojo.OmsOrderStockLock;
import com.fireworks.model.pojo.PmsProduct;
import com.fireworks.model.vo.OmsOrderDetailVO;
import com.fireworks.model.vo.OmsOrderItemVO;
import com.fireworks.model.vo.OmsOrderListItemVO;
import com.fireworks.model.vo.OrderConfirmVO;
import com.fireworks.model.vo.OrderSubmitVO;
import com.fireworks.model.vo.PageResult;
import com.fireworks.service.OmsOrderService;
import com.fireworks.service.cart.CartItemEntry;
import com.fireworks.service.cart.CartMqProducer;
import com.fireworks.service.cart.CartRedisHelper;
import com.fireworks.service.exception.OrderException;
import com.fireworks.service.exception.RepeatSubmitException;
import com.fireworks.service.exception.StockException;
import com.fireworks.service.mapper.OmsOrderItemMapper;
import com.fireworks.service.mapper.OmsOrderMapper;
import com.fireworks.service.mapper.OmsOrderOperateLogMapper;
import com.fireworks.service.mapper.OmsOrderPayMapper;
import com.fireworks.service.mapper.OmsOrderStockLockMapper;
import com.fireworks.service.mapper.PmsProductMapper;
import com.fireworks.service.order.OrderNoGenerator;
import com.fireworks.service.order.OrderRedisHelper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OmsOrderServiceImpl implements OmsOrderService {

    private static final Logger log = LoggerFactory.getLogger(OmsOrderServiceImpl.class);
    private static final long ORDER_EXPIRE_MILLIS = 15 * 60 * 1000L;

    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final OmsOrderPayMapper orderPayMapper;
    private final OmsOrderStockLockMapper stockLockMapper;
    private final OmsOrderOperateLogMapper operateLogMapper;
    private final PmsProductMapper productMapper;
    private final OrderRedisHelper orderRedisHelper;
    private final OrderNoGenerator orderNoGenerator;
    private final CartRedisHelper cartRedisHelper;
    private final CartMqProducer cartMqProducer;
    private final RedissonClient redissonClient;

    public OmsOrderServiceImpl(OmsOrderMapper orderMapper, OmsOrderItemMapper orderItemMapper, OmsOrderPayMapper orderPayMapper, OmsOrderStockLockMapper stockLockMapper, OmsOrderOperateLogMapper operateLogMapper, PmsProductMapper productMapper, OrderRedisHelper orderRedisHelper, OrderNoGenerator orderNoGenerator, CartRedisHelper cartRedisHelper, CartMqProducer cartMqProducer, RedissonClient redissonClient) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderPayMapper = orderPayMapper;
        this.stockLockMapper = stockLockMapper;
        this.operateLogMapper = operateLogMapper;
        this.productMapper = productMapper;
        this.orderRedisHelper = orderRedisHelper;
        this.orderNoGenerator = orderNoGenerator;
        this.cartRedisHelper = cartRedisHelper;
        this.cartMqProducer = cartMqProducer;
        this.redissonClient = redissonClient;
    }

    @Override
    public OrderConfirmVO confirm(Long userId, List<Long> productIds) {
        Map<String, CartItemEntry> cart = cartRedisHelper.getCart(userId);
        List<OmsOrderItemVO> items = new ArrayList<OmsOrderItemVO>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int itemCount = 0;
        for (Map.Entry<String, CartItemEntry> e : cart.entrySet()) {
            Long pid = Long.valueOf(e.getKey());
            if (productIds != null && !productIds.isEmpty() && !productIds.contains(pid)) {
                continue;
            }
            CartItemEntry ce = e.getValue();
            PmsProduct p = productMapper.selectById(pid);
            if (p == null || !Integer.valueOf(1).equals(p.getStatus())) {
                continue;
            }
            int ls = p.getLockStock() == null ? 0 : p.getLockStock();
            int st = p.getStock() == null ? 0 : p.getStock();
            if ((st - ls) < ce.getQuantity()) {
                continue;
            }
            if (p.getLimitPerUser() != null && ce.getQuantity() > p.getLimitPerUser()) {
                continue;
            }
            OmsOrderItemVO iv = new OmsOrderItemVO();
            iv.setProductId(pid);
            iv.setProductTitle(p.getTitle());
            iv.setProductImage(ce.getImageSnapshot());
            iv.setProductPrice(p.getPrice());
            iv.setQuantity(ce.getQuantity());
            BigDecimal lt = p.getPrice().multiply(BigDecimal.valueOf(ce.getQuantity()));
            iv.setTotalAmount(lt);
            items.add(iv);
            totalAmount = totalAmount.add(lt);
            itemCount += ce.getQuantity();
        }
        String token = orderRedisHelper.createSubmitToken(userId);
        OrderConfirmVO vo = new OrderConfirmVO();
        vo.setSubmitToken(token);
        vo.setSourceType(OrderSourceTypeEnum.CART.getCode());
        vo.setItemCount(itemCount);
        vo.setTotalAmount(totalAmount);
        vo.setItems(items);
        return vo;
    }

    @Override
    public OrderSubmitVO submit(Long userId, OrderSubmitParam param) {
        RLock lock = redissonClient.getLock(orderRedisHelper.buildSubmitLockKey(userId));
        boolean locked = false;
        try {
            locked = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!locked) {
                throw new RepeatSubmitException("操作频繁，请稍后重试");
            }
            return doSubmit(userId, param);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new OrderException("提交订单被中断，请重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO doSubmit(Long userId, OrderSubmitParam param) {
        if (!orderRedisHelper.consumeSubmitToken(userId, param.getSubmitToken())) {
            throw new RepeatSubmitException("请勿重复提交订单");
        }
        List<OrderItemSubmitParam> submitItems = param.getItems();
        List<OmsOrderItem> orderItems = new ArrayList<OmsOrderItem>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int itemCount = 0;
        Date expireTime = new Date(System.currentTimeMillis() + ORDER_EXPIRE_MILLIS);
        String orderNo = orderNoGenerator.generateOrderNo();
        for (OrderItemSubmitParam item : submitItems) {
            PmsProduct p = productMapper.selectById(item.getProductId());
            if (p == null) {
                throw new OrderException("商品不存在：" + item.getProductId());
            }
            if (!Integer.valueOf(1).equals(p.getStatus())) {
                throw new OrderException("商品已下架：" + p.getTitle());
            }
            int ls = p.getLockStock() == null ? 0 : p.getLockStock();
            int st = p.getStock() == null ? 0 : p.getStock();
            if ((st - ls) < item.getQuantity()) {
                throw new StockException("商品库存不足：" + p.getTitle());
            }
            if (p.getLimitPerUser() != null && item.getQuantity() > p.getLimitPerUser()) {
                throw new OrderException("商品超出限购数量：" + p.getTitle());
            }
            if (productMapper.lockStock(item.getProductId(), item.getQuantity()) == 0) {
                throw new StockException("库存锁定失败：" + p.getTitle());
            }
            BigDecimal lt = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(lt);
            itemCount += item.getQuantity();
            OmsOrderItem oi = new OmsOrderItem();
            oi.setOrderNo(orderNo);
            oi.setUserId(userId);
            oi.setProductId(p.getId());
            oi.setProductTitle(p.getTitle());
            oi.setProductImage(extractFirstImage(p.getImages()));
            oi.setProductPrice(p.getPrice());
            oi.setQuantity(item.getQuantity());
            oi.setTotalAmount(lt);
            oi.setItemStatus(0);
            orderItems.add(oi);
        }
        OmsOrder order = new OmsOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setOrderStatus(OrderStatusEnum.WAIT_PAY.getCode());
        order.setPayStatus(PayStatusEnum.WAIT_PAY.getCode());
        order.setSourceType(param.getSourceType());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setItemCount(itemCount);
        order.setReceiverName(param.getReceiverName());
        order.setReceiverPhone(param.getReceiverPhone());
        order.setReceiverProvince(param.getReceiverProvince());
        order.setReceiverCity(param.getReceiverCity());
        order.setReceiverDistrict(param.getReceiverDistrict());
        order.setReceiverDetailAddress(param.getReceiverDetailAddress());
        order.setRemark(param.getRemark());
        order.setSubmitToken(param.getSubmitToken());
        order.setExpireTime(expireTime);
        order.setDeleted(0);
        orderMapper.insert(order);
        for (OmsOrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }
        String payOrderNo = orderNoGenerator.generatePayOrderNo();
        OmsOrderPay pay = new OmsOrderPay();
        pay.setPayOrderNo(payOrderNo);
        pay.setOrderId(order.getId());
        pay.setOrderNo(orderNo);
        pay.setUserId(userId);
        pay.setPayType(PayTypeEnum.MOCK.getCode());
        pay.setPayStatus(PayStatusEnum.WAIT_PAY.getCode());
        pay.setPayAmount(totalAmount);
        pay.setNotifyStatus(0);
        orderPayMapper.insert(pay);
        for (OmsOrderItem oi : orderItems) {
            OmsOrderStockLock sl = new OmsOrderStockLock();
            sl.setOrderId(order.getId());
            sl.setOrderNo(orderNo);
            sl.setUserId(userId);
            sl.setProductId(oi.getProductId());
            sl.setLockQuantity(oi.getQuantity());
            sl.setLockStatus(StockLockStatusEnum.LOCKED.getCode());
            sl.setExpireTime(expireTime);
            stockLockMapper.insert(sl);
        }
        insertLog(order.getId(), orderNo, OrderOperateTypeEnum.SUBMIT, null, OrderStatusEnum.WAIT_PAY.getDesc(), "提交订单", userId, 1);
        for (OrderItemSubmitParam item : submitItems) {
            cartRedisHelper.removeItem(userId, item.getProductId());
            cartMqProducer.sendDelete(userId, item.getProductId());
        }
        orderRedisHelper.addExpireOrder(orderNo, expireTime.getTime());
        OrderSubmitVO vo = new OrderSubmitVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(orderNo);
        vo.setPayOrderNo(payOrderNo);
        vo.setOrderStatus(order.getOrderStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setExpireTime(expireTime);
        vo.setPayAmount(totalAmount);
        return vo;
    }

    @Override
    public PageResult<OmsOrderListItemVO> list(Long userId, OmsOrderQueryParam param) {
        param.setUserId(userId);
        Page<OmsOrderListItemVO> page = new Page<OmsOrderListItemVO>(param.getPageNum(), param.getPageSize());
        orderMapper.selectOrderPage(page, param);
        return new PageResult<OmsOrderListItemVO>(page.getRecords(), page.getTotal());
    }

    @Override
    public PageResult<OmsOrderListItemVO> adminList(OmsOrderQueryParam param) {
        Page<OmsOrderListItemVO> page = new Page<OmsOrderListItemVO>(param.getPageNum(), param.getPageSize());
        orderMapper.selectOrderPage(page, param);
        return new PageResult<OmsOrderListItemVO>(page.getRecords(), page.getTotal());
    }

    @Override
    public OmsOrderDetailVO detail(Long userId, String orderNo) {
        OmsOrder order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new OrderException("\u65e0\u6743\u67e5\u770b\u8be5\u8ba2\u5355");
        }
        return buildDetailVO(order);
    }

    @Override
    public OmsOrderDetailVO adminDetail(String orderNo) {
        return buildDetailVO(getOrderByNo(orderNo));
    }

    @Override
    public void cancel(Long userId, OrderCancelParam param) {
        OmsOrder order = getOrderByNo(param.getOrderNo());
        if (!order.getUserId().equals(userId)) {
            throw new OrderException("\u65e0\u6743\u64cd\u4f5c\u8be5\u8ba2\u5355");
        }
        doClose(order, OrderOperateTypeEnum.USER_CANCEL, OrderStatusEnum.CANCELED, "\u7528\u6237\u53d6\u6d88\u8ba2\u5355", userId, 1);
    }

    @Override
    public void adminClose(String orderNo) {
        doClose(getOrderByNo(orderNo), OrderOperateTypeEnum.ADMIN_CLOSE, OrderStatusEnum.CLOSED, "\u540e\u53f0\u5173\u95ed\u8ba2\u5355", null, 3);
    }

    @Override
    public void closeExpiredOrder(String orderNo) {
        OmsOrder order = orderMapper.selectOne(new LambdaQueryWrapper<OmsOrder>().eq(OmsOrder::getOrderNo, orderNo).eq(OmsOrder::getDeleted, 0));
        if (order == null) {
            orderRedisHelper.removeExpireOrder(orderNo);
            return;
        }
        if (!OrderStatusEnum.WAIT_PAY.getCode().equals(order.getOrderStatus())) {
            orderRedisHelper.removeExpireOrder(orderNo);
            return;
        }
        doClose(order, OrderOperateTypeEnum.TIMEOUT_CLOSE, OrderStatusEnum.CLOSED, "\u8ba2\u5355\u8d85\u65f6\u81ea\u52a8\u5173\u95ed", null, 2);
    }

    private void doClose(OmsOrder order, OrderOperateTypeEnum operateType, OrderStatusEnum targetStatus, String note, Long operatorId, Integer operatorType) {
        if (!OrderStatusEnum.WAIT_PAY.getCode().equals(order.getOrderStatus())) {
            throw new OrderException("\u8ba2\u5355\u72b6\u6001\u4e0d\u5141\u8bb8\u5173\u95ed");
        }
        RLock closeLock = redissonClient.getLock(orderRedisHelper.buildCloseLockKey(order.getOrderNo()));
        boolean locked = false;
        try {
            locked = closeLock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!locked) {
                throw new OrderException("\u8ba2\u5355\u6b63\u5728\u5904\u7406\u4e2d\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5");
            }
            OmsOrder fresh = orderMapper.selectById(order.getId());
            if (fresh == null || !OrderStatusEnum.WAIT_PAY.getCode().equals(fresh.getOrderStatus())) {
                return;
            }
            orderMapper.update(null, new LambdaUpdateWrapper<OmsOrder>().eq(OmsOrder::getId, order.getId()).set(OmsOrder::getOrderStatus, targetStatus.getCode()).set(OmsOrder::getPayStatus, PayStatusEnum.CLOSED.getCode()).set(OmsOrder::getCancelTime, new Date()));
            orderPayMapper.update(null, new LambdaUpdateWrapper<OmsOrderPay>().eq(OmsOrderPay::getOrderNo, order.getOrderNo()).set(OmsOrderPay::getPayStatus, PayStatusEnum.CLOSED.getCode()));
            List<OmsOrderStockLock> locks = stockLockMapper.selectList(new LambdaQueryWrapper<OmsOrderStockLock>().eq(OmsOrderStockLock::getOrderNo, order.getOrderNo()).eq(OmsOrderStockLock::getLockStatus, StockLockStatusEnum.LOCKED.getCode()));
            for (OmsOrderStockLock sl : locks) {
                productMapper.releaseStock(sl.getProductId(), sl.getLockQuantity());
                stockLockMapper.update(null, new LambdaUpdateWrapper<OmsOrderStockLock>().eq(OmsOrderStockLock::getId, sl.getId()).set(OmsOrderStockLock::getLockStatus, StockLockStatusEnum.RELEASED.getCode()));
            }
            insertLog(order.getId(), order.getOrderNo(), operateType, OrderStatusEnum.WAIT_PAY.getDesc(), targetStatus.getDesc(), note, operatorId, operatorType);
            orderRedisHelper.removeExpireOrder(order.getOrderNo());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new OrderException("\u5173\u95ed\u8ba2\u5355\u88ab\u4e2d\u65ad\uff0c\u8bf7\u91cd\u8bd5");
        } finally {
            if (locked && closeLock.isHeldByCurrentThread()) {
                closeLock.unlock();
            }
        }
    }

    private OmsOrder getOrderByNo(String orderNo) {
        OmsOrder order = orderMapper.selectOne(new LambdaQueryWrapper<OmsOrder>().eq(OmsOrder::getOrderNo, orderNo).eq(OmsOrder::getDeleted, 0));
        if (order == null) {
            throw new OrderException("\u8ba2\u5355\u4e0d\u5b58\u5728\uff1a" + orderNo);
        }
        return order;
    }

    private OmsOrderDetailVO buildDetailVO(OmsOrder o) {
        OmsOrderDetailVO vo = new OmsOrderDetailVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setUserId(o.getUserId());
        vo.setOrderStatus(o.getOrderStatus());
        vo.setPayStatus(o.getPayStatus());
        vo.setSourceType(o.getSourceType());
        vo.setTotalAmount(o.getTotalAmount());
        vo.setPayAmount(o.getPayAmount());
        vo.setItemCount(o.getItemCount());
        vo.setReceiverName(o.getReceiverName());
        vo.setReceiverPhone(o.getReceiverPhone());
        vo.setReceiverProvince(o.getReceiverProvince());
        vo.setReceiverCity(o.getReceiverCity());
        vo.setReceiverDistrict(o.getReceiverDistrict());
        vo.setReceiverDetailAddress(o.getReceiverDetailAddress());
        vo.setRemark(o.getRemark());
        vo.setExpireTime(o.getExpireTime());
        vo.setPayTime(o.getPayTime());
        vo.setCancelTime(o.getCancelTime());
        vo.setCancelReason(o.getCancelReason());
        vo.setCreateTime(o.getCreateTime());
        List<OmsOrderItem> dbItems = orderItemMapper.selectList(new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, o.getId()));
        List<OmsOrderItemVO> ivs = new ArrayList<OmsOrderItemVO>();
        for (OmsOrderItem i : dbItems) {
            OmsOrderItemVO iv = new OmsOrderItemVO();
            iv.setProductId(i.getProductId());
            iv.setProductTitle(i.getProductTitle());
            iv.setProductImage(i.getProductImage());
            iv.setProductPrice(i.getProductPrice());
            iv.setQuantity(i.getQuantity());
            iv.setTotalAmount(i.getTotalAmount());
            ivs.add(iv);
        }
        vo.setItems(ivs);
        return vo;
    }

    private void insertLog(Long orderId, String orderNo, OrderOperateTypeEnum type, String pre, String post, String note, Long operatorId, Integer operatorType) {
        OmsOrderOperateLog lg = new OmsOrderOperateLog();
        lg.setOrderId(orderId);
        lg.setOrderNo(orderNo);
        lg.setOperateType(type.getCode());
        lg.setPreStatus(pre);
        lg.setPostStatus(post);
        lg.setNote(note);
        lg.setOperatorId(operatorId);
        lg.setOperatorType(operatorType);
        operateLogMapper.insert(lg);
    }

    private String extractFirstImage(String images) {
        if (images == null || images.trim().isEmpty()) {
            return null;
        }
        String t = images.trim();
        if (t.startsWith("[")) {
            t = t.replaceAll("[\\\\[\\\\]\"]", "");
        }
        String[] parts = t.split(",");
        return parts[0].trim();
    }
}
