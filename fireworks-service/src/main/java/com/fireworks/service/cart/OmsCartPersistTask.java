package com.fireworks.service.cart;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireworks.model.pojo.OmsCartItem;
import com.fireworks.service.mapper.OmsCartItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车异步落库任务。
 * <p>
 * 所有方法均标注 {@link Async}，在独立线程池中执行，不阻塞主流程。
 * 按 (userId, productId) 做 upsert 或 delete，保证幂等。
 * 失败重试逻辑通过循环实现，最多重试 3 次，仍失败则记录错误日志。
 * </p>
 */
@Component
public class OmsCartPersistTask {

    private static final Logger log = LoggerFactory.getLogger(OmsCartPersistTask.class);
    private static final int MAX_RETRY = 3;

    private final OmsCartItemMapper cartItemMapper;

    public OmsCartPersistTask(OmsCartItemMapper cartItemMapper) {
        this.cartItemMapper = cartItemMapper;
    }

    /**
     * 异步 upsert 购物车条目（加购 / 改数量场景）。
     */
    @Async
    public void asyncUpsert(Long userId, Long productId, Integer quantity,
                            BigDecimal priceSnapshot, String titleSnapshot, String imageSnapshot) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                OmsCartItem existing = cartItemMapper.selectOne(
                        new LambdaQueryWrapper<OmsCartItem>()
                                .eq(OmsCartItem::getUserId, userId)
                                .eq(OmsCartItem::getProductId, productId));
                if (existing == null) {
                    OmsCartItem item = new OmsCartItem();
                    item.setUserId(userId);
                    item.setProductId(productId);
                    item.setQuantity(quantity);
                    item.setPriceSnapshot(priceSnapshot);
                    item.setTitleSnapshot(titleSnapshot);
                    item.setImageSnapshot(imageSnapshot);
                    cartItemMapper.insert(item);
                } else {
                    existing.setQuantity(quantity);
                    existing.setPriceSnapshot(priceSnapshot);
                    existing.setTitleSnapshot(titleSnapshot);
                    existing.setImageSnapshot(imageSnapshot);
                    cartItemMapper.updateById(existing);
                }
                return;
            } catch (Exception e) {
                log.warn("购物车落库 upsert 第 {}/{} 次失败, userId={}, productId={}",
                        attempt, MAX_RETRY, userId, productId, e);
            }
        }
        log.error("购物车落库 upsert 超过最大重试次数, userId={}, productId={}", userId, productId);
    }

    /**
     * 异步删除购物车单项。
     */
    @Async
    public void asyncDelete(Long userId, Long productId) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                cartItemMapper.delete(
                        new LambdaQueryWrapper<OmsCartItem>()
                                .eq(OmsCartItem::getUserId, userId)
                                .eq(OmsCartItem::getProductId, productId));
                return;
            } catch (Exception e) {
                log.warn("购物车落库 delete 第 {}/{} 次失败, userId={}, productId={}",
                        attempt, MAX_RETRY, userId, productId, e);
            }
        }
        log.error("购物车落库 delete 超过最大重试次数, userId={}, productId={}", userId, productId);
    }

    /**
     * 异步清空用户购物车。
     */
    @Async
    public void asyncClear(Long userId) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                cartItemMapper.delete(
                        new LambdaQueryWrapper<OmsCartItem>()
                                .eq(OmsCartItem::getUserId, userId));
                return;
            } catch (Exception e) {
                log.warn("购物车落库 clear 第 {}/{} 次失败, userId={}", attempt, MAX_RETRY, userId, e);
            }
        }
        log.error("购物车落库 clear 超过最大重试次数, userId={}", userId);
    }
}
