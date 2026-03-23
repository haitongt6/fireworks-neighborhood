package com.fireworks.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 提交订单参数。
 */
@ApiModel(value = "OrderSubmitParam", description = "提交订单请求参数")
public class OrderSubmitParam {

    @NotBlank(message = "submitToken不能为空")
    @ApiModelProperty(value = "提交幂等Token（从确认页接口获取）", required = true)
    private String submitToken;

    @NotNull(message = "订单来源不能为空")
    @ApiModelProperty(value = "订单来源：1-购物车下单，2-立即购买", required = true, example = "1")
    private Integer sourceType;

    @NotEmpty(message = "下单商品不能为空")
    @Valid
    @ApiModelProperty(value = "下单商品列表", required = true)
    private List<OrderItemSubmitParam> items;

    @NotBlank(message = "收货人不能为空")
    @ApiModelProperty(value = "收货人姓名", required = true, example = "张三")
    private String receiverName;

    @NotBlank(message = "收货电话不能为空")
    @ApiModelProperty(value = "收货人电话", required = true, example = "13800138000")
    private String receiverPhone;

    @NotBlank(message = "省不能为空")
    @ApiModelProperty(value = "省", required = true, example = "广东省")
    private String receiverProvince;

    @NotBlank(message = "市不能为空")
    @ApiModelProperty(value = "市", required = true, example = "深圳市")
    private String receiverCity;

    @NotBlank(message = "区不能为空")
    @ApiModelProperty(value = "区", required = true, example = "南山区")
    private String receiverDistrict;

    @NotBlank(message = "详细地址不能为空")
    @ApiModelProperty(value = "详细地址", required = true, example = "科技园南区XX栋")
    private String receiverDetailAddress;

    @ApiModelProperty(value = "用户备注", example = "请尽快发货")
    private String remark;

    public String getSubmitToken() { return submitToken; }
    public void setSubmitToken(String submitToken) { this.submitToken = submitToken; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
    public List<OrderItemSubmitParam> getItems() { return items; }
    public void setItems(List<OrderItemSubmitParam> items) { this.items = items; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getReceiverProvince() { return receiverProvince; }
    public void setReceiverProvince(String receiverProvince) { this.receiverProvince = receiverProvince; }
    public String getReceiverCity() { return receiverCity; }
    public void setReceiverCity(String receiverCity) { this.receiverCity = receiverCity; }
    public String getReceiverDistrict() { return receiverDistrict; }
    public void setReceiverDistrict(String receiverDistrict) { this.receiverDistrict = receiverDistrict; }
    public String getReceiverDetailAddress() { return receiverDetailAddress; }
    public void setReceiverDetailAddress(String receiverDetailAddress) { this.receiverDetailAddress = receiverDetailAddress; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
