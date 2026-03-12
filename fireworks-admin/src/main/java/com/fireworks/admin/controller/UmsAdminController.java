package com.fireworks.admin.controller;

import com.fireworks.admin.dto.UmsAdminLoginParam;
import com.fireworks.common.api.Result;
import com.fireworks.service.UmsAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理员认证接口。
 *
 * <p>所有接口均返回 {@link Result} 统一响应体，前端根据 {@code code} 字段判断请求结果。</p>
 */
@RestController
@RequestMapping("/admin")
public class UmsAdminController {

    private static final Logger log = LoggerFactory.getLogger(UmsAdminController.class);

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
        try {
            String token = umsAdminService.login(loginParam.getUsername(), loginParam.getPassword());

            Map<String, String> tokenMap = new HashMap<String, String>(2);
            tokenMap.put("tokenHead", "Bearer ");
            tokenMap.put("token", token);

            return Result.success(tokenMap);

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            // 统一返回模糊描述，防止用户枚举攻击
            log.warn("登录失败 - 用户名或密码错误，username={}", loginParam.getUsername());
            return Result.failed("用户名或密码错误");

        } catch (DisabledException e) {
            log.warn("登录失败 - 账号已被禁用，username={}", loginParam.getUsername());
            return Result.failed(e.getMessage());

        } catch (Exception e) {
            log.error("登录时发生未知异常，username={}", loginParam.getUsername(), e);
            return Result.failed("系统繁忙，请稍后重试");
        }
    }

    @GetMapping("demo")
    public Result<String> demo() {
        return Result.success("恭喜你校验成功了");
    }

}
