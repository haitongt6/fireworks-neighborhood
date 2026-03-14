package com.fireworks.service;

import com.fireworks.model.dto.UmsRoleAddParam;
import com.fireworks.model.dto.UmsRoleUpdateParam;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.pojo.UmsRole;
import com.fireworks.model.vo.PermissionTreeNodeVO;
import com.fireworks.model.vo.UmsRoleWithPermissionsVO;

import java.util.List;

/**
 * 角色业务接口。
 */
public interface UmsRoleService {

    List<UmsRole> listRoles();

    /**
     * 获取角色列表（含权限）。超级管理员返回全部角色，其他用户仅返回自身拥有的角色。
     *
     * @param currentAdminId 当前登录管理员 ID，从 ums_admin_role_relation 关联查询
     */
    List<UmsRoleWithPermissionsVO> listRolesWithPermissions(Long currentAdminId);

    List<UmsPermission> listAllPermissions();

    /**
     * 获取权限树（级联结构），供角色新增/编辑页面的权限选择器使用。
     * 目录(type=0) -> 菜单(type=1) -> 按钮(type=2)，仅叶子节点可勾选。
     */
    List<PermissionTreeNodeVO> listPermissionTree();

    UmsRole addRole(UmsRoleAddParam param);

    void updateRole(Long roleId, UmsRoleUpdateParam param);

    void deleteRole(Long roleId);
}
