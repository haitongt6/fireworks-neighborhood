package com.fireworks.service.order;

import com.fireworks.model.constant.RedisKeyConstant;
import com.fireworks.service.OmsOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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

    /**
     * 原子弹出 score 最小的一条 member（ZRANGE 0 0 + ZREM），兼容 Redis 3.x/4.x。
     * {@link org.springframework.data.redis.core.DefaultZSetOperations#popMin} 依赖 ZPOPMIN（Redis 5.0+），老版本会报 ERR unknown command 'ZPOPMIN'。
     */
    @SuppressWarnings("rawtypes")
    private static final DefaultRedisScript<List> POP_MIN_SCRIPT = new DefaultRedisScript<>();

    static {
        POP_MIN_SCRIPT.setScriptText(
                "local z = redis.call('ZRANGE', KEYS[1], 0, 0, 'WITHSCORES')\n"
                        + "if #z == 0 then return nil end\n"
                        + "redis.call('ZREM', KEYS[1], z[1])\n"
                        + "return {z[1], z[2]}");
        POP_MIN_SCRIPT.setResultType(List.class);
    }

    private final StringRedisTemplate stringRedisTemplate;
    private final OmsOrderService omsOrderService;

    public OrderExpireScheduler(StringRedisTemplate stringRedisTemplate,
                                OmsOrderService omsOrderService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.omsOrderService = omsOrderService;
    }

    @Scheduled(fixedDelay = 10000L)
    public void closeExpiredOrders() {
        long now = System.currentTimeMillis();

        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            @SuppressWarnings("unchecked")
            List<Object> popped = stringRedisTemplate.execute(
                    POP_MIN_SCRIPT,
                    Collections.singletonList(RedisKeyConstant.ORDER_EXPIRE_ZSET));

            if (popped == null || popped.size() < 2) {
                return;
            }

            String orderNo = popped.get(0).toString();
            double score = Double.parseDouble(popped.get(1).toString());

            // 还没到期：放回去并结束本轮
            if (score > now) {
                stringRedisTemplate.opsForZSet().add(RedisKeyConstant.ORDER_EXPIRE_ZSET, orderNo, score);
                return;
            }

            try {
                omsOrderService.closeExpiredOrder(orderNo);
            } catch (Exception e) {
                log.error("关闭过期订单失败, orderNo={}", orderNo, e);
            }
        }
    }
}
