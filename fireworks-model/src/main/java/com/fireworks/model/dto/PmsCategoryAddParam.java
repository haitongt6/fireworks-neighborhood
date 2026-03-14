package com.fireworks.model.dto;

import lombok.Data;

/**
 * 新增商品类目参数。
 */
@Data
public class PmsCategoryAddParam {

    private String name;
    private Integer sort = 0;
    private Integer status = 1;
}
