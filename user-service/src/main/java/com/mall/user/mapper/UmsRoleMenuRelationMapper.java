package com.mall.user.mapper;

import com.mall.user.model.UmsRoleMenuRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**角色菜单关系Mapper */
public interface UmsRoleMenuRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsRoleMenuRelation row);

    int insertSelective(UmsRoleMenuRelation row);

    UmsRoleMenuRelation selectByPrimaryKey(Long id);

    List<UmsRoleMenuRelation> selectByCondition(UmsRoleMenuRelation record);

    int deleteByCondition(UmsRoleMenuRelation record);

    int updateByPrimaryKeySelective(UmsRoleMenuRelation row);

    int updateByPrimaryKey(UmsRoleMenuRelation row);
}