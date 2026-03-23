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
 * 订单主表实体。
 */
@Data
@TableName("oms_order")
@ApiModel(value = "OmsOrder", description = "订单主表")
public class OmsOrder {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
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

    @ApiModelProperty(value = "提交幂等token", hidden = true)
    private String submitToken;

    @ApiModelProperty(value = "支付过期时间")
    private Date expireTime;

    @ApiModelProperty(value = "支付成功时间")
    private Date payTime;

    @ApiModelProperty(value = "取消时间")
    private Date cancelTime;

    @ApiModelProperty(value = "取消原因")
    private String cancelReason;

    @ApiModelProperty(value = "逻辑删除：0-否，1-是", hidden = true)
    private Integer deleted;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
