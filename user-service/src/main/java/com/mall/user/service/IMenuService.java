package com.mall.user.service;

import com.mall.user.domain.dto.UmsMenuNode;
import com.mall.user.model.UmsMenu;

import java.util.List;

/**
 * 后台菜单管理Service
 * Created by macro on 2020/2/2.
 */
public interface IMenuService {
    /**
     * 根据ID获取菜单详情
     */
    UmsMenu getItem(Long id);

    /**
     * 分页查询后台菜单
     */
    List<UmsMenu> list(Long parentId, Integer pageSize, Integer pageNum);

    /**
     * 树形结构返回所有菜单列表
     */
    List<UmsMenuNode> treeList();

    /**
     * 修改菜单显示状态
     */
    int updateHidden(Long id, Integer hidden);
}
