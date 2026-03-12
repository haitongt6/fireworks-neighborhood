package com.fireworks.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 泛型存取工具类。
 * <p>
 * 基于 {@link StringRedisTemplate} + {@link ObjectMapper}，
 * 存入时将对象序列化为 JSON 字符串，取出时反序列化为指定类型，
 * 保证 Redis 中存储的值为纯净 JSON（不含 @class 类型标记）。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>
 * // 存
 * redisUtil.set("user:1", userObj);
 * redisUtil.set("token:abc", tokenObj, 30, TimeUnit.MINUTES);
 *
 * // 取
 * User user = redisUtil.get("user:1", User.class);
 * </pre>
 */
@Component
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RedisUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 存入对象（无过期时间）。
     */
    public <T> void set(String key, T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json);
        } catch (Exception e) {
            throw new RuntimeException("Redis set 序列化失败, key=" + key, e);
        }
    }

    /**
     * 存入对象并设置过期时间。
     */
    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, timeout, timeUnit);
        } catch (Exception e) {
            throw new RuntimeException("Redis set 序列化失败, key=" + key, e);
        }
    }

    /**
     * 按类型取出对象。
     *
     * @return 对象实例；key 不存在时返回 null
     */
    public <T> T get(String key, Class<T> clazz) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Redis get 反序列化失败, key=" + key, e);
        }
    }

    /**
     * 取出字符串值（无需反序列化的简单场景）。
     *
     * @return 字符串值；key 不存在时返回 null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定 key。
     *
     * @return true 表示删除成功
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 判断 key 是否存在。
     */
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 为 key 设置过期时间。
     */
    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return stringRedisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * 获取 key 的剩余过期时间（秒）。
     *
     * @return 剩余秒数；key 不存在返回 -2，永不过期返回 -1
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}
