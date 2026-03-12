package com.fireworks.service.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireworks.model.UmsAdmin;
import com.fireworks.model.UmsPermission;
import com.fireworks.service.mapper.UmsAdminMapper;
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

    public AdminUserDetailsService(UmsAdminMapper umsAdminMapper) {
        this.umsAdminMapper = umsAdminMapper;
    }

    /**
     * 根据用户名加载管理员及其所有权限。
     *
     * @param username 登录用户名
     * @return {@link AdminUserDetails} 对象
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

        // 2. 查询该管理员经角色关联获得的所有权限
        List<UmsPermission> permissions = umsAdminMapper.selectPermissionByAdminId(admin.getId());
        log.debug("加载管理员 [{}] 成功，拥有权限数量: {}", username, permissions.size());

        return new AdminUserDetails(admin, permissions);
    }
}
