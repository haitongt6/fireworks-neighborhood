package com.fireworks.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

/**
 * 模拟支付参数。
 */
@ApiModel(value = "MockPayParam", description = "模拟支付请求参数")
public class MockPayParam {

    @NotBlank(message = "订单号不能为空")
    @ApiModelProperty(value = "订单号", required = true, example = "20240101120000000001")
    private String orderNo;

    @NotBlank(message = "requestNo不能为空")
    @ApiModelProperty(value = "支付请求幂等号", required = true, example = "REQ20240101120000001")
    private String requestNo;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }
}
