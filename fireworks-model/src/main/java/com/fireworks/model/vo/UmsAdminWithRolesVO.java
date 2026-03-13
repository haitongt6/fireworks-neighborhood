package com.fireworks.model.vo;

import com.fireworks.model.pojo.UmsRole;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 管理员列表项，含关联角色。
 */
@Data
public class UmsAdminWithRolesVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private Integer status;
    private Date createTime;
    /** 该管理员的角色列表 */
    private List<UmsRole> roles;
}
