package com.fireworks.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 后台首页统计数据。
 */
@ApiModel(value = "AdminHomeStatisticsVO", description = "后台首页统计数据")
public class AdminHomeStatisticsVO {

    @ApiModelProperty(value = "销售总额")
    private BigDecimal totalSalesAmount;

    @ApiModelProperty(value = "订单各状态数量")
    private OrderStatusCountVO orderStatusCount;

    @ApiModelProperty(value = "新增用户数")
    private Long newMemberCount;

    @ApiModelProperty(value = "热销商品Top5")
    private List<TopProductVO> topProducts = new ArrayList<>();

    @ApiModelProperty(value = "销售趋势")
    private List<SalesTrendVO> salesTrend = new ArrayList<>();

    @ApiModelProperty(value = "最近10条订单")
    private List<RecentOrderVO> recentOrders = new ArrayList<>();

    public BigDecimal getTotalSalesAmount() {
        return totalSalesAmount;
    }

    public void setTotalSalesAmount(BigDecimal totalSalesAmount) {
        this.totalSalesAmount = totalSalesAmount;
    }

    public OrderStatusCountVO getOrderStatusCount() {
        return orderStatusCount;
    }

    public void setOrderStatusCount(OrderStatusCountVO orderStatusCount) {
        this.orderStatusCount = orderStatusCount;
    }

    public Long getNewMemberCount() {
        return newMemberCount;
    }

    public void setNewMemberCount(Long newMemberCount) {
        this.newMemberCount = newMemberCount;
    }

    public List<TopProductVO> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopProductVO> topProducts) {
        this.topProducts = topProducts;
    }

    public List<SalesTrendVO> getSalesTrend() {
        return salesTrend;
    }

    public void setSalesTrend(List<SalesTrendVO> salesTrend) {
        this.salesTrend = salesTrend;
    }

    public List<RecentOrderVO> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<RecentOrderVO> recentOrders) {
        this.recentOrders = recentOrders;
    }

    @ApiModel(value = "OrderStatusCountVO", description = "订单状态数量统计")
    public static class OrderStatusCountVO {

        @ApiModelProperty(value = "待支付数量")
        private Long waitPay;

        @ApiModelProperty(value = "已支付数量")
        private Long paid;

        @ApiModelProperty(value = "已取消数量")
        private Long canceled;

        @ApiModelProperty(value = "已关闭数量")
        private Long closed;

        @ApiModelProperty(value = "已完成数量")
        private Long finished;

        public Long getWaitPay() {
            return waitPay;
        }

        public void setWaitPay(Long waitPay) {
            this.waitPay = waitPay;
        }

        public Long getPaid() {
            return paid;
        }

        public void setPaid(Long paid) {
            this.paid = paid;
        }

        public Long getCanceled() {
            return canceled;
        }

        public void setCanceled(Long canceled) {
            this.canceled = canceled;
        }

        public Long getClosed() {
            return closed;
        }

        public void setClosed(Long closed) {
            this.closed = closed;
        }

        public Long getFinished() {
            return finished;
        }

        public void setFinished(Long finished) {
            this.finished = finished;
        }
    }

    @ApiModel(value = "TopProductVO", description = "热销商品")
    public static class TopProductVO {

        @ApiModelProperty(value = "商品ID")
        private Long productId;

        @ApiModelProperty(value = "商品标题")
        private String productTitle;

        @ApiModelProperty(value = "商品图片")
        private String productImage;

        @ApiModelProperty(value = "总销量")
        private Long totalSales;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductTitle() {
            return productTitle;
        }

        public void setProductTitle(String productTitle) {
            this.productTitle = productTitle;
        }

        public String getProductImage() {
            return productImage;
        }

        public void setProductImage(String productImage) {
            this.productImage = productImage;
        }

        public Long getTotalSales() {
            return totalSales;
        }

        public void setTotalSales(Long totalSales) {
            this.totalSales = totalSales;
        }
    }

    @ApiModel(value = "SalesTrendVO", description = "销售趋势")
    public static class SalesTrendVO {

        @ApiModelProperty(value = "日期，格式 yyyy-MM-dd")
        private String date;

        @ApiModelProperty(value = "当日销售额")
        private BigDecimal amount;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    @ApiModel(value = "RecentOrderVO", description = "最近订单")
    public static class RecentOrderVO {

        @ApiModelProperty(value = "订单号")
        private String orderNo;

        @ApiModelProperty(value = "订单状态：0-待支付，1-已支付，2-已取消，3-已关闭，4-已完成")
        private Integer orderStatus;

        @ApiModelProperty(value = "订单总金额")
        private BigDecimal totalAmount;

        @ApiModelProperty(value = "实付金额")
        private BigDecimal payAmount;

        @ApiModelProperty(value = "创建时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date createTime;

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public Integer getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(Integer orderStatus) {
            this.orderStatus = orderStatus;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public BigDecimal getPayAmount() {
            return payAmount;
        }

        public void setPayAmount(BigDecimal payAmount) {
            this.payAmount = payAmount;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }
    }
}
