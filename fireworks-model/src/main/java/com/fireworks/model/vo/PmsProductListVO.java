package com.fireworks.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品列表展示 VO，含类目名称。
 */
@Data
public class PmsProductListVO {

    private Long id;
    /** 商品标题 */
    private String title;
    /** 副标题 */
    private String subTitle;
    /** 类目ID */
    private Long categoryId;
    /** 类目名称 */
    private String categoryName;
    /** 主图（多张URL，前端取第一张或解析） */
    private String images;
    /** 展示价 */
    private BigDecimal price;
    /** 总库存 */
    private Integer stock;
    /** 销量（已售件数） */
    private Integer sale;
    /** 状态：1-上架，0-下架，2-待上架 */
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
