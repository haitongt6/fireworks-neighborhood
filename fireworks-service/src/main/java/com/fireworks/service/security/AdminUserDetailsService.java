package com.fireworks.service.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireworks.common.api.util.UserRoleUtil;
import com.fireworks.model.pojo.UmsAdmin;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.pojo.UmsRole;
import com.fireworks.service.mapper.UmsAdminMapper;
import com.fireworks.service.mapper.UmsPermissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security {@link UserDetailsService} 的管理员实现。
 * <p>
 * 根据用户名从数据库加载管理员信息及其权限，
 * 供 Spring Security 在以下场景调用：
 * <ul>
 *   <li>登录时通过 {@code AuthenticationManager} 触发</li>
 *   <li>JWT 过滤器每次请求时重新加载用户信息</li>
 * </ul>
 * </p>
 */
@Service
public class AdminUserDetailsService implements UserDetailsService {



    private static final Logger log = LoggerFactory.getLogger(AdminUserDetailsService.class);

    private final UmsAdminMapper umsAdminMapper;
    private final UmsPermissionMapper umsPermissionMapper;

    public AdminUserDetailsService(UmsAdminMapper umsAdminMapper, UmsPermissionMapper umsPermissionMapper) {
        this.umsAdminMapper = umsAdminMapper;
        this.umsPermissionMapper = umsPermissionMapper;
    }

    /**
     * 根据用户名加载管理员及其所有权限。
     *
     * @param username 登录用户名
     * @return {@link com.fireworks.common.api.AdminUserDetails} 对象
     * @throws UsernameNotFoundException 当用户名不存在时抛出，统一返回"用户名或密码错误"避免信息泄露
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 精确匹配用户名，查询管理员基本信息
        UmsAdmin admin = umsAdminMapper.selectOne(
                new LambdaQueryWrapper<UmsAdmin>()
                        .eq(UmsAdmin::getUsername, username)
        );

        if (admin == null) {
            // 故意使用模糊描述，防止用户枚举攻击
            log.warn("登录失败，用户名不存在: {}", username);
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 2. 查询该管理员经角色关联获得的所有权限及角色
        List<UmsPermission> permissions = umsAdminMapper.selectPermissionByAdminId(admin.getId());
        List<UmsRole> roles = umsAdminMapper.selectRolesByAdminId(admin.getId());

        // 3. 超级管理员（启用状态）从 DB 动态加载全部权限，避免硬编码
        if (UserRoleUtil.isSuperAdminEnabled(roles)) {
            permissions = umsPermissionMapper.selectList(
                    new LambdaQueryWrapper<UmsPermission>().orderByAsc(UmsPermission::getId));
            log.debug("超级管理员 [{}] 使用全部权限，权限数: {}", username, permissions.size());
        } else {
            log.debug("加载管理员 [{}] 成功，权限数: {}, 角色数: {}", username, permissions.size(), roles != null ? roles.size() : 0);
        }

        return new com.fireworks.common.api.AdminUserDetails(admin, permissions, roles);
    }


}
