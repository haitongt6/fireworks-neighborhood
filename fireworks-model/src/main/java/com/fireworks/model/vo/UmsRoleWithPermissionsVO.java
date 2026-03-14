package com.fireworks.model.vo;

import com.fireworks.model.pojo.UmsPermission;
import lombok.Data;

import java.util.List;

/**
 * 角色及其关联的权限列表。
 */
@Data
public class UmsRoleWithPermissionsVO {

    private Long id;
    private String name;
    private String description;
    private Integer status;
    private List<UmsPermission> permissions;
}
