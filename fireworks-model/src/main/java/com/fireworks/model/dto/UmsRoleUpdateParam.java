package com.fireworks.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色更新请求参数（权限、描述、状态）。
 */
@Data
public class UmsRoleUpdateParam {

    private String description;
    private Integer status;
    private List<Long> permissionIds;
}
