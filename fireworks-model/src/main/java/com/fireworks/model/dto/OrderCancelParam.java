package com.fireworks.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

/**
 * 取消订单参数。
 */
@ApiModel(value = "OrderCancelParam", description = "取消订单请求参数")
public class OrderCancelParam {

    @NotBlank(message = "订单号不能为空")
    @ApiModelProperty(value = "订单号", required = true, example = "20240101120000000001")
    private String orderNo;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}
