package com.mall.member.mapper;

import com.mall.member.model.UmsMemberMemberTagRelation;
import com.mall.member.model.UmsMemberMemberTagRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员与标签关联数据访问接口
 */
public interface UmsMemberMemberTagRelationMapper {
    long countByExample(UmsMemberMemberTagRelationExample example);

    int deleteByExample(UmsMemberMemberTagRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberMemberTagRelation row);

    int insertSelective(UmsMemberMemberTagRelation row);

    List<UmsMemberMemberTagRelation> selectByExample(UmsMemberMemberTagRelationExample example);

    UmsMemberMemberTagRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") UmsMemberMemberTagRelation row, @Param("example") UmsMemberMemberTagRelationExample example);

    int updateByExample(@Param("row") UmsMemberMemberTagRelation row, @Param("example") UmsMemberMemberTagRelationExample example);

    int updateByPrimaryKeySelective(UmsMemberMemberTagRelation row);

    int updateByPrimaryKey(UmsMemberMemberTagRelation row);
}