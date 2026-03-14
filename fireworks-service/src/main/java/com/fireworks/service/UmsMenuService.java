package com.fireworks.service;

import com.fireworks.model.vo.MenuTreeNodeVO;

import java.util.List;

/**
 * 后台菜单业务接口。
 */
public interface UmsMenuService {

    /**
     * 根据管理员 ID 获取其可访问的菜单树。
     * <p>
     * 仅返回 type 为 0（目录）和 1（菜单）的节点，按 pid 构建树形结构。
     * </p>
     *
     * @param adminId 管理员 ID
     * @return 菜单树根节点列表
     */
    List<MenuTreeNodeVO> getMenuTree(Long adminId);
}
