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
 * 订单明细实体。
 */
@Data
@TableName("oms_order_item")
@ApiModel(value = "OmsOrderItem", description = "订单明细")
public class OmsOrderItem {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "商品ID")
    private Long productId;

    @ApiModelProperty(value = "商品标题快照")
    private String productTitle;

    @ApiModelProperty(value = "商品图片快照")
    private String productImage;

    @ApiModelProperty(value = "商品单价快照")
    private BigDecimal productPrice;

    @ApiModelProperty(value = "购买数量")
    private Integer quantity;

    @ApiModelProperty(value = "明细小计金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "明细状态：0-正常，1-已取消")
    private Integer itemStatus;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
