package com.mall.user.mapper;

import com.mall.user.model.UmsRole;
import com.mall.user.model.UmsMenu;
import com.mall.user.model.UmsResource;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**后台角色Mapper */
@Primary
public interface UmsRoleMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsRole row);

    int insertSelective(UmsRole row);

    UmsRole selectByPrimaryKey(Long id);

    List<UmsRole> selectByCondition(UmsRole record);

    int deleteByCondition(UmsRole record);

    int updateByPrimaryKeySelective(UmsRole row);

    int updateByPrimaryKey(UmsRole row);

    List<UmsMenu> getMenuList(@Param("adminId") Long adminId);

    List<UmsMenu> getMenuListByRoleId(@Param("roleId") Long roleId);

    List<UmsResource> getResourceListByRoleId(@Param("roleId") Long roleId);
}