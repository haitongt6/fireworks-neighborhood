package com.fireworks.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单确认页中单条异常提示（库存、限购、下架、价格变动等）。
 */
@Data
@ApiModel(value = "OrderConfirmIssueVO", description = "订单确认页异常提示项")
public class OrderConfirmIssueVO {

    @ApiModelProperty(value = "问题类型：INSUFFICIENT_STOCK-库存不足，EXCEED_LIMIT-超出限购，PRICE_CHANGED-价格变动，PRODUCT_OFFLINE-商品下架或不存在", required = true, example = "INSUFFICIENT_STOCK")
    private String issueType;

    @ApiModelProperty(value = "商品ID", required = true)
    private Long productId;

    @ApiModelProperty(value = "商品标题（展示用）")
    private String productTitle;

    @ApiModelProperty(value = "商品图片快照")
    private String productImage;

    @ApiModelProperty(value = "可读说明，可直接展示给用户")
    private String message;

    @ApiModelProperty(value = "购物车中购买数量")
    private Integer cartQuantity;

    @ApiModelProperty(value = "当前可售库存（可用=stock-lockStock），仅库存不足时有值")
    private Integer availableQuantity;

    @ApiModelProperty(value = "每人限购数量，仅超限购时有值")
    private Integer limitPerUser;

    @ApiModelProperty(value = "加入购物车时的单价，仅价格变动时有值")
    private BigDecimal previousPrice;

    @ApiModelProperty(value = "当前商品单价，仅价格变动时有值")
    private BigDecimal currentPrice;
}
