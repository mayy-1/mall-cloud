package com.mall.user.mapper;

import com.mall.user.model.UmsRoleResourceRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**角色资源关系Mapper */
public interface UmsRoleResourceRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsRoleResourceRelation row);

    int insertSelective(UmsRoleResourceRelation row);

    UmsRoleResourceRelation selectByPrimaryKey(Long id);

    List<UmsRoleResourceRelation> selectByCondition(UmsRoleResourceRelation record);

    int deleteByCondition(UmsRoleResourceRelation record);

    int updateByPrimaryKeySelective(UmsRoleResourceRelation row);

    int updateByPrimaryKey(UmsRoleResourceRelation row);
}