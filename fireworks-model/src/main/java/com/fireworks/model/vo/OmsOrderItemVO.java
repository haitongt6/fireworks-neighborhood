package com.fireworks.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单商品明细。
 */
@Data
@ApiModel(value = "OmsOrderItemVO", description = "订单商品明细")
public class OmsOrderItemVO {

    @ApiModelProperty(value = "商品ID")
    private Long productId;

    @ApiModelProperty(value = "商品标题")
    private String productTitle;

    @ApiModelProperty(value = "商品图片")
    private String productImage;

    @ApiModelProperty(value = "商品单价")
    private BigDecimal productPrice;

    @ApiModelProperty(value = "购买数量")
    private Integer quantity;

    @ApiModelProperty(value = "小计金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "是否相对购物车价格快照有变动，仅确认页可能为 true")
    private Boolean priceChanged;

    @ApiModelProperty(value = "购物车价格快照，仅 priceChanged 为 true 时有值")
    private BigDecimal previousPrice;
}
