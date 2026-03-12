package com.fireworks.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 后台权限实体，对应数据库表 {@code ums_permission}。
 * <p>
 * 权限以树形结构组织（通过 {@code pid} 关联父节点），叶子节点的 {@code value}
 * 字段（如 {@code pms:product:read}）会被转换为 Spring Security 的
 * {@link org.springframework.security.core.authority.SimpleGrantedAuthority}。
 * </p>
 */
@Data
@TableName("ums_permission")
public class UmsPermission {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父权限 ID，0 表示根节点 */
    private Long pid;

    /** 权限名称（界面展示用） */
    private String name;

    /** 权限标识符，如 {@code pms:product:read}，用于 @PreAuthorize 校验 */
    private String value;

    /** 菜单图标 */
    private String icon;

    /** 节点类型：0-目录，1-菜单，2-按钮 */
    private Integer type;
}
