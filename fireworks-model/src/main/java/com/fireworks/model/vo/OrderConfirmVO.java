package com.fireworks.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认页返回。
 */
@Data
@ApiModel(value = "OrderConfirmVO", description = "订单确认页返回")
public class OrderConfirmVO {

    @ApiModelProperty(value = "提交幂等Token，下单时必须携带")
    private String submitToken;

    @ApiModelProperty(value = "订单来源：1-购物车下单，2-立即购买")
    private Integer sourceType;

    @ApiModelProperty(value = "商品总件数")
    private Integer itemCount;

    @ApiModelProperty(value = "订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "有效商品列表")
    private List<OmsOrderItemVO> items;
}
