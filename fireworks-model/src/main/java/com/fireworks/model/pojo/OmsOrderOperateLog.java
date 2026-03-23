package com.fireworks.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 订单操作日志实体。
 */
@Data
@TableName("oms_order_operate_log")
@ApiModel(value = "OmsOrderOperateLog", description = "订单操作日志")
public class OmsOrderOperateLog {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "操作类型：1-提交订单，2-发起支付，3-支付成功，4-支付失败，5-用户取消，6-超时关闭，7-释放锁库存，8-后台关闭")
    private Integer operateType;

    @ApiModelProperty(value = "变更前状态")
    private String preStatus;

    @ApiModelProperty(value = "变更后状态")
    private String postStatus;

    @ApiModelProperty(value = "备注")
    private String note;

    @ApiModelProperty(value = "操作人ID")
    private Long operatorId;

    @ApiModelProperty(value = "操作人类型：1-用户，2-系统，3-后台管理员")
    private Integer operatorType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
