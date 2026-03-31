package com.fireworks.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fireworks.model.pojo.OmsCartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 购物车明细 Mapper。
 */
@Mapper
public interface OmsCartItemMapper extends BaseMapper<OmsCartItem> {

    /**
     * 单行 upsert，依赖唯一索引 uk_user_product(user_id, product_id)。
     */
    void upsert(@Param("userId") Long userId,
                @Param("productId") Long productId,
                @Param("quantity") Integer quantity,
                @Param("priceSnapshot") BigDecimal priceSnapshot,
                @Param("titleSnapshot") String titleSnapshot,
                @Param("imageSnapshot") String imageSnapshot);
}
