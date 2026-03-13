package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireworks.model.dto.UmsAdminUpdateParam;
import com.fireworks.model.pojo.UmsAdmin;
import com.fireworks.model.vo.UmsAdminWithRolesVO;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public UmsAdmin addAdmin(String username, String password, String nickname,
                             String email, Integer status, List<Long> roleIds) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一个角色");
        }

        UmsAdmin exist = getAdminByUsername(username);
        if (exist != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        UmsAdmin admin = new UmsAdmin();
        admin.setUsername(username.trim());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setNickname(nickname != null ? nickname.trim() : null);
        admin.setEmail(email != null ? email.trim() : null);
        admin.setStatus(status != null ? status : 1);
        admin.setCreateTime(new Date());

        umsAdminMapper.insert(admin);
        Long adminId = admin.getId();

        for (Long roleId : roleIds) {
            if (roleId != null) {
                umsAdminMapper.insertAdminRoleRelation(adminId, roleId);
            }
        }

        admin.setPassword(null);
        log.info("超级管理员添加用户成功: username={}", username);
        return admin;
    }

    @Override
    public List<UmsAdminWithRolesVO> listAdmins() {
        List<UmsAdmin> admins = umsAdminMapper.selectList(null);
        List<UmsAdminWithRolesVO> result = new ArrayList<>(admins.size());
        for (UmsAdmin admin : admins) {
            UmsAdminWithRolesVO vo = new UmsAdminWithRolesVO();
            vo.setId(admin.getId());
            vo.setUsername(admin.getUsername());
            vo.setNickname(admin.getNickname());
            vo.setEmail(admin.getEmail());
            vo.setStatus(admin.getStatus());
            vo.setCreateTime(admin.getCreateTime());
            vo.setRoles(umsAdminMapper.selectRolesByAdminId(admin.getId()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public void updateAdmin(Long adminId, UmsAdminUpdateParam param) {
        if (adminId == null) {
            throw new IllegalArgumentException("管理员 ID 不能为空");
        }
        if (param == null) {
            throw new IllegalArgumentException("更新参数不能为空");
        }
        if (param.getRoleIds() == null || param.getRoleIds().isEmpty()) {
            throw new IllegalArgumentException("请至少选择一个角色");
        }

        UmsAdmin admin = umsAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new IllegalArgumentException("管理员不存在");
        }

        if (param.getNickname() != null) {
            admin.setNickname(param.getNickname().trim());
        }
        if (param.getEmail() != null) {
            admin.setEmail(param.getEmail().trim());
        }
        if (param.getStatus() != null) {
            admin.setStatus(param.getStatus());
        }
        umsAdminMapper.updateById(admin);

        umsAdminMapper.deleteAdminRoleRelationByAdminId(adminId);
        for (Long roleId : param.getRoleIds()) {
            if (roleId != null) {
                umsAdminMapper.insertAdminRoleRelation(adminId, roleId);
            }
        }
        log.info("更新管理员成功: adminId={}", adminId);
    }
}
