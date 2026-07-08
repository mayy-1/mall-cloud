package com.mall.user.mapper;

import com.mall.user.model.UmsAdminRoleRelation;
import com.mall.user.model.UmsAdminRoleRelationExample;
import com.mall.user.model.UmsResource;
import com.mall.user.model.UmsRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 后台用户与角色管理Mapper（合并MBG生成+自定义）
 * Created by macro on 2018/10/8.
 */
public interface UmsAdminRoleRelationMapperCustom extends UmsAdminRoleRelationMapper {
    // ===== MBG 自动生成方法 =====
    long countByExample(UmsAdminRoleRelationExample example);
    int deleteByExample(UmsAdminRoleRelationExample example);
    int deleteByPrimaryKey(Long id);
    int insert(UmsAdminRoleRelation record);
    int insertSelective(UmsAdminRoleRelation record);
    List<UmsAdminRoleRelation> selectByExample(UmsAdminRoleRelationExample example);
    UmsAdminRoleRelation selectByPrimaryKey(Long id);
    int updateByExampleSelective(@Param("record") UmsAdminRoleRelation record, @Param("example") UmsAdminRoleRelationExample example);
    int updateByExample(@Param("record") UmsAdminRoleRelation record, @Param("example") UmsAdminRoleRelationExample example);
    int updateByPrimaryKeySelective(UmsAdminRoleRelation record);
    int updateByPrimaryKey(UmsAdminRoleRelation record);

    // ===== 自定义方法 =====
    int insertList(@Param("list") List<UmsAdminRoleRelation> adminRoleRelationList);
    List<UmsRole> getRoleList(@Param("adminId") Long adminId);
    List<UmsResource> getResourceList(@Param("adminId") Long adminId);
    List<Long> getAdminIdList(@Param("resourceId") Long resourceId);
}
