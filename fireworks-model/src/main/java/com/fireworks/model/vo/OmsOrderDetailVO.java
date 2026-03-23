package com.fireworks.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单详情。
 */
@Data
@ApiModel(value = "OmsOrderDetailVO", description = "订单详情")
public class OmsOrderDetailVO {

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

    @ApiModelProperty(value = "收货人")
    private String receiverName;

    @ApiModelProperty(value = "收货电话")
    private String receiverPhone;

    @ApiModelProperty(value = "省")
    private String receiverProvince;

    @ApiModelProperty(value = "市")
    private String receiverCity;

    @ApiModelProperty(value = "区")
    private String receiverDistrict;

    @ApiModelProperty(value = "详细地址")
    private String receiverDetailAddress;

    @ApiModelProperty(value = "用户备注")
    private String remark;

    @ApiModelProperty(value = "支付过期时间")
    private Date expireTime;

    @ApiModelProperty(value = "支付成功时间")
    private Date payTime;

    @ApiModelProperty(value = "取消时间")
    private Date cancelTime;

    @ApiModelProperty(value = "取消原因")
    private String cancelReason;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "商品明细列表")
    private List<OmsOrderItemVO> items;
}
