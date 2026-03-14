package com.fireworks.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点，供前端侧边栏渲染。
 */
@Data
public class MenuTreeNodeVO {

    private Long id;
    private String name;
    private String path;
    private String icon;
    private List<MenuTreeNodeVO> children = new ArrayList<>();
}
