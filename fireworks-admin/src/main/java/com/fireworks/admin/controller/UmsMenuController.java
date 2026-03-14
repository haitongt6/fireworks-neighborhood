package com.fireworks.admin.controller;

import com.fireworks.common.api.AdminUserDetails;
import com.fireworks.common.api.Result;
import com.fireworks.model.constant.PermissionConstant;
import com.fireworks.model.dto.ButtonAddParam;
import com.fireworks.model.dto.ButtonUpdateParam;
import com.fireworks.model.dto.RootMenuAddParam;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.vo.MenuManageItemVO;
import com.fireworks.model.vo.MenuTreeNodeVO;
import com.fireworks.service.UmsMenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 后台菜单接口。
 * <p>
 * 根据当前登录管理员及其角色权限，返回可访问的菜单树。
 * </p>
 */
@RestController
@RequestMapping("/admin")
public class UmsMenuController {

    private final UmsMenuService umsMenuService;

    public UmsMenuController(UmsMenuService umsMenuService) {
        this.umsMenuService = umsMenuService;
    }

    /**
     * 获取当前管理员的菜单树。
     *
     * @return 菜单树根节点列表
     */
    @GetMapping("/menu/tree")
    public Result<List<MenuTreeNodeVO>> getMenuTree() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof AdminUserDetails)) {
            return Result.success(Collections.emptyList());
        }
        AdminUserDetails details = (AdminUserDetails) principal;
        Long adminId = details.getUmsAdmin().getId();
        List<MenuTreeNodeVO> tree = umsMenuService.getMenuTree(adminId);
        return Result.success(tree);
    }

    /**
     * 菜单管理：获取全部目录、菜单、按钮的级联列表。
     */
    @GetMapping("/menu/listAll")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).MENU_LIST)")
    public Result<List<MenuManageItemVO>> listAllForManage() {
        return Result.success(umsMenuService.listAllForManage());
    }

    /**
     * 新增根级目录或菜单。
     */
    @PostMapping("/menu/root")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).BUTTON_ADD)")
    public Result<UmsPermission> addRootMenu(@RequestBody RootMenuAddParam param) {
        return Result.success(umsMenuService.addRootMenu(param));
    }

    /**
     * 在指定菜单下新增按钮权限。
     */
    @PostMapping("/menu/button")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).BUTTON_ADD)")
    public Result<UmsPermission> addButton(@RequestBody ButtonAddParam param) {
        return Result.success(umsMenuService.addButton(param));
    }

    /**
     * 修改按钮权限。
     */
    @PutMapping("/menu/button/{buttonId}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).BUTTON_EDIT)")
    public Result<Void> updateButton(@PathVariable Long buttonId, @RequestBody ButtonUpdateParam param) {
        umsMenuService.updateButton(buttonId, param);
        return Result.success();
    }

    /**
     * 删除按钮权限。
     */
    @DeleteMapping("/menu/button/{buttonId}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).BUTTON_DELETE)")
    public Result<Void> deleteButton(@PathVariable Long buttonId) {
        umsMenuService.deleteButton(buttonId);
        return Result.success();
    }

    /**
     * 删除目录或菜单（含其下所有子节点）。
     */
    @DeleteMapping("/menu/directory/{id}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).BUTTON_DELETE)")
    public Result<Void> deleteDirectoryOrMenu(@PathVariable Long id) {
        umsMenuService.deleteDirectoryOrMenu(id);
        return Result.success();
    }
}
