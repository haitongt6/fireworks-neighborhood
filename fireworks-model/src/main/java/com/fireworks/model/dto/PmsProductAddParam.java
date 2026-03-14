package com.fireworks.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增商品参数。
 */
@Data
public class PmsProductAddParam {

    private String title;
    private String subTitle;
    private Long categoryId;
    private String images;
    private String mainVideo;
    private String detailPics;
    private BigDecimal price;
    private Integer status = 1;
    private Integer sort = 0;
}
