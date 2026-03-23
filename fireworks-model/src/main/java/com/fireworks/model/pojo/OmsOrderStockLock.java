package com.fireworks.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 锁库存实体。
 */
@Data
@TableName("oms_order_stock_lock")
@ApiModel(value = "OmsOrderStockLock", description = "订单锁库存")
public class OmsOrderStockLock {

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

    @ApiModelProperty(value = "锁定数量")
    private Integer lockQuantity;

    @ApiModelProperty(value = "锁库存状态：0-已锁定，1-已扣减，2-已释放")
    private Integer lockStatus;

    @ApiModelProperty(value = "锁库存过期时间")
    private Date expireTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
