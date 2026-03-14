package com.fireworks.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 新增角色请求参数。
 */
@Data
public class UmsRoleAddParam {

    private String name;
    private String description;
    private Integer status = 1;
    private List<Long> permissionIds;
}
