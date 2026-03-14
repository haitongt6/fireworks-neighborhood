package com.fireworks.model.dto;

import lombok.Data;

/**
 * 按钮权限新增参数。
 */
@Data
public class ButtonAddParam {

    /** 所属菜单 ID（type=1 的 ums_permission.id） */
    private Long menuId;
    private String name;
    /** 权限值，如 ums:role:list */
    private String value;
}
