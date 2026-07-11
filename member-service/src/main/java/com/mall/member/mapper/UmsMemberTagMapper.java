package com.mall.member.mapper;

import com.mall.member.model.UmsMemberTag;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员标签数据访问接口
 */
public interface UmsMemberTagMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberTag row);

    int insertSelective(UmsMemberTag row);
    UmsMemberTag selectByPrimaryKey(Long id);

    List<UmsMemberTag> selectByCondition(UmsMemberTag record);

    int deleteByCondition(UmsMemberTag record);

    int updateSelectiveByCondition(@Param("record") UmsMemberTag record, @Param("condition") UmsMemberTag condition);

    int updateByPrimaryKeySelective(UmsMemberTag row);

    int updateByPrimaryKey(UmsMemberTag row);
}