package com.fireworks.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * C 端商品详情 VO。
 */
@Data
public class ApiProductDetailVO {

    private Long id;
    private String title;
    private String subTitle;
    private Long categoryId;
    private String categoryName;
    private String images;
    private String mainVideo;
    private String detailPics;
    private BigDecimal price;
    private Integer stock;
    /** 销量（已售件数） */
    private Integer sale;
    private Integer status;
}
