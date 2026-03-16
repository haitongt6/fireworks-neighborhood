package com.fireworks.model.dto;

import lombok.Data;

/**
 * 管理员登录请求参数。
 */
@Data
public class UmsAdminLoginParam {

    /** 登录用户名 */
    private String username;

    /** 明文密码 */
    private String password;
}
