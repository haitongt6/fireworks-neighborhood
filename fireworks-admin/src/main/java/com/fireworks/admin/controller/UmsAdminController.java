package com.fireworks.admin.controller;

import com.fireworks.admin.dto.UmsAdminLoginParam;
import com.fireworks.model.dto.UmsAdminAddParam;
import com.fireworks.common.api.Result;
import com.fireworks.model.pojo.UmsAdmin;
import com.fireworks.service.UmsAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理员认证接口。
 *
 * <p>所有接口均返回 {@link Result} 统一响应体，前端根据 {@code code} 字段判断请求结果。</p>
 * <p>异常由 {@link com.fireworks.admin.handler.GlobalExceptionHandler} 统一拦截处理。</p>
 */
@RestController
@RequestMapping("/admin")
public class UmsAdminController {

    private final UmsAdminService umsAdminService;

    public UmsAdminController(UmsAdminService umsAdminService) {
        this.umsAdminService = umsAdminService;
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
    public Result<Map<String, String>> login(@RequestBody UmsAdminLoginParam loginParam) {
        String token = umsAdminService.login(loginParam.getUsername(), loginParam.getPassword());

        Map<String, String> tokenMap = new HashMap<String, String>(2);
        tokenMap.put("tokenHead", "Bearer ");
        tokenMap.put("token", token);

        return Result.success(tokenMap);
    }

    @GetMapping("demo")
    public Result<String> demo() {
        return Result.success("恭喜你校验成功了");
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
    @PreAuthorize("hasAuthority('ums:admin:add')")
    public Result<UmsAdmin> addUser(@RequestBody UmsAdminAddParam param) {
        UmsAdmin admin = umsAdminService.addAdmin(
                param.getUsername(),
                param.getPassword(),
                param.getNickname(),
                param.getEmail(),
                param.getStatus(),
                param.getRoleIds()
        );
        return Result.success(admin);
    }

}
