package com.fireworks.model.constant;

/**
 * 权限标识常量，与 {@code ums_permission.value} 及 {@code @PreAuthorize} 保持一致。
 * <p>
 * 新增接口鉴权时：
 * <ol>
 *   <li>在此添加常量</li>
 *   <li>在 Flyway 迁移脚本 {@code db/migration/V*.sql} 中插入对应权限并授予角色</li>
 *   <li>在 Controller 使用 {@code @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ADMIN_LIST)")}</code></li>
 * </ol>
 * </p>
 */
public final class PermissionConstant {

    private PermissionConstant() {
    }

    // ---------- 管理员 ----------
    /** 添加管理员 */
    public static final String ADMIN_ADD = "ums:admin:add";
    /** 管理员列表 */
    public static final String ADMIN_LIST = "ums:admin:list";
    /** 编辑管理员 */
    public static final String ADMIN_EDIT = "ums:admin:edit";
    /** 删除管理员 */
    public static final String ADMIN_DELETE = "ums:admin:delete";

    // ---------- 角色 ----------
    /** 角色列表 */
    public static final String ROLE_LIST = "ums:role:list";
    /** 新增角色 */
    public static final String ROLE_ADD = "ums:role:add";
    /** 编辑角色 */
    public static final String ROLE_EDIT = "ums:role:edit";
    /** 删除角色 */
    public static final String ROLE_DELETE = "ums:role:delete";

    // ---------- 菜单管理 ----------
    /** 菜单列表 */
    public static final String MENU_LIST = "ums:menu:list";

    // ---------- 按钮权限（菜单下的按钮 CRUD）----------
    /** 新增按钮 */
    public static final String BUTTON_ADD = "ums:button:add";
    /** 编辑按钮 */
    public static final String BUTTON_EDIT = "ums:button:edit";
    /** 删除按钮 */
    public static final String BUTTON_DELETE = "ums:button:delete";

    // ---------- 商品 ----------
    /** 商品列表 */
    public static final String PRODUCT_LIST = "pms:product:list";
    /** 新增商品 */
    public static final String PRODUCT_ADD = "pms:product:add";
    /** 编辑商品 */
    public static final String PRODUCT_EDIT = "pms:product:edit";

    // ---------- 商品类目 ----------
    /** 类目列表 */
    public static final String CATEGORY_LIST = "pms:category:list";
    /** 新增类目 */
    public static final String CATEGORY_ADD = "pms:category:add";
    /** 编辑类目 */
    public static final String CATEGORY_EDIT = "pms:category:edit";
    /** 删除类目 */
    public static final String CATEGORY_DELETE = "pms:category:delete";
}
