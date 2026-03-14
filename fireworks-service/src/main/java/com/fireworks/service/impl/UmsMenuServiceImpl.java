package com.fireworks.service.impl;

import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.vo.MenuTreeNodeVO;
import com.fireworks.service.UmsMenuService;
import com.fireworks.service.mapper.UmsAdminMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link UmsMenuService} 的实现类。
 */
@Service
public class UmsMenuServiceImpl implements UmsMenuService {

    private final UmsAdminMapper umsAdminMapper;

    public UmsMenuServiceImpl(UmsAdminMapper umsAdminMapper) {
        this.umsAdminMapper = umsAdminMapper;
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
}
