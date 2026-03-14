package com.fireworks.admin.controller;

import com.fireworks.common.api.AdminUserDetails;
import com.fireworks.common.api.Result;
import com.fireworks.model.vo.MenuTreeNodeVO;
import com.fireworks.service.UmsMenuService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * <p>
     * 需携带有效 JWT Token。返回的菜单仅包含该管理员角色下可见的目录与菜单。
     * </p>
     *
     * @return 菜单树根节点列表
     */
    @GetMapping("/menu/tree")
    public Result<List<MenuTreeNodeVO>> getMenuTree() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof AdminUserDetails)) {
            return Result.success(List.of());
        }
        AdminUserDetails details = (AdminUserDetails) principal;
        Long adminId = details.getUmsAdmin().getId();
        List<MenuTreeNodeVO> tree = umsMenuService.getMenuTree(adminId);
        return Result.success(tree);
    }
}
