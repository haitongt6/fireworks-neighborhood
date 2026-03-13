package com.fireworks.service.impl;

import com.fireworks.model.pojo.UmsRole;
import com.fireworks.service.UmsRoleService;
import com.fireworks.service.mapper.UmsRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link UmsRoleService} 的实现类。
 */
@Service
public class UmsRoleServiceImpl implements UmsRoleService {

    private final UmsRoleMapper umsRoleMapper;

    public UmsRoleServiceImpl(UmsRoleMapper umsRoleMapper) {
        this.umsRoleMapper = umsRoleMapper;
    }

    @Override
    public List<UmsRole> listRoles() {
        return umsRoleMapper.selectList(null);
    }
}
