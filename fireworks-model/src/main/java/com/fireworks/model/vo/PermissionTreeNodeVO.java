package com.fireworks.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限树节点，供角色新增/编辑页面的级联权限选择器使用。
 * <p>
 * type=2（按钮）为叶子节点，可勾选；type=0（目录）、type=1（菜单）为分组节点，支持全选/全不选、展开折叠。
 * </p>
 */
@Data
public class PermissionTreeNodeVO {

    /** 权限 ID */
    private Long id;
    /** 权限名称（展示用） */
    private String name;
    /** 权限标识，如 ums:role:list，叶子节点有效 */
    private String value;
    /** 节点类型：0-目录，1-菜单，2-按钮 */
    private Integer type;
    /** 是否叶子节点（type=2），叶子节点为可勾选项 */
    private Boolean leaf;
    /** 子节点 */
    private List<PermissionTreeNodeVO> children = new ArrayList<>();
}
