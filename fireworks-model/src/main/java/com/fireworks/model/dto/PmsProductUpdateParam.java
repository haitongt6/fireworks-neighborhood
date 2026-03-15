package com.fireworks.model.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 编辑商品参数。
 */
@Data
public class PmsProductUpdateParam {

    /** 商品标题，必填 */
    @NotBlank(message = "商品标题不能为空")
    private String title;

    /** 副标题/促销语，可选 */
    private String subTitle;

    /** 类目ID，必填 */
    @NotNull(message = "类目不能为空")
    private Long categoryId;

    /** 主图，多张图片URL逗号分隔，必填，至少一张 */
    @NotBlank(message = "主图至少保留一张")
    private String images;

    /** 主图视频URL，可选，传空字符串表示删除 */
    private String mainVideo;

    /** 详情图，多张图片URL逗号分隔，必填，至少一张 */
    @NotBlank(message = "详情图至少保留一张")
    private String detailPics;

    /** 展示价/起售价（元），必填，大于0，最多两位小数 */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "价格必须大于0")
    @DecimalMax(value = "99999999", message = "价格必须小于等于99999999")
    @Digits(integer = 10, fraction = 2, message = "价格最多两位小数")
    private BigDecimal price;

    /** 库存，必填，大于等于0 */
    @NotNull(message = "库存必填")
    @Min(value = 0, message = "库存必须大于等于0")
    private Integer stock;

    /** 上下架状态：1-上架，0-下架，2-待上架，必填 */
    @NotNull(message = "上下架状态不能为空")
    private Integer status;
}
