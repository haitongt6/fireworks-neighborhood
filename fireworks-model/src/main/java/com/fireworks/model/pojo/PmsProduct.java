package com.fireworks.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品详情实体，对应 {@code pms_product}。
 */
@Data
@TableName("pms_product")
public class PmsProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品标题 */
    private String title;

    /** 副标题/促销语 */
    private String subTitle;

    /** 类目ID */
    private Long categoryId;

    /** 主图，多张图片URL存JSON数组或逗号分隔 */
    private String images;

    /** 主图视频URL */
    private String mainVideo;

    /** 详情图，多张图片URL存JSON数组或逗号分隔 */
    private String detailPics;

    /** 展示价/起售价，单位元 */
    private BigDecimal price;

    /** 状态：1-上架，0-下架，2-待上架 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    private Date createTime;
    private Date updateTime;
}
