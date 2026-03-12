package com.fireworks.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 后台角色实体，对应数据库表 {@code ums_role}。
 * <p>
 * 角色是权限的集合，管理员通过关联表 {@code ums_admin_role_relation} 与角色关联。
 * </p>
 */
@Data
@TableName("ums_role")
public class UmsRole {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名称，如 SUPER_ADMIN */
    private String name;

    /** 角色描述 */
    private String description;

    /** 角色状态：1-启用，0-禁用 */
    private Integer status;
}
