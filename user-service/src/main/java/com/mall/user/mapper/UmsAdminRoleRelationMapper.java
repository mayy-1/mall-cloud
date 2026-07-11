package com.mall.user.mapper;

import com.mall.user.model.UmsAdminRoleRelation;
import com.mall.user.model.UmsResource;
import com.mall.user.model.UmsRole;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 后台用户角色关系Mapper
 */
@Primary
public interface UmsAdminRoleRelationMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UmsAdminRoleRelation row);

    int insertSelective(UmsAdminRoleRelation row);

    UmsAdminRoleRelation selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UmsAdminRoleRelation row);

    int updateByPrimaryKey(UmsAdminRoleRelation row);

    List<UmsAdminRoleRelation> selectByCondition(UmsAdminRoleRelation record);

    int deleteByCondition(UmsAdminRoleRelation record);

    int insertList(@Param("list") List<UmsAdminRoleRelation> adminRoleRelationList);

    List<UmsRole> getRoleList(@Param("adminId") Long adminId);

    List<UmsResource> getResourceList(@Param("adminId") Long adminId);

    List<Long> getAdminIdList(@Param("resourceId") Long resourceId);
}
