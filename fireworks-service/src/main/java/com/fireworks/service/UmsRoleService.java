package com.fireworks.service;

import com.fireworks.model.pojo.UmsRole;

import java.util.List;

/**
 * 角色业务接口。
 */
public interface UmsRoleService {

    /**
     * 获取所有角色列表（用于编辑时角色选择）。
     *
     * @return 角色列表
     */
    List<UmsRole> listRoles();
}
