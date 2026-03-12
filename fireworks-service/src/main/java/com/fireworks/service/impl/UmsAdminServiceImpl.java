package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireworks.model.UmsAdmin;
import com.fireworks.model.constant.RedisKeyConstant;
import com.fireworks.service.UmsAdminService;
import com.fireworks.service.mapper.UmsAdminMapper;
import com.fireworks.service.security.AdminUserDetailsService;
import com.fireworks.service.utils.JwtTokenUtil;
import com.fireworks.service.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * {@link UmsAdminService} 的实现类。
 * <p>
 * 使用构造器注入，所有依赖均为必填项，便于单元测试 mock。
 * {@link PasswordEncoder} Bean 由 {@code fireworks-admin} 模块的
 * {@code SecurityConfig} 提供（{@code BCryptPasswordEncoder}），
 * 运行时同属同一 Spring 应用上下文，可正常注入。
 * </p>
 */
@Service
public class UmsAdminServiceImpl implements UmsAdminService {

    private static final Logger log = LoggerFactory.getLogger(UmsAdminServiceImpl.class);

    private final UmsAdminMapper umsAdminMapper;
    private final AdminUserDetailsService adminUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    public UmsAdminServiceImpl(UmsAdminMapper umsAdminMapper,
                               AdminUserDetailsService adminUserDetailsService,
                               JwtTokenUtil jwtTokenUtil,
                               PasswordEncoder passwordEncoder) {
        this.umsAdminMapper = umsAdminMapper;
        this.adminUserDetailsService = adminUserDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * {@inheritDoc}
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>通过 {@link AdminUserDetailsService#loadUserByUsername} 加载用户（内部判断用户名是否存在）</li>
     *   <li>校验账号启用状态</li>
     *   <li>使用 {@link PasswordEncoder#matches} 比对密码</li>
     *   <li>生成 JWT Token 并返回</li>
     * </ol>
     * </p>
     */
    @Override
    public String login(String username, String password) {
        // 1. 加载用户信息（用户名不存在时会抛出 UsernameNotFoundException）
        UserDetails userDetails = adminUserDetailsService.loadUserByUsername(username);

        // 2. 校验账号是否可用
        if (!userDetails.isEnabled()) {
            throw new DisabledException("账号已被禁用，请联系管理员");
        }

        // 3. BCrypt 比对密码
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 4. 生成并返回 JWT Token
        String token = jwtTokenUtil.generateToken(userDetails);
        // 5. 将用户信息存入redis
        RedisUtil.set(RedisKeyConstant.USER_INFO_KEY+userDetails.getUsername(),userDetails,30,TimeUnit.MINUTES);
        log.info("管理员 [{}] 登录成功", username);
        return token;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UmsAdmin getAdminByUsername(String username) {
        return umsAdminMapper.selectOne(
                new LambdaQueryWrapper<UmsAdmin>()
                        .eq(UmsAdmin::getUsername, username)
        );
    }
}
