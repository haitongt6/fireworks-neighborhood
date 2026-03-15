package com.fireworks.service.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Redis 泛型存取工具类（静态方法调用，无需注入）。
 * <p>
 * 应用启动后通过 {@link #init()} 将 Spring 管理的 {@link StringRedisTemplate}
 * 赋值给静态字段，之后即可在任意位置通过静态方法直接操作 Redis。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>
 * // 存
 * RedisUtil.set("user:1", userObj);
 * RedisUtil.set("token:abc", tokenObj, 30, TimeUnit.MINUTES);
 *
 * // 取
 * User user = RedisUtil.get("user:1", User.class);
 * </pre>
 */
@Component
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private static StringRedisTemplate REDIS_TEMPLATE;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public RedisUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    private void init() {
        REDIS_TEMPLATE = this.stringRedisTemplate;
    }

    /**
     * 存入对象（无过期时间）。
     */
    public static <T> void set(String key, T value) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(value);
            REDIS_TEMPLATE.opsForValue().set(key, json);
        } catch (Exception e) {
            throw new RuntimeException("Redis set 序列化失败, key=" + key, e);
        }
    }

    /**
     * 存入对象并设置过期时间。
     */
    public static <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(value);
            REDIS_TEMPLATE.opsForValue().set(key, json, timeout, timeUnit);
        } catch (Exception e) {
            throw new RuntimeException("Redis set 序列化失败, key=" + key, e);
        }
    }

    /**
     * 按类型取出对象。
     *
     * @return 对象实例；key 不存在时返回 null
     */
    public static <T> T get(String key, Class<T> clazz) {
        String json = REDIS_TEMPLATE.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Redis get 反序列化失败, key=" + key, e);
        }
    }

    /**
     * 按 TypeReference 取出对象（支持泛型如 List&lt;T&gt;）。
     *
     * @return 对象实例；key 不存在时返回 null
     */
    public static <T> T get(String key, TypeReference<T> typeRef) {
        String json = REDIS_TEMPLATE.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Redis get 反序列化失败, key=" + key, e);
        }
    }

    /**
     * 取出字符串值（无需反序列化的简单场景）。
     *
     * @return 字符串值；key 不存在时返回 null
     */
    public static String get(String key) {
        return REDIS_TEMPLATE.opsForValue().get(key);
    }

    /**
     * 删除指定 key。
     *
     * @return true 表示删除成功
     */
    public static Boolean delete(String key) {
        return REDIS_TEMPLATE.delete(key);
    }

    /**
     * 判断 key 是否存在。
     */
    public static Boolean hasKey(String key) {
        return REDIS_TEMPLATE.hasKey(key);
    }

    /**
     * 为 key 设置过期时间。
     */
    public static Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return REDIS_TEMPLATE.expire(key, timeout, timeUnit);
    }

    /**
     * 获取 key 的剩余过期时间（秒）。
     *
     * @return 剩余秒数；key 不存在返回 -2，永不过期返回 -1
     */
    public static Long getExpire(String key) {
        return REDIS_TEMPLATE.getExpire(key, TimeUnit.SECONDS);
    }
}
