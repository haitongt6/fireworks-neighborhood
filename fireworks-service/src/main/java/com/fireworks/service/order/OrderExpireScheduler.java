package com.fireworks.service.order;

import com.fireworks.model.constant.RedisKeyConstant;
import com.fireworks.service.OmsOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时关闭定时任务。
 */
@Component
public class OrderExpireScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderExpireScheduler.class);

    /**
     * 单次任务最多处理的过期订单数，避免一次调度阻塞过久。
     */
    private static final int MAX_BATCH_SIZE = 200;

    private final StringRedisTemplate stringRedisTemplate;
    private final OmsOrderService omsOrderService;

    public OrderExpireScheduler(StringRedisTemplate stringRedisTemplate,
                                OmsOrderService omsOrderService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.omsOrderService = omsOrderService;
    }

//    @Scheduled(fixedDelay = 10000L)
//    public void closeExpiredOrders() {
//        long now = System.currentTimeMillis();
//
//        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
//            ZSetOperations.TypedTuple<String> tuple = stringRedisTemplate.opsForZSet()
//                    .popMin(RedisKeyConstant.ORDER_EXPIRE_ZSET);
//
//            if (tuple == null || tuple.getValue() == null || tuple.getScore() == null) {
//                return;
//            }
//
//            String orderNo = tuple.getValue();
//            double score = tuple.getScore();
//
//            // 还没到期：放回去并结束本轮
//            if (score > now) {
//                stringRedisTemplate.opsForZSet().add(RedisKeyConstant.ORDER_EXPIRE_ZSET, orderNo, score);
//                return;
//            }
//
//            try {
//                omsOrderService.closeExpiredOrder(orderNo);
//            } catch (Exception e) {
//                log.error("关闭过期订单失败, orderNo={}", orderNo, e);
//            }
//        }
//    }
}
