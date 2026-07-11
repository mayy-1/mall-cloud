package com.mall.member.mapper;

import com.mall.member.model.UmsMemberMemberTagRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员与标签关联数据访问接口
 */
public interface UmsMemberMemberTagRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberMemberTagRelation row);

    int insertSelective(UmsMemberMemberTagRelation row);
    UmsMemberMemberTagRelation selectByPrimaryKey(Long id);

    List<UmsMemberMemberTagRelation> selectByCondition(UmsMemberMemberTagRelation record);

    int deleteByCondition(UmsMemberMemberTagRelation record);

    int updateSelectiveByCondition(@Param("record") UmsMemberMemberTagRelation record, @Param("condition") UmsMemberMemberTagRelation condition);

    int updateByPrimaryKeySelective(UmsMemberMemberTagRelation row);

    int updateByPrimaryKey(UmsMemberMemberTagRelation row);
}