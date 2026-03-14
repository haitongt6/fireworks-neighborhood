package com.fireworks.model.dto;

import lombok.Data;

/**
 * 根级目录/菜单新增参数。pid 固定为 0。
 */
@Data
public class RootMenuAddParam {

    /** 0-目录，1-菜单 */
    private Integer type;
    private String name;
    /** 菜单时必填，如 /products */
    private String value;
    /** 图标名，如 LayoutDashboard */
    private String icon;
}
