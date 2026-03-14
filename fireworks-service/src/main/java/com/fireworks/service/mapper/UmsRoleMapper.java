package com.fireworks.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.pojo.UmsRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper。
 */
public interface UmsRoleMapper extends BaseMapper<UmsRole> {

    List<UmsPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    int insertRolePermissionRelation(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    int deleteRolePermissionRelationByRoleId(@Param("roleId") Long roleId);

    int deleteRolePermissionRelationByPermissionId(@Param("permissionId") Long permissionId);

    int deleteAdminRoleRelationByRoleId(@Param("roleId") Long roleId);
}
