package com.fireworks.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 商品类目实体，对应 {@code pms_product_category}。
 * 一级扁平结构，无父子层级。
 */
@Data
@TableName("pms_product_category")
public class PmsProductCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类目名称 */
    private String name;

    /** 排序，升序 */
    private Integer sort;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    private Date createTime;
}
