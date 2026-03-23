package com.fireworks.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 订单列表查询参数。
 */
@ApiModel(value = "OmsOrderQueryParam", description = "订单列表查询参数")
public class OmsOrderQueryParam {

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "用户ID（后台专用）")
    private Long userId;

    @ApiModelProperty(value = "订单状态：0-待支付，1-已支付，2-已取消，3-已关闭，4-已完成")
    private Integer orderStatus;

    @ApiModelProperty(value = "支付状态：0-未支付，1-支付中，2-支付成功，3-支付失败，4-已关闭")
    private Integer payStatus;

    @ApiModelProperty(value = "创建时间起始（yyyy-MM-dd）", example = "2024-01-01")
    private String createTimeStart;

    @ApiModelProperty(value = "创建时间截止（yyyy-MM-dd）", example = "2024-12-31")
    private String createTimeEnd;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize = 10;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }
    public Integer getPayStatus() { return payStatus; }
    public void setPayStatus(Integer payStatus) { this.payStatus = payStatus; }
    public String getCreateTimeStart() { return createTimeStart; }
    public void setCreateTimeStart(String createTimeStart) { this.createTimeStart = createTimeStart; }
    public String getCreateTimeEnd() { return createTimeEnd; }
    public void setCreateTimeEnd(String createTimeEnd) { this.createTimeEnd = createTimeEnd; }
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
