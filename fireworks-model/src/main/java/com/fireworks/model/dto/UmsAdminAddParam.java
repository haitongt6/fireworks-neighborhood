package com.fireworks.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 超级管理员添加用户请求参数。
 */
@Data
public class UmsAdminAddParam {

    /** 登录用户名，必填，全局唯一 */
    private String username;

    /** 明文密码，必填，入库前会 BCrypt 加密 */
    private String password;

    /** 显示昵称 */
    private String nickname;

    /** 联系邮箱 */
    private String email;

    /** 账号状态：1-启用，0-禁用，默认 1 */
    private Integer status = 1;

    /** 角色 ID 列表，至少选择一个 */
    private List<Long> roleIds;
}
