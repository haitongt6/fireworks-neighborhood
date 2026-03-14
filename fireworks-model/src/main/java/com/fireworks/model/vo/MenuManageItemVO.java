package com.fireworks.model.vo;

import lombok.Data;

/**
 * 菜单管理列表项。用于级联展示目录、菜单、按钮，level 供前端缩进。
 */
@Data
public class MenuManageItemVO {

    private Long id;
    private Long pid;
    private String name;
    private String value;
    private String icon;
    /** 0-目录，1-菜单，2-按钮 */
    private Integer type;
    /** 层级深度，根为 0，用于列表缩进 */
    private Integer level;
}
