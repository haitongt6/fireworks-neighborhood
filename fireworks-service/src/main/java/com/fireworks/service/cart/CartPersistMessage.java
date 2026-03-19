package com.fireworks.service.cart;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车落库消息体。
 * <p>
 * type: UPSERT / DELETE / CLEAR
 * </p>
 */
public class CartPersistMessage implements Serializable {

    /** 操作类型 */
    public enum Type {
        /** 新增或更新（加购 / 改数量） */
        UPSERT,
        /** 删除单项 */
        DELETE,
        /** 清空购物车 */
        CLEAR
    }

    private Type type;
    private Long userId;
    /** DELETE/CLEAR 时为 null */
    private Long productId;
    /** DELETE/CLEAR 时为 null */
    private Integer quantity;
    private BigDecimal priceSnapshot;
    private String titleSnapshot;
    private String imageSnapshot;

    /** Jackson 反序列化需要无参构造 */
    public CartPersistMessage() {}

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPriceSnapshot() { return priceSnapshot; }
    public void setPriceSnapshot(BigDecimal priceSnapshot) { this.priceSnapshot = priceSnapshot; }

    public String getTitleSnapshot() { return titleSnapshot; }
    public void setTitleSnapshot(String titleSnapshot) { this.titleSnapshot = titleSnapshot; }

    public String getImageSnapshot() { return imageSnapshot; }
    public void setImageSnapshot(String imageSnapshot) { this.imageSnapshot = imageSnapshot; }
}
