package com.fireworks.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 提交订单结果。
 */
@Data
@ApiModel(value = "OrderSubmitVO", description = "提交订单结果")
public class OrderSubmitVO {

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "支付单号")
    private String payOrderNo;

    @ApiModelProperty(value = "订单状态：0-待支付")
    private Integer orderStatus;

    @ApiModelProperty(value = "支付状态：0-未支付")
    private Integer payStatus;

    @ApiModelProperty(value = "订单过期时间（15分钟后）")
    private Date expireTime;

    @ApiModelProperty(value = "应付金额")
    private BigDecimal payAmount;
}
