package com.fireworks.model.dto;

import javax.validation.constraints.NotNull;

/**
 * 删除购物车单项请求参数。
 */
public class CartRemoveParam {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
