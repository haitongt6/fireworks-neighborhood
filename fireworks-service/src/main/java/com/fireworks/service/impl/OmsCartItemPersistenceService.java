package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireworks.model.pojo.OmsCartItem;
import com.fireworks.service.mapper.OmsCartItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 购物车 MySQL 同步落库（仅数据库事务，不包含 Redis）。
 * <p>
 * 从 {@code OmsCartServiceImpl} 单独抽出，避免同类自调用导致 {@code @Transactional} 不生效。
 * upsert 使用 {@code INSERT ... ON DUPLICATE KEY UPDATE}，依赖 {@code uk_user_product(user_id, product_id)}。
 * </p>
 */
@Service
public class OmsCartItemPersistenceService {

    private final OmsCartItemMapper cartItemMapper;

    public OmsCartItemPersistenceService(OmsCartItemMapper cartItemMapper) {
        this.cartItemMapper = cartItemMapper;
    }

    /**
     * 加购 / 改数量：与已停用的 CartPersistConsumer#handleUpsert 语义一致。
     */
    @Transactional(rollbackFor = Exception.class)
    public void upsert(Long userId, Long productId, Integer quantity,
                       BigDecimal priceSnapshot, String titleSnapshot, String imageSnapshot) {
        cartItemMapper.upsert(userId, productId, quantity, priceSnapshot, titleSnapshot, imageSnapshot);
    }

    /**
     * 删除单项。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long userId, Long productId) {
        cartItemMapper.delete(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getUserId, userId)
                        .eq(OmsCartItem::getProductId, productId));
    }

    /**
     * 按用户清空购物车行。
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearUser(Long userId) {
        cartItemMapper.delete(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getUserId, userId));
    }
}
