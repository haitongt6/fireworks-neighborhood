package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireworks.model.dto.CartAddParam;
import com.fireworks.model.dto.CartRemoveParam;
import com.fireworks.model.dto.CartUpdateParam;
import com.fireworks.model.pojo.OmsCartItem;
import com.fireworks.model.pojo.PmsProduct;
import com.fireworks.model.vo.CartItemVO;
import com.fireworks.service.OmsCartService;
import com.fireworks.service.cart.CartItemEntry;
import com.fireworks.service.cart.CartRedisHelper;
import com.fireworks.service.cart.OmsCartPersistTask;
import com.fireworks.service.mapper.OmsCartItemMapper;
import com.fireworks.service.mapper.PmsProductMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 购物车业务实现。
 * <p>
 * 读：Redis 优先，miss 则查 MySQL 并预热 Redis。<br>
 * 写：先更新 Redis，再异步落库 MySQL，保证最终一致性。
 * </p>
 */
@Service
public class OmsCartServiceImpl implements OmsCartService {

    private static final Logger log = LoggerFactory.getLogger(OmsCartServiceImpl.class);

    /** 默认限购数量（limit_per_user 为 null 时使用） */
    private static final int DEFAULT_LIMIT = 99;

    private final CartRedisHelper cartRedisHelper;
    private final OmsCartPersistTask persistTask;
    private final OmsCartItemMapper cartItemMapper;
    private final PmsProductMapper productMapper;
    private final RedissonClient redissonClient;

    public OmsCartServiceImpl(CartRedisHelper cartRedisHelper,
                              OmsCartPersistTask persistTask,
                              OmsCartItemMapper cartItemMapper,
                              PmsProductMapper productMapper,
                              RedissonClient redissonClient) {
        this.cartRedisHelper = cartRedisHelper;
        this.persistTask = persistTask;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.redissonClient = redissonClient;
    }

    // ─────────────────────────────────────────────
    // 读 - 购物车列表
    // ─────────────────────────────────────────────

    @Override
    public List<CartItemVO> list(Long userId) {
        Map<String, CartItemEntry> redisCart = cartRedisHelper.getCart(userId);

        if (redisCart.isEmpty()) {
            // Redis miss：从 MySQL 加载并预热
            redisCart = loadFromDbAndWarm(userId);
        }

        return buildVOList(userId, redisCart);
    }

    // ─────────────────────────────────────────────
    // 写 - 加购
    // ─────────────────────────────────────────────

    @Override
    public void add(Long userId, CartAddParam param) {
        Long productId = param.getProductId();
        int addQty = param.getQuantity();

        // 分布式锁：锁粒度为 userId + productId，防止同一用户同一商品并发加购数量不准
        String lockKey = "cart:lock:" + userId + ":" + productId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!locked) {
                throw new IllegalStateException("操作频繁，请稍后重试");
            }

            PmsProduct product = productMapper.selectById(productId);
            if (product == null) {
                throw new IllegalArgumentException("商品不存在");
            }
            if (!Integer.valueOf(1).equals(product.getStatus())) {
                throw new IllegalArgumentException("商品已下架");
            }

            // 锁保护内：读-计算-校验-写 串行执行，保证并发下数量准确
            CartItemEntry existing = cartRedisHelper.getItem(userId, productId);
            int newQty = getNewQty(existing, addQty, product);

            // 取第一张图作为快照
            String imageSnapshot = extractFirstImage(product.getImages());

            CartItemEntry entry = new CartItemEntry(
                    newQty, product.getPrice(), product.getTitle(), imageSnapshot);

            // 1. 写 Redis
            cartRedisHelper.setItem(userId, productId, entry);

            // 2. 异步落库
            persistTask.asyncUpsert(userId, productId, newQty,
                    product.getPrice(), product.getTitle(), imageSnapshot);

            log.info("加购成功, userId={}, productId={}, newQty={}", userId, productId, newQty);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("加购被中断，请重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private static int getNewQty(CartItemEntry existing, int addQty, PmsProduct product) {
        int currentQty = (existing != null) ? existing.getQuantity() : 0;
        int newQty = currentQty + addQty;

        // 库存校验
        if (product.getStock() == null || newQty > product.getStock()) {
            throw new IllegalArgumentException("库存不足");
        }

        // 限购校验
        int limit = (product.getLimitPerUser() != null) ? product.getLimitPerUser() : DEFAULT_LIMIT;
        if (newQty > limit) {
            throw new IllegalArgumentException("超出限购数量，每人限购 " + limit + " 件");
        }
        return newQty;
    }

    // ─────────────────────────────────────────────
    // 写 - 修改数量
    // ─────────────────────────────────────────────

