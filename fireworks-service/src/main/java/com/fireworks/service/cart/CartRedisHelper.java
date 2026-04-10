package com.fireworks.service.cart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fireworks.model.constant.RedisKeyConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 购物车 Redis Hash 操作封装。
 * <p>
 * Key：{@code cart:{userId}}，类型：Hash，Field：productId（字符串），Value：{@link CartItemEntry} JSON。
 * TTL：当天 23:59:59 过期（每次写操作刷新）。
 * </p>
 */
@Component
public class CartRedisHelper {

    private static final Logger log = LoggerFactory.getLogger(CartRedisHelper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final StringRedisTemplate stringRedisTemplate;

    public CartRedisHelper(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // ─────────────────────────────────────────────
    // 读操作
    // ─────────────────────────────────────────────

    /**
     * 获取用户购物车全部条目，key 不存在时返回空 Map。
     */
    public Map<String, CartItemEntry> getCart(Long userId) {
        String key = buildKey(userId);
        Map<Object, Object> raw = stringRedisTemplate.opsForHash().entries(key);
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, CartItemEntry> result = new HashMap<String, CartItemEntry>();
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            String field = (String) entry.getKey();
            String json = (String) entry.getValue();
            try {
                CartItemEntry item = MAPPER.readValue(json, new TypeReference<CartItemEntry>() {});
                result.put(field, item);
            } catch (Exception e) {
                log.warn("购物车 Redis 条目反序列化失败, userId={}, field={}", userId, field, e);
            }
        }
        return result;
    }

    /**
     * 获取单个商品的购物车条目，不存在返回 null。
     */
    public CartItemEntry getItem(Long userId, Long productId) {
        String json = (String) stringRedisTemplate.opsForHash()
                .get(buildKey(userId), String.valueOf(productId));
        if (json == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, CartItemEntry.class);
        } catch (Exception e) {
            log.warn("购物车 Redis 单项反序列化失败, userId={}, productId={}", userId, productId, e);
            return null;
        }
    }

    /**
     * 判断购物车 key 是否存在（用于判断是否需要从 MySQL 预热）。
     */
    public boolean exists(Long userId) {
        Boolean hasKey = stringRedisTemplate.hasKey(buildKey(userId));
        return Boolean.TRUE.equals(hasKey);
    }

    // ─────────────────────────────────────────────
    // 写操作
    // ─────────────────────────────────────────────

    /**
     * 设置（新增或覆盖）购物车单项，并刷新 TTL 至当天 23:59:59。
     */
    public void setItem(Long userId, Long productId, CartItemEntry entry) {
        String key = buildKey(userId);
        stringRedisTemplate.opsForHash().put(key, String.valueOf(productId), toJson(entry));
        refreshTtl(key);
    }

    /**
     * 批量预热购物车条目（从 MySQL 加载后回填 Redis）。
     */
    public void setAll(Long userId, Map<String, CartItemEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        String key = buildKey(userId);
        Map<String, String> rawMap = new HashMap<String, String>();
        for (Map.Entry<String, CartItemEntry> e : entries.entrySet()) {
            rawMap.put(e.getKey(), toJson(e.getValue()));
        }
        stringRedisTemplate.opsForHash().putAll(key, rawMap);
        refreshTtl(key);
    }

    /**
     * 删除购物车中的单个商品条目，并刷新 TTL。
     */
    public void removeItem(Long userId, Long productId) {
        String key = buildKey(userId);
        stringRedisTemplate.opsForHash().delete(key, String.valueOf(productId));
        // 删除后若 hash 非空则刷新 TTL，否则 key 已不存在无需处理
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            refreshTtl(key);
        }
    }

    /**
     * 清空整个购物车（删除 key）。
     */
    public void clear(Long userId) {
        stringRedisTemplate.delete(buildKey(userId));
    }

    /**
     * 删除用户购物车 Redis Key（与 {@link #clear} 相同），用于 DB 已成功但 Redis 写入失败时，
     * 强制下次 {@code list()} 走空 Map 并从 MySQL 回源，避免长期脏读。
     */
    public void invalidateUserCart(Long userId) {
        stringRedisTemplate.delete(buildKey(userId));
    }

    // ─────────────────────────────────────────────
    // 私有工具
    // ─────────────────────────────────────────────

    private String buildKey(Long userId) {
        return RedisKeyConstant.CART_KEY_PREFIX + userId;
    }

    /**
     * 刷新 TTL 至当天 23:59:59。
     */
    private void refreshTtl(String key) {
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Date endDate = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
        long secondsUntilEnd = (endDate.getTime() - System.currentTimeMillis()) / 1000;
        if (secondsUntilEnd > 0) {
            stringRedisTemplate.expire(key, secondsUntilEnd, TimeUnit.SECONDS);
        }
    }

    private String toJson(CartItemEntry entry) {
        try {
            return MAPPER.writeValueAsString(entry);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("购物车条目序列化失败", e);
        }
    }
}
