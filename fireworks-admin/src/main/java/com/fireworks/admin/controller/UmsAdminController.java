package com.fireworks.admin.controller;

import com.fireworks.common.util.RsaUtil;
import com.fireworks.admin.dto.UmsAdminLoginParam;
import com.fireworks.service.config.RsaKeyHolder;
import com.fireworks.model.constant.PermissionConstant;
import com.fireworks.model.dto.ProfileUpdateParam;
import com.fireworks.model.dto.UmsAdminAddParam;
import com.fireworks.model.dto.UmsAdminUpdateParam;
import com.fireworks.common.api.Result;
import com.fireworks.model.pojo.UmsAdmin;
import com.fireworks.model.pojo.UmsRole;
import com.fireworks.model.vo.UmsAdminWithRolesVO;
import com.fireworks.service.UmsAdminService;
import com.fireworks.service.UmsRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fireworks.common.api.AdminUserDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台管理员认证接口。
 *
 * <p>所有接口均返回 {@link Result} 统一响应体，前端根据 {@code code} 字段判断请求结果。</p>
 * <p>异常由 {@link com.fireworks.service.handler.GlobalExceptionHandler} 统一拦截处理。</p>
 */
@RestController
@RequestMapping("/admin")
public class UmsAdminController {

    private final UmsAdminService umsAdminService;
    private final UmsRoleService umsRoleService;
    private final RsaKeyHolder rsaKeyHolder;

    public UmsAdminController(UmsAdminService umsAdminService, UmsRoleService umsRoleService,
                             RsaKeyHolder rsaKeyHolder) {
        this.umsAdminService = umsAdminService;
        this.umsRoleService = umsRoleService;
        this.rsaKeyHolder = rsaKeyHolder;
    }

    /**
     * 获取 RSA 公钥（供前端加密密码，登录前调用，无需认证）。
     */
    @GetMapping("/publicKey")
    public Result<Map<String, String>> getPublicKey() {
        Map<String, String> data = new HashMap<>(1);
        data.put("publicKey", RsaUtil.getPublicKeyBase64(rsaKeyHolder.getPublicKey()));
        return Result.success(data);
    }

    /**
     * 管理员登录接口。
     *
     * <p>
     * 接收 JSON 格式的用户名和密码，使用 BCrypt 验证后生成 JWT Token 返回。
     * 前端后续请求需在请求头中携带 {@code Authorization: Bearer <token>}。
     * </p>
     *
     * <h3>请求示例</h3>
     * <pre>
     * POST /admin/login
     * Content-Type: application/json
     *
     * {
     *   "username": "admin",
     *   "password": "123456"
     * }
     * </pre>
     *
     * <h3>成功响应示例</h3>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "tokenHead": "Bearer ",
     *     "token": "eyJhbGciOiJIUzUxMiJ9..."
     *   }
     * }
     * </pre>
     *
     * @param loginParam 包含用户名和密码的登录参数
     * @return 包含 {@code tokenHead} 和 {@code token} 的统一响应；认证失败时返回错误描述
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UmsAdminLoginParam loginParam) {
        String password = RsaUtil.decrypt(loginParam.getPassword(), rsaKeyHolder.getPrivateKey());
        String token = umsAdminService.login(loginParam.getUsername(), password);
        UmsAdmin admin = umsAdminService.getAdminByUsername(loginParam.getUsername());

        Map<String, Object> data = new HashMap<>(5);
        data.put("tokenHead", "Bearer ");
        data.put("token", token);
        data.put("username", admin != null ? admin.getUsername() : loginParam.getUsername());
        data.put("nickname", admin != null ? admin.getNickname() : null);
        data.put("email", admin != null ? admin.getEmail() : null);

        return Result.success(data);
    }

    /**
     * 登出。需携带有效 Token，清除 Redis 中的会话，使 Token 立即失效。
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AdminUserDetails) {
            String username = ((AdminUserDetails) auth.getPrincipal()).getUmsAdmin().getUsername();
            umsAdminService.logout(username);
        }
        return Result.success();
    }


    /**
     * 获取当前登录用户的个人信息（供左下角个人信息弹窗，从 DB 读取最新数据）。
     */
    @GetMapping("/user/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> data = new HashMap<>(4);
        if (auth != null && auth.getPrincipal() instanceof AdminUserDetails) {
            Long adminId = ((AdminUserDetails) auth.getPrincipal()).getUmsAdmin().getId();
            UmsAdmin admin = umsAdminService.getAdminById(adminId);
            if (admin != null) {
                data.put("username", admin.getUsername());
                data.put("nickname", admin.getNickname());
                data.put("email", admin.getEmail());
            }
        }
        return Result.success(data);
    }

