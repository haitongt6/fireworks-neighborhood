package com.fireworks.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fireworks.model.pojo.UmsAdmin;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.pojo.UmsRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 后台管理员 Mapper。
 * <p>
 * 继承 {@link BaseMapper} 获得 MyBatis-Plus 内置的单表 CRUD 能力；
 * 同时声明自定义多表联查方法，SQL 写在对应 XML 文件中。
 * </p>
 */
public interface UmsAdminMapper extends BaseMapper<UmsAdmin> {

    /**
     * 查询指定管理员拥有的全部权限（经由角色关联表三表联查）。
     * <p>
     * 关联路径：{@code ums_admin} → {@code ums_admin_role_relation}
     * → {@code ums_role} → {@code ums_role_permission_relation}
     * → {@code ums_permission}
     * </p>
     *
     * @param adminId 管理员主键 ID
     * @return 权限列表，不含重复项
     */
    List<UmsPermission> selectPermissionByAdminId(@Param("adminId") Long adminId);

    /**
     * 插入管理员-角色关联记录。
     *
     * @param adminId 管理员 ID
     * @param roleId  角色 ID
     * @return 影响行数
     */
    int insertAdminRoleRelation(@Param("adminId") Long adminId, @Param("roleId") Long roleId);

    /**
     * 查询指定管理员的角色列表。
     *
     * @param adminId 管理员 ID
     * @return 角色列表
     */
    List<UmsRole> selectRolesByAdminId(@Param("adminId") Long adminId);

    /**
     * 删除指定管理员的全部角色关联。
     *
     * @param adminId 管理员 ID
     * @return 影响行数
     */
    int deleteAdminRoleRelationByAdminId(@Param("adminId") Long adminId);
}
