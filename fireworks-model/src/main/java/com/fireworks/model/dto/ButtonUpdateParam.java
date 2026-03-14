package com.fireworks.model.dto;

import lombok.Data;

/**
 * 按钮权限修改参数。
 */
@Data
public class ButtonUpdateParam {

    private String name;
    /** 权限值，如 ums:role:list */
    private String value;
}
