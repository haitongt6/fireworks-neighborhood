package com.fireworks.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表查询参数。
 */
@Data
public class PmsProductQueryParam {

    /** 商品ID，精准匹配 */
    private Long productId;

    /** 关键词：商品标题 */
    private String keyword;

    /** 类目ID，为空表示所有分类 */
    private Long categoryId;

    /** 状态：1-上架，0-下架，2-待上架，为空表示全部 */
    private Integer status;

    /** 价格下限，单位元 */
    private BigDecimal priceMin;

    /** 价格上限，单位元 */
    private BigDecimal priceMax;

    /** 创建时间起（含），格式 yyyy-MM-dd */
    private String createTimeStart;

    /** 创建时间止（含），格式 yyyy-MM-dd */
    private String createTimeEnd;

    /** 页码，从 1 开始 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
