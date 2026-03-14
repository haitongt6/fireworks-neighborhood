package com.fireworks.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 编辑商品参数。
 */
@Data
public class PmsProductUpdateParam {

    private String title;
    private String subTitle;
    private Long categoryId;
    private String images;
    private String mainVideo;
    private String detailPics;
    private BigDecimal price;
    private Integer status;
    private Integer sort;
}
