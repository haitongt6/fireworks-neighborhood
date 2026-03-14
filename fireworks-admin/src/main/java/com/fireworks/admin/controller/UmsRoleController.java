package com.fireworks.admin.controller;

import com.fireworks.common.api.Result;
import com.fireworks.model.dto.UmsRoleAddParam;
import com.fireworks.model.dto.UmsRoleUpdateParam;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.pojo.UmsRole;
import com.fireworks.model.vo.UmsRoleWithPermissionsVO;
import com.fireworks.service.UmsRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/role/list")
    @PreAuthorize("hasAuthority('ums:admin:list')")
    public Result<List<UmsRole>> listRoles() {
        return Result.success(umsRoleService.listRoles());
    }

    @GetMapping("/role/listWithPermissions")
    @PreAuthorize("hasAuthority('ums:role:list') or hasAuthority('ums:admin:list')")
    public Result<List<UmsRoleWithPermissionsVO>> listRolesWithPermissions() {
        return Result.success(umsRoleService.listRolesWithPermissions());
    }

    @GetMapping("/permission/list")
    @PreAuthorize("hasAuthority('ums:role:list') or hasAuthority('ums:role:add') or hasAuthority('ums:role:edit') or hasAuthority('ums:admin:list')")
    public Result<List<UmsPermission>> listAllPermissions() {
        return Result.success(umsRoleService.listAllPermissions());
    }

    @PostMapping("/role")
    @PreAuthorize("hasAuthority('ums:role:add')")
    public Result<UmsRole> addRole(@RequestBody UmsRoleAddParam param) {
        return Result.success(umsRoleService.addRole(param));
    }

    @PutMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('ums:role:edit')")
    public Result<Void> updateRole(@PathVariable Long roleId, @RequestBody UmsRoleUpdateParam param) {
        umsRoleService.updateRole(roleId, param);
        return Result.success();
    }

    @DeleteMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('ums:role:delete')")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        umsRoleService.deleteRole(roleId);
        return Result.success();
    }
}
