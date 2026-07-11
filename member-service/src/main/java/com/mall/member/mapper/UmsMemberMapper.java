package com.mall.member.mapper;

import com.mall.member.model.UmsMember;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员数据访问接口
 */
public interface UmsMemberMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMember row);

    int insertSelective(UmsMember row);
    UmsMember selectByPrimaryKey(Long id);

    List<UmsMember> selectByCondition(UmsMember record);

    int deleteByCondition(UmsMember record);

    int updateSelectiveByCondition(@Param("record") UmsMember record, @Param("condition") UmsMember condition);

    int updateByPrimaryKeySelective(UmsMember row);

    int updateByPrimaryKey(UmsMember row);
}