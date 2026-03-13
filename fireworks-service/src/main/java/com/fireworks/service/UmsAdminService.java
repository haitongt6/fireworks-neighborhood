package com.fireworks.service;

import com.fireworks.model.dto.UmsAdminUpdateParam;
import com.fireworks.model.pojo.UmsAdmin;
import com.fireworks.model.vo.UmsAdminWithRolesVO;

import java.util.List;

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

    /**
     * 超级管理员添加用户。
     * <p>
     * 密码使用 BCrypt 加密后入库，同时建立管理员与角色的关联关系。
     * </p>
     *
     * @param username 登录用户名，必填
     * @param password 明文密码，入库前 BCrypt 加密
     * @param nickname 显示昵称，可空
     * @param email    联系邮箱，可空
     * @param status   账号状态：1-启用，0-禁用
     * @param roleIds  角色 ID 列表，至少一个
     * @return 新创建的管理员（不含密码）
     * @throws IllegalArgumentException 用户名已存在或参数不合法时抛出
     */
    UmsAdmin addAdmin(String username, String password, String nickname,
                      String email, Integer status, List<Long> roleIds);

    /**
     * 获取所有管理员列表（含角色）。
     *
     * @return 管理员列表，每条包含角色信息
     */
    List<UmsAdminWithRolesVO> listAdmins();

    /**
     * 更新管理员信息及角色。
     *
     * @param adminId 管理员 ID
     * @param param   更新参数（角色、昵称、邮箱、状态）
     * @throws IllegalArgumentException 管理员不存在或参数不合法时抛出
     */
    void updateAdmin(Long adminId, UmsAdminUpdateParam param);
}
