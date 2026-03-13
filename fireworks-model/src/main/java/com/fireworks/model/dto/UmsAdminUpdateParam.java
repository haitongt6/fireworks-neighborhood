package com.fireworks.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理员编辑（角色、信息）请求参数。
 */
@Data
public class UmsAdminUpdateParam {

    /** 角色 ID 列表 */
    private List<Long> roleIds;

    /** 显示昵称 */
    private String nickname;

    /** 联系邮箱 */
    private String email;

    /** 账号状态：1-启用，0-禁用 */
    private Integer status;
}
