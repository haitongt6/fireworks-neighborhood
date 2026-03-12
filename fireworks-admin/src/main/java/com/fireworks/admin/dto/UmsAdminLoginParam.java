package com.fireworks.admin.dto;

/**
 * 管理员登录请求参数。
 */
public class UmsAdminLoginParam {

    /** 登录用户名 */
    private String username;

    /** 明文密码 */
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
