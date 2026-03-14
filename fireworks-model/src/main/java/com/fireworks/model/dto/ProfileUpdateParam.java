package com.fireworks.model.dto;

import lombok.Data;

/**
 * 当前用户个人信息修改参数（昵称、邮箱、密码）。
 */
@Data
public class ProfileUpdateParam {

    /** 显示昵称 */
    private String nickname;

    /** 联系邮箱 */
    private String email;

    /** 新密码，不修改请不传或传空 */
    private String password;
}
