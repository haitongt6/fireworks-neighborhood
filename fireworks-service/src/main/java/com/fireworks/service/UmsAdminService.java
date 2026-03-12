package com.fireworks.service;

import com.fireworks.model.UmsAdmin;

/**
 * 后台管理员业务接口。
 */
public interface UmsAdminService {

    /**
     * 管理员登录。
     * <p>
     * 使用 {@code BCryptPasswordEncoder} 比对密码，通过后生成并返回 JWT Token。
     * </p>
     *
     * @param username 登录用户名
     * @param password 明文密码
     * @return JWT Token 字符串
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException
     *         用户名不存在时抛出
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         密码错误时抛出
     * @throws org.springframework.security.authentication.DisabledException
     *         账号被禁用时抛出
     */
    String login(String username, String password);

    /**
     * 根据用户名查询管理员信息。
     *
     * @param username 登录用户名
     * @return {@link UmsAdmin} 实体；用户名不存在时返回 {@code null}
     */
    UmsAdmin getAdminByUsername(String username);
}