    @Override
    public void update(Long userId, CartUpdateParam param) {
        Long productId = param.getProductId();
        int newQty = param.getQuantity();

        PmsProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        // 库存校验
        if (product.getStock() == null || newQty > product.getStock()) {
            throw new IllegalArgumentException("库存不足");
        }

        // 限购校验
        int limit = (product.getLimitPerUser() != null) ? product.getLimitPerUser() : DEFAULT_LIMIT;
        if (newQty > limit) {
            throw new IllegalArgumentException("超出限购数量，每人限购 " + limit + " 件");
        }

        // 取快照（Redis 中已有则复用，否则从商品表取）
        CartItemEntry existing = cartRedisHelper.getItem(userId, productId);
        String imageSnapshot = (existing != null && existing.getImageSnapshot() != null)
                ? existing.getImageSnapshot() : extractFirstImage(product.getImages());

        CartItemEntry entry = new CartItemEntry(
                newQty, product.getPrice(), product.getTitle(), imageSnapshot);

        // 1. 写 Redis
        cartRedisHelper.setItem(userId, productId, entry);

        // 2. 异步落库
        persistTask.asyncUpsert(userId, productId, newQty,
                product.getPrice(), product.getTitle(), imageSnapshot);

        log.debug("修改数量成功, userId={}, productId={}, newQty={}", userId, productId, newQty);
    }

    // ─────────────────────────────────────────────
    // 写 - 删除单项
    // ─────────────────────────────────────────────

    @Override
    public void remove(Long userId, CartRemoveParam param) {
        Long productId = param.getProductId();

        // 1. 删 Redis
        cartRedisHelper.removeItem(userId, productId);

        // 2. 异步落库
        persistTask.asyncDelete(userId, productId);

        log.debug("删除购物车单项成功, userId={}, productId={}", userId, productId);
    }

    // ─────────────────────────────────────────────
    // 写 - 清空
    // ─────────────────────────────────────────────

    @Override
    public void clear(Long userId) {
        // 1. 清 Redis
        cartRedisHelper.clear(userId);

        // 2. 异步落库
        persistTask.asyncClear(userId);

        log.debug("清空购物车成功, userId={}", userId);
    }

    // ─────────────────────────────────────────────
    // 私有方法
    // ─────────────────────────────────────────────

    /**
     * 从 MySQL 加载购物车并预热 Redis，返回条目 Map。
     */
    private Map<String, CartItemEntry> loadFromDbAndWarm(Long userId) {
        List<OmsCartItem> dbItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getUserId, userId));

        if (dbItems == null || dbItems.isEmpty()) {
            return new HashMap<String, CartItemEntry>();
        }

        Map<String, CartItemEntry> entries = new HashMap<String, CartItemEntry>();
        for (OmsCartItem item : dbItems) {
            CartItemEntry entry = new CartItemEntry(
                    item.getQuantity(),
                    item.getPriceSnapshot(),
                    item.getTitleSnapshot(),
                    item.getImageSnapshot());
            entries.put(String.valueOf(item.getProductId()), entry);
        }

        // 预热 Redis
        cartRedisHelper.setAll(userId, entries);
        log.debug("购物车 Redis miss，从 MySQL 预热 {} 条, userId={}", entries.size(), userId);
        return entries;
    }

    /**
     * 将 Redis 中的购物车条目组装为 VO 列表，并补全商品实时信息。
     */
    private List<CartItemVO> buildVOList(Long userId, Map<String, CartItemEntry> redisCart) {
        List<CartItemVO> result = new ArrayList<CartItemVO>();
        if (redisCart.isEmpty()) {
            return result;
        }

        for (Map.Entry<String, CartItemEntry> entry : redisCart.entrySet()) {
            Long productId = Long.valueOf(entry.getKey());
            CartItemEntry redisEntry = entry.getValue();

            PmsProduct product = productMapper.selectById(productId);

            CartItemVO vo = new CartItemVO();
            vo.setProductId(productId);
            vo.setQuantity(redisEntry.getQuantity());
            vo.setPriceSnapshot(redisEntry.getPriceSnapshot());
            vo.setTitleSnapshot(redisEntry.getTitleSnapshot());
            vo.setImageSnapshot(redisEntry.getImageSnapshot());

            if (product != null) {
                vo.setProductStatus(product.getStatus());
                vo.setProductStock(product.getStock());
                vo.setLimitPerUser(product.getLimitPerUser());

                int limit = (product.getLimitPerUser() != null)
                        ? product.getLimitPerUser() : DEFAULT_LIMIT;
                boolean outOfStock = (product.getStock() == null || product.getStock() < redisEntry.getQuantity());
                boolean overLimit = (redisEntry.getQuantity() > limit);
                boolean offShelf = !Integer.valueOf(1).equals(product.getStatus());
                vo.setInvalid(outOfStock || overLimit || offShelf);
            } else {
                // 商品已被删除，标记失效
                vo.setProductStatus(0);
                vo.setProductStock(0);
                vo.setInvalid(true);
            }

            result.add(vo);
        }
        return result;
    }

    /**
     * 从 images 字段中提取第一张图 URL（images 为逗号分隔或 JSON 数组均取第一段）。
     */
    private String extractFirstImage(String images) {
        if (images == null || images.trim().isEmpty()) {
            return null;
        }
        String trimmed = images.trim();
        // JSON 数组格式：["url1","url2"]
        if (trimmed.startsWith("[")) {
            trimmed = trimmed.replaceAll("[\\[\\]\"]", "");
        }
        String[] parts = trimmed.split(",");
        return parts[0].trim();
    }
}
