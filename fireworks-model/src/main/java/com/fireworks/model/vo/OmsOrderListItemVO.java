package com.fireworks.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单列表项。
 */
@Data
@ApiModel(value = "OmsOrderListItemVO", description = "订单列表项")
public class OmsOrderListItemVO {

    @ApiModelProperty(value = "订单ID")
    private Long id;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "订单状态：0-待支付，1-已支付，2-已取消，3-已关闭，4-已完成")
    private Integer orderStatus;

    @ApiModelProperty(value = "支付状态：0-未支付，1-支付中，2-支付成功，3-支付失败，4-已关闭")
    private Integer payStatus;

    @ApiModelProperty(value = "订单来源：1-购物车下单，2-立即购买")
    private Integer sourceType;

    @ApiModelProperty(value = "订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "实付金额")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "商品总件数")
    private Integer itemCount;

    @ApiModelProperty(value = "支付过期时间")
    private Date expireTime;

    @ApiModelProperty(value = "支付成功时间")
    private Date payTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
