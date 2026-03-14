package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireworks.model.dto.ButtonAddParam;
import com.fireworks.model.dto.ButtonUpdateParam;
import com.fireworks.model.dto.RootMenuAddParam;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.vo.MenuManageItemVO;
import com.fireworks.model.vo.MenuTreeNodeVO;
import com.fireworks.service.UmsMenuService;
import com.fireworks.service.mapper.UmsAdminMapper;
import com.fireworks.service.mapper.UmsPermissionMapper;
import com.fireworks.service.mapper.UmsRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link UmsMenuService} 的实现类。
 */
@Service
public class UmsMenuServiceImpl implements UmsMenuService {

    private final UmsAdminMapper umsAdminMapper;
    private final UmsPermissionMapper umsPermissionMapper;
    private final UmsRoleMapper umsRoleMapper;

    public UmsMenuServiceImpl(UmsAdminMapper umsAdminMapper, UmsPermissionMapper umsPermissionMapper,
                              UmsRoleMapper umsRoleMapper) {
        this.umsAdminMapper = umsAdminMapper;
        this.umsPermissionMapper = umsPermissionMapper;
        this.umsRoleMapper = umsRoleMapper;
    }

    @Override
    public List<MenuTreeNodeVO> getMenuTree(Long adminId) {
        List<UmsPermission> permissions = umsAdminMapper.selectPermissionByAdminId(adminId);
        List<UmsPermission> menuNodes = permissions.stream()
                .filter(p -> p.getType() != null && (p.getType() == 0 || p.getType() == 1))
                .collect(Collectors.toList());
        return buildTree(menuNodes, 0L);
    }

    private List<MenuTreeNodeVO> buildTree(List<UmsPermission> list, Long pid) {
        List<MenuTreeNodeVO> result = new ArrayList<>();
        for (UmsPermission p : list) {
            if (pid.equals(p.getPid())) {
                MenuTreeNodeVO vo = new MenuTreeNodeVO();
                vo.setId(p.getId());
                vo.setName(p.getName());
                vo.setPath(p.getValue() != null ? p.getValue() : "");
                vo.setIcon(p.getIcon());
                vo.setChildren(buildTree(list, p.getId()));
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public List<MenuManageItemVO> listAllForManage() {
        List<UmsPermission> all = umsPermissionMapper.selectList(
                new LambdaQueryWrapper<UmsPermission>().orderByAsc(UmsPermission::getId));
        List<MenuManageItemVO> result = new ArrayList<>();
        appendWithLevel(all, 0L, 0, result);
        return result;
    }

    private void appendWithLevel(List<UmsPermission> list, Long pid, int level, List<MenuManageItemVO> result) {
        for (UmsPermission p : list) {
            Long pPid = p.getPid() != null ? p.getPid() : 0L;
            if (!pPid.equals(pid)) continue;
            MenuManageItemVO vo = new MenuManageItemVO();
            vo.setId(p.getId());
            vo.setPid(p.getPid());
            vo.setName(p.getName());
            vo.setValue(p.getValue());
            vo.setIcon(p.getIcon());
            vo.setType(p.getType());
            vo.setLevel(level);
            result.add(vo);
            appendWithLevel(list, p.getId(), level + 1, result);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UmsPermission addRootMenu(RootMenuAddParam param) {
        if (param.getType() == null || (param.getType() != 0 && param.getType() != 1)) {
            throw new IllegalArgumentException("类型必须为目录(0)或菜单(1)");
        }
        if (param.getName() == null || param.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }
        if (param.getType() == 1 && (param.getValue() == null || param.getValue().trim().isEmpty())) {
            throw new IllegalArgumentException("菜单类型时路由不能为空");
        }
        UmsPermission p = new UmsPermission();
        p.setPid(0L);
        p.setName(param.getName().trim());
        p.setValue(param.getType() == 1 ? param.getValue().trim() : (param.getValue() != null ? param.getValue().trim() : null));
        p.setIcon(param.getIcon() != null ? param.getIcon().trim() : null);
        p.setType(param.getType());
        umsPermissionMapper.insert(p);
        return p;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UmsPermission addButton(ButtonAddParam param) {
        if (param.getMenuId() == null) {
            throw new IllegalArgumentException("所属菜单不能为空");
        }
        UmsPermission menu = umsPermissionMapper.selectById(param.getMenuId());
        if (menu == null || menu.getType() == null || menu.getType() != 1) {
            throw new IllegalArgumentException("所属菜单不存在或不是菜单类型");
        }
        if (param.getName() == null || param.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("按钮名称不能为空");
        }
        if (param.getValue() == null || param.getValue().trim().isEmpty()) {
            throw new IllegalArgumentException("权限值不能为空");
        }
        UmsPermission btn = new UmsPermission();
        btn.setPid(param.getMenuId());
        btn.setName(param.getName().trim());
        btn.setValue(param.getValue().trim());
        btn.setType(2);
        umsPermissionMapper.insert(btn);
        return btn;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateButton(Long buttonId, ButtonUpdateParam param) {
        if (buttonId == null) {
            throw new IllegalArgumentException("按钮 ID 不能为空");
        }
        UmsPermission btn = umsPermissionMapper.selectById(buttonId);
        if (btn == null || btn.getType() == null || btn.getType() != 2) {
            throw new IllegalArgumentException("按钮不存在");
        }
        if (param.getName() != null) {
            btn.setName(param.getName().trim());
        }
        if (param.getValue() != null) {
            btn.setValue(param.getValue().trim());
        }
        umsPermissionMapper.updateById(btn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteButton(Long buttonId) {
        if (buttonId == null) {
            throw new IllegalArgumentException("按钮 ID 不能为空");
        }
        UmsPermission btn = umsPermissionMapper.selectById(buttonId);
        if (btn == null || btn.getType() == null || btn.getType() != 2) {
            throw new IllegalArgumentException("按钮不存在");
        }
        umsPermissionMapper.deleteById(buttonId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDirectoryOrMenu(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID 不能为空");
        }
        UmsPermission node = umsPermissionMapper.selectById(id);
        if (node == null || node.getType() == null || (node.getType() != 0 && node.getType() != 1)) {
            throw new IllegalArgumentException("目录或菜单不存在");
        }
        List<UmsPermission> all = umsPermissionMapper.selectList(null);
        List<Long> toDelete = collectDescendantIds(all, id);
        toDelete.add(id);
        for (Long pid : toDelete) {
            umsRoleMapper.deleteRolePermissionRelationByPermissionId(pid);
            umsPermissionMapper.deleteById(pid);
        }
    }

    /** 收集自身及所有后代 ID，按深度优先倒序（先删子后删父） */
    private List<Long> collectDescendantIds(List<UmsPermission> list, Long rootId) {
        List<Long> result = new LinkedList<>();
        collectRecursive(list, rootId, result);
        return result;
    }

    private void collectRecursive(List<UmsPermission> list, Long pid, List<Long> result) {
        for (UmsPermission p : list) {
            if (pid.equals(p.getPid() != null ? p.getPid() : 0L)) {
                collectRecursive(list, p.getId(), result);
                result.add(p.getId());
            }
        }
    }
}
