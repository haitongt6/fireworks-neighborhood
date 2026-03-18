package com.fireworks.service.cart;

import java.math.BigDecimal;

/**
 * Redis Hash 中购物车单条目的值对象（JSON 序列化存储）。
 */
public class CartItemEntry {

    private Integer quantity;

    private BigDecimal priceSnapshot;

    private String titleSnapshot;

    private String imageSnapshot;

    public CartItemEntry() {
    }

    public CartItemEntry(Integer quantity, BigDecimal priceSnapshot,
                         String titleSnapshot, String imageSnapshot) {
        this.quantity = quantity;
        this.priceSnapshot = priceSnapshot;
        this.titleSnapshot = titleSnapshot;
        this.imageSnapshot = imageSnapshot;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceSnapshot() {
        return priceSnapshot;
    }

    public void setPriceSnapshot(BigDecimal priceSnapshot) {
        this.priceSnapshot = priceSnapshot;
    }

    public String getTitleSnapshot() {
        return titleSnapshot;
    }

    public void setTitleSnapshot(String titleSnapshot) {
        this.titleSnapshot = titleSnapshot;
    }

    public String getImageSnapshot() {
        return imageSnapshot;
    }

    public void setImageSnapshot(String imageSnapshot) {
        this.imageSnapshot = imageSnapshot;
    }
}
