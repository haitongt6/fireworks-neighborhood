package com.fireworks.service;

import com.fireworks.model.dto.ButtonAddParam;
import com.fireworks.model.dto.ButtonUpdateParam;
import com.fireworks.model.dto.RootMenuAddParam;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.vo.MenuManageItemVO;
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

    /**
     * 菜单管理：获取全部目录、菜单、按钮的级联列表。
     *
     * @return 带 level 的扁平列表，按树序排列
     */
    List<MenuManageItemVO> listAllForManage();

    /**
     * 新增根级目录或菜单。
     *
     * @param param 新增参数
     * @return 新增的权限
     */
    UmsPermission addRootMenu(RootMenuAddParam param);

    /**
     * 在指定菜单下新增按钮权限。
     *
     * @param param 新增参数
     * @return 新增的权限
     */
    UmsPermission addButton(ButtonAddParam param);

    /**
     * 修改按钮权限（名称、权限值）。
     *
     * @param buttonId 按钮 ID
     * @param param    修改参数
     */
    void updateButton(Long buttonId, ButtonUpdateParam param);

    /**
     * 删除按钮权限。
     *
     * @param buttonId 按钮 ID
     */
    void deleteButton(Long buttonId);

    /**
     * 删除目录或菜单（含其下所有子节点及角色关联）。仅支持 type 0、1。
     *
     * @param id 目录或菜单 ID
     */
    void deleteDirectoryOrMenu(Long id);
}