    /**
     * 更新当前登录用户的个人信息（昵称、邮箱、密码）。
     * <p>密码修改后会清除会话，需重新登录。</p>
     */
    @PutMapping("/user/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updateCurrentUserProfile(@RequestBody ProfileUpdateParam param) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AdminUserDetails)) {
            throw new IllegalArgumentException("未登录");
        }
        if (param.getPassword() != null && !param.getPassword().trim().isEmpty()) {
            param.setPassword(RsaUtil.decrypt(param.getPassword(), rsaKeyHolder.getPrivateKey()));
        }
        Long adminId = ((AdminUserDetails) auth.getPrincipal()).getUmsAdmin().getId();
        umsAdminService.updateProfile(adminId, param);
        return Result.success();
    }

    /**
     * 获取当前登录用户的权限标识列表（供前端按钮显隐控制）。
     * <p>仅返回 type=2 的按钮权限 value，如 ums:admin:add。</p>
     */
    @GetMapping("/user/permissions")
    @PreAuthorize("isAuthenticated()")
    public Result<List<String>> getCurrentUserPermissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<String> perms = new ArrayList<>();
        if (auth != null && auth.getAuthorities() != null) {
            perms = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
        return Result.success(perms);
    }

    /**
     * 超级管理员添加用户。
     * <p>
     * 需拥有 {@code ums:admin:add} 权限。请求头需携带 {@code Authorization: Bearer <token>}。
     * </p>
     *
     * @param param 添加用户参数（username、password 必填，roleIds 至少一个）
     * @return 新创建的管理员信息（不含密码）
     */
    @PostMapping("/user")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ADMIN_ADD)")
    public Result<UmsAdmin> addUser(@RequestBody UmsAdminAddParam param) {
        String password = RsaUtil.decrypt(param.getPassword(), rsaKeyHolder.getPrivateKey());
        UmsAdmin admin = umsAdminService.addAdmin(
                param.getUsername(),
                password,
                param.getNickname(),
                param.getEmail(),
                param.getStatus(),
                param.getRoleIds()
        );
        return Result.success(admin);
    }

    /**
     * 获取所有管理员列表（含角色）。
     * <p>需拥有 {@code ums:admin:list} 权限。</p>
     *
     * @return 管理员列表，每条包含角色信息
     */
    @GetMapping("/user/list")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ADMIN_LIST)")
    public Result<List<UmsAdminWithRolesVO>> listUsers() {
        List<UmsAdminWithRolesVO> list = umsAdminService.listAdmins();
        return Result.success(list);
    }

    /**
     * 更新管理员（角色、昵称、邮箱、状态、密码）。
     * <p>需拥有 {@code ums:admin:edit} 权限。</p>
     * <p>超级管理员不能修改自己的信息（防止误操作），但可以修改他人（如禁用账号）。</p>
     * <p>密码修改仅允许：当前用户修改自己的密码，或超级管理员修改任意用户密码。</p>
     *
     * @param adminId 管理员 ID
     * @param param   更新参数
     * @return 操作成功
     */
    @PutMapping("/user/{adminId}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ADMIN_EDIT)")
    public Result<Void> updateUser(@PathVariable Long adminId,
                                   @RequestBody UmsAdminUpdateParam param) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AdminUserDetails) {
            AdminUserDetails current = (AdminUserDetails) auth.getPrincipal();
            Long currentId = current.getUmsAdmin().getId();
            boolean isSuperAdmin = current.getRoleList() != null
                    && current.getRoleList().stream()
                    .anyMatch(r -> r != null && "SUPER_ADMIN".equals(r.getName()));
            boolean isSelf = adminId != null && adminId.equals(currentId);
            if (isSelf && isSuperAdmin) {
                throw new IllegalArgumentException("超级管理员不能修改自己的信息，防止误操作");
            }
            if (param.getPassword() != null && !param.getPassword().trim().isEmpty()) {
                if (!isSelf && !isSuperAdmin) {
                    throw new IllegalArgumentException("仅可修改自己的密码，或由超级管理员修改");
                }
                param.setPassword(RsaUtil.decrypt(param.getPassword(), rsaKeyHolder.getPrivateKey()));
            }
        }
        umsAdminService.updateAdmin(adminId, param);
        return Result.success();
    }

    /**
     * 获取所有角色列表。
     * <p>供编辑表单中的角色选择器使用。需拥有 {@code ums:admin:list} 权限。</p>
     *
     * @return 角色列表
     */
    @GetMapping("/role/list")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ADMIN_LIST)")
    public Result<List<UmsRole>> listRoles() {
        return Result.success(umsRoleService.listRoles());
    }

    /**
     * 删除管理员。超级管理员不允许删除。
     *
     * @param adminId 管理员 ID
     */
    @DeleteMapping("/user/{adminId}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ADMIN_DELETE)")
    public Result<Void> deleteUser(@PathVariable Long adminId) {
        umsAdminService.deleteAdmin(adminId);
        return Result.success();
    }

    @GetMapping("test")
    public Result<Void> test() {
        return Result.success();
    }
}
