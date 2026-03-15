package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireworks.common.api.AdminUserDetails;
import com.fireworks.common.api.util.UserRoleUtil;
import com.fireworks.model.dto.UmsRoleAddParam;
import com.fireworks.model.dto.UmsRoleUpdateParam;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.pojo.UmsRole;
import com.fireworks.model.vo.PermissionTreeNodeVO;
import com.fireworks.model.vo.UmsRoleWithPermissionsVO;
import com.fireworks.service.UmsRoleService;
import com.fireworks.service.mapper.UmsAdminMapper;
import com.fireworks.service.mapper.UmsPermissionMapper;
import com.fireworks.service.mapper.UmsRoleMapper;
import com.fireworks.service.security.AdminUserDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link UmsRoleService} 的实现类。
 */
@Service
public class UmsRoleServiceImpl implements UmsRoleService {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    private final UmsRoleMapper umsRoleMapper;
    private final UmsPermissionMapper umsPermissionMapper;
    private final UmsAdminMapper umsAdminMapper;

    private final AdminUserDetailsService adminUserDetailsService;

    public UmsRoleServiceImpl(UmsRoleMapper umsRoleMapper,
                              UmsPermissionMapper umsPermissionMapper,
                              UmsAdminMapper umsAdminMapper, AdminUserDetailsService adminUserDetailsService) {
        this.umsRoleMapper = umsRoleMapper;
        this.umsPermissionMapper = umsPermissionMapper;
        this.umsAdminMapper = umsAdminMapper;
        this.adminUserDetailsService = adminUserDetailsService;
    }

    @Override
    public List<UmsPermission> listAllPermissions() {
        return umsPermissionMapper.selectList(new LambdaQueryWrapper<UmsPermission>().orderByAsc(UmsPermission::getId));
    }

    @Override
    public List<PermissionTreeNodeVO> listPermissionTree() {
        List<UmsPermission> all = umsPermissionMapper.selectList(
                new LambdaQueryWrapper<UmsPermission>().orderByAsc(UmsPermission::getId));
        return buildPermissionTree(all, 0L);
    }

    /**
     * 将权限 ID 列表扩展为其与所有祖先节点的并集，确保角色关联目录、菜单后侧栏可正常展示。
     */
    private Set<Long> expandWithAncestors(List<Long> permissionIds) {
        Set<Long> result = new LinkedHashSet<>();
        if (permissionIds == null) {
            return result;
        }
        for (Long id : permissionIds) {
            if (id == null) {
                continue;
            }
            result.add(id);
            Long currentPid = id;
            while (true) {
                UmsPermission p = umsPermissionMapper.selectById(currentPid);
                if (p == null || p.getPid() == null || p.getPid() == 0) {
                    break;
                }
                currentPid = p.getPid();
                result.add(currentPid);
            }
        }
        return result;
    }

    /**
     * 递归构建权限树。目录/菜单为分组节点，type=2 按钮为叶子可勾选节点。
     */
    private List<PermissionTreeNodeVO> buildPermissionTree(List<UmsPermission> list, Long pid) {
        List<PermissionTreeNodeVO> result = new ArrayList<>();
        for (UmsPermission p : list) {
            if (pid.equals(p.getPid() != null ? p.getPid() : 0L)) {
                PermissionTreeNodeVO vo = new PermissionTreeNodeVO();
                vo.setId(p.getId());
                vo.setName(p.getName());
                vo.setValue(p.getValue());
                vo.setType(p.getType());
                vo.setLeaf(p.getType() != null && p.getType() == 2);
                if (vo.getLeaf() == null || !vo.getLeaf()) {
                    vo.setChildren(buildPermissionTree(list, p.getId()));
                }
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public List<UmsRole> listRoles() {
        return umsRoleMapper.selectList(null);
    }

    @Override
    public List<UmsRoleWithPermissionsVO> listRolesWithPermissions(Long currentAdminId) {
        List<UmsRole> rolesToShow;
        if (currentAdminId == null) {
            rolesToShow = umsRoleMapper.selectList(null);
        } else {
            List<UmsRole> myRoles = umsAdminMapper.selectRolesByAdminId(currentAdminId);
            boolean isSuperAdmin = myRoles != null && myRoles.stream()
                    .anyMatch(r -> SUPER_ADMIN.equals(r != null ? r.getName() : null));
            if (isSuperAdmin) {
                rolesToShow = umsRoleMapper.selectList(null);
            } else {
                rolesToShow = myRoles != null ? myRoles : new ArrayList<>();
            }
        }
        List<UmsRoleWithPermissionsVO> result = new ArrayList<>(rolesToShow.size());
        for (UmsRole role : rolesToShow) {
            UmsRoleWithPermissionsVO vo = new UmsRoleWithPermissionsVO();
            vo.setId(role.getId());
            vo.setName(role.getName());
            vo.setDescription(role.getDescription());
            vo.setStatus(role.getStatus());
            vo.setPermissions(umsRoleMapper.selectPermissionsByRoleId(role.getId()));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UmsRole addRole(UmsRoleAddParam param) {
        if (param.getName() == null || param.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        UmsRole role = new UmsRole();
        role.setName(param.getName().trim());
        role.setDescription(param.getDescription() != null ? param.getDescription().trim() : null);
        role.setStatus(param.getStatus() != null ? param.getStatus() : 1);
        umsRoleMapper.insert(role);
        if (param.getPermissionIds() != null) {
            Set<Long> allIds = expandWithAncestors(param.getPermissionIds());
            for (Long pid : allIds) {
                if (pid != null) {
                    umsRoleMapper.insertRolePermissionRelation(role.getId(), pid);
                }
            }
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, UmsRoleUpdateParam param) {
        if (Long.valueOf(1).equals(roleId)) {
            throw new IllegalArgumentException("超级管理员禁止修改角色");
        }
        if (roleId == null) {
            throw new IllegalArgumentException("角色 ID 不能为空");
        }
        UmsRole role = umsRoleMapper.selectById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        if (param.getDescription() != null) {
            role.setDescription(param.getDescription().trim());
        }
        if (param.getStatus() != null) {
            role.setStatus(param.getStatus());
        }
        umsRoleMapper.updateById(role);
        if (param.getPermissionIds() != null) {
            umsRoleMapper.deleteRolePermissionRelationByRoleId(roleId);
            Set<Long> allIds = expandWithAncestors(param.getPermissionIds());
            for (Long pid : allIds) {
                if (pid != null) {
                    umsRoleMapper.insertRolePermissionRelation(roleId, pid);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色 ID 不能为空");
        }
        UmsRole role = umsRoleMapper.selectById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        if ("SUPER_ADMIN".equals(role.getName())) {
            throw new IllegalArgumentException("超级管理员角色不可删除");
        }
        umsRoleMapper.deleteRolePermissionRelationByRoleId(roleId);
        umsRoleMapper.deleteAdminRoleRelationByRoleId(roleId);
        umsRoleMapper.deleteById(roleId);
    }
}
