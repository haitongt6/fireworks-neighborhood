package com.fireworks.model.vo;

import java.math.BigDecimal;

/**
 * 购物车列表项 VO。
 */
public class CartItemVO {

    private Long id;

    private Long productId;

    /** 购买数量 */
    private Integer quantity;

    /** 加购时价格快照 */
    private BigDecimal priceSnapshot;

    /** 加购时标题快照 */
    private String titleSnapshot;

    /** 加购时主图快照 */
    private String imageSnapshot;

    /** 商品当前状态：1-上架，0-下架 */
    private Integer productStatus;

    /** 商品当前库存 */
    private Integer productStock;

    /** 每人限购数量，NULL 表示不限购 */
    private Integer limitPerUser;

    /**
     * 是否失效：商品下架、库存不足或超限购均视为失效。
     */
    private Boolean invalid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public Integer getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(Integer productStatus) {
        this.productStatus = productStatus;
    }

    public Integer getProductStock() {
        return productStock;
    }

    public void setProductStock(Integer productStock) {
        this.productStock = productStock;
    }

    public Integer getLimitPerUser() {
        return limitPerUser;
    }

    public void setLimitPerUser(Integer limitPerUser) {
        this.limitPerUser = limitPerUser;
    }

    public Boolean getInvalid() {
        return invalid;
    }

    public void setInvalid(Boolean invalid) {
        this.invalid = invalid;
    }
}
