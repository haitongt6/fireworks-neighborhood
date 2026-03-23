package com.fireworks.service.order;

import com.fireworks.model.constant.RedisKeyConstant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 订单 Redis 辅助。
 */
@Component
public class OrderRedisHelper {

    private final StringRedisTemplate stringRedisTemplate;

    public OrderRedisHelper(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String createSubmitToken(Long userId) {
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        String key = buildSubmitTokenKey(userId, token);
        stringRedisTemplate.opsForValue().set(key, "1", 15, TimeUnit.MINUTES);
        return token;
    }

    public boolean consumeSubmitToken(Long userId, String token) {
        Boolean deleted = stringRedisTemplate.delete(buildSubmitTokenKey(userId, token));
        return Boolean.TRUE.equals(deleted);
    }

    public void addExpireOrder(String orderNo, long expireTimestamp) {
        stringRedisTemplate.opsForZSet().add(RedisKeyConstant.ORDER_EXPIRE_ZSET, orderNo, expireTimestamp);
    }

    public void removeExpireOrder(String orderNo) {
        stringRedisTemplate.opsForZSet().remove(RedisKeyConstant.ORDER_EXPIRE_ZSET, orderNo);
    }

    public String buildPayLockKey(String orderNo) {
        return RedisKeyConstant.ORDER_PAY_LOCK_PREFIX + orderNo;
    }

    public String buildCloseLockKey(String orderNo) {
        return RedisKeyConstant.ORDER_CLOSE_LOCK_PREFIX + orderNo;
    }

    public String buildSubmitLockKey(Long userId) {
        return RedisKeyConstant.ORDER_SUBMIT_LOCK_PREFIX + userId;
    }

    private String buildSubmitTokenKey(Long userId, String token) {
        return RedisKeyConstant.ORDER_SUBMIT_TOKEN_PREFIX + userId + ":" + token;
    }
}
