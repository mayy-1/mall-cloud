package com.mall.user.mapper;

import com.mall.user.model.UmsAdminPermissionRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 后台用户权限关系Mapper
 */
public interface UmsAdminPermissionRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsAdminPermissionRelation row);

    int insertSelective(UmsAdminPermissionRelation row);

    UmsAdminPermissionRelation selectByPrimaryKey(Long id);

    List<UmsAdminPermissionRelation> selectByCondition(UmsAdminPermissionRelation record);

    int deleteByCondition(UmsAdminPermissionRelation record);

    int updateByPrimaryKeySelective(UmsAdminPermissionRelation row);

    int updateByPrimaryKey(UmsAdminPermissionRelation row);
}