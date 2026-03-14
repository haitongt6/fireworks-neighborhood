package com.fireworks.model.dto;

import lombok.Data;

/**
 * 更新商品类目参数。
 */
@Data
public class PmsCategoryUpdateParam {

    private String name;
    private Integer sort;
    private Integer status;
}
