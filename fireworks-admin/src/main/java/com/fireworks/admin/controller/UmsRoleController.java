package com.fireworks.admin.controller;

import com.fireworks.common.api.AdminUserDetails;
import com.fireworks.common.api.Result;
import com.fireworks.model.constant.PermissionConstant;
import com.fireworks.model.dto.UmsRoleAddParam;
import com.fireworks.model.dto.UmsRoleUpdateParam;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.pojo.UmsRole;
import com.fireworks.model.vo.PermissionTreeNodeVO;
import com.fireworks.model.vo.UmsRoleWithPermissionsVO;
import com.fireworks.service.UmsRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口。
 */
@RestController
@RequestMapping("/admin")
public class UmsRoleController {

    private final UmsRoleService umsRoleService;

    public UmsRoleController(UmsRoleService umsRoleService) {
        this.umsRoleService = umsRoleService;
    }

    @GetMapping("/role/listWithPermissions")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ROLE_LIST) or hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ADMIN_LIST)")
    public Result<List<UmsRoleWithPermissionsVO>> listRolesWithPermissions() {
        Long adminId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AdminUserDetails) {
            adminId = ((AdminUserDetails) auth.getPrincipal()).getUmsAdmin().getId();
        }
        return Result.success(umsRoleService.listRolesWithPermissions(adminId));
    }


    @GetMapping("/permission/list")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ROLE_LIST) or hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ROLE_ADD) or hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ROLE_EDIT) or hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ADMIN_LIST)")
    public Result<List<UmsPermission>> listAllPermissions() {
        return Result.success(umsRoleService.listAllPermissions());
    }

    /**
     * 获取权限树（级联结构），供角色新增/编辑页面的权限选择器使用。
     * 目录 -> 菜单 -> 按钮，仅叶子节点（type=2）可勾选，支持全选/全不选、展开折叠。
     */
    @GetMapping("/permission/tree")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ROLE_LIST) or hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ROLE_ADD) or hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ROLE_EDIT) or hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ADMIN_LIST)")
    public Result<List<PermissionTreeNodeVO>> listPermissionTree() {
        return Result.success(umsRoleService.listPermissionTree());
    }

    @PostMapping("/role")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ROLE_ADD)")
    public Result<UmsRole> addRole(@RequestBody UmsRoleAddParam param) {
        return Result.success(umsRoleService.addRole(param));
    }

    @PutMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ROLE_EDIT)")
    public Result<Void> updateRole(@PathVariable Long roleId, @RequestBody UmsRoleUpdateParam param) {
        umsRoleService.updateRole(roleId, param);
        return Result.success();
    }

    @DeleteMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ROLE_DELETE)")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        umsRoleService.deleteRole(roleId);
        return Result.success();
    }
}
