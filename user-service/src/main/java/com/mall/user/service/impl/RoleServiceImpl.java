package com.mall.user.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.user.mapper.UmsRoleMapper;
import com.mall.user.mapper.UmsRoleMenuRelationMapper;
import com.mall.user.mapper.UmsRoleResourceRelationMapper;
import com.mall.user.model.*;
import com.mall.user.service.IResourceService;
import com.mall.user.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 后台角色管理Service实现类
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {
    /** 角色Mapper */
    private final UmsRoleMapper roleMapper;
    /** 角色菜单关系Mapper */
    private final UmsRoleMenuRelationMapper roleMenuRelationMapper;
    /** 角色资源关系Mapper */
    private final UmsRoleResourceRelationMapper roleResourceRelationMapper;
    /** 资源服务 */
    private final IResourceService resourceService;

    @Override
    public int create(UmsRole role) {
        role.setCreateTime(new Date());
        role.setAdminCount(0);
        role.setSort(0);
        return roleMapper.insert(role);
    }

    @Override
    public int update(Long id, UmsRole role) {
        role.setId(id);
        return roleMapper.updateByPrimaryKeySelective(role);
    }

    @Override
    public int delete(List<Long> ids) {
        // TODO: 批量删除角色需要实现 deleteByIds
        int count = 0;
        for (Long id : ids) {
            count += roleMapper.deleteByPrimaryKey(id);
        }
        resourceService.initPathResourceMap();
        return count;
    }

    @Override
    public List<UmsRole> list() {
        return roleMapper.selectByCondition(null);
    }

    @Override
    public List<UmsRole> list(String keyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsRole query = new UmsRole();
        if (!StringUtils.isEmpty(keyword)) {
            query.setName("%" + keyword + "%");
        }
        return roleMapper.selectByCondition(query);
    }

    @Override
    public List<UmsMenu> getMenuList(Long adminId) {
        return roleMapper.getMenuList(adminId);
    }

    @Override
    public List<UmsMenu> listMenu(Long roleId) {
        return roleMapper.getMenuListByRoleId(roleId);
    }

    @Override
    public List<UmsResource> listResource(Long roleId) {
        return roleMapper.getResourceListByRoleId(roleId);
    }

    @Override
    public int allocMenu(Long roleId, List<Long> menuIds) {
        //先删除原有关系
        UmsRoleMenuRelation relation = new UmsRoleMenuRelation();
        relation.setRoleId(roleId);
        roleMenuRelationMapper.deleteByCondition(relation);
        //批量插入新关系
        for (Long menuId : menuIds) {
            UmsRoleMenuRelation relationNew = new UmsRoleMenuRelation();
            relationNew.setRoleId(roleId);
            relationNew.setMenuId(menuId);
            roleMenuRelationMapper.insert(relationNew);
        }
        return menuIds.size();
    }

    @Override
    public int allocResource(Long roleId, List<Long> resourceIds) {
        //先删除原有关系
        UmsRoleResourceRelation relation = new UmsRoleResourceRelation();
        relation.setRoleId(roleId);
        roleResourceRelationMapper.deleteByCondition(relation);
        //批量插入新关系
        for (Long resourceId : resourceIds) {
            UmsRoleResourceRelation relationNew = new UmsRoleResourceRelation();
            relationNew.setRoleId(roleId);
            relationNew.setResourceId(resourceId);
            roleResourceRelationMapper.insert(relationNew);
        }
        resourceService.initPathResourceMap();
        return resourceIds.size();
    }
}
