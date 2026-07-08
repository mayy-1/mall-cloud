package com.mall.user.mapper;

import com.mall.user.model.UmsAdminPermissionRelation;
import com.mall.user.model.UmsAdminPermissionRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 后台用户权限关系Mapper
 */
public interface UmsAdminPermissionRelationMapper {
    long countByExample(UmsAdminPermissionRelationExample example);

    int deleteByExample(UmsAdminPermissionRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsAdminPermissionRelation row);

    int insertSelective(UmsAdminPermissionRelation row);

    List<UmsAdminPermissionRelation> selectByExample(UmsAdminPermissionRelationExample example);

    UmsAdminPermissionRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") UmsAdminPermissionRelation row, @Param("example") UmsAdminPermissionRelationExample example);

    int updateByExample(@Param("row") UmsAdminPermissionRelation row, @Param("example") UmsAdminPermissionRelationExample example);

    int updateByPrimaryKeySelective(UmsAdminPermissionRelation row);

    int updateByPrimaryKey(UmsAdminPermissionRelation row);
}