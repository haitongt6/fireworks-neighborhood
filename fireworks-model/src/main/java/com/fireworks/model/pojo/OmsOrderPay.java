package com.fireworks.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付单实体。
 */
@Data
@TableName("oms_order_pay")
@ApiModel(value = "OmsOrderPay", description = "订单支付单")
public class OmsOrderPay {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "支付单号")
    private String payOrderNo;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "支付方式：1-微信，2-支付宝，9-模拟支付")
    private Integer payType;

    @ApiModelProperty(value = "支付状态：0-待支付，1-支付中，2-支付成功，3-支付失败，4-已关闭")
    private Integer payStatus;

    @ApiModelProperty(value = "支付金额")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "第三方交易号")
    private String thirdPartyTradeNo;

    @ApiModelProperty(value = "支付请求幂等号")
    private String requestNo;

    @ApiModelProperty(value = "支付成功时间")
    private Date payTime;

    @ApiModelProperty(value = "支付失败原因")
    private String failReason;

    @ApiModelProperty(value = "通知处理状态：0-未处理，1-已处理")
    private Integer notifyStatus;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
