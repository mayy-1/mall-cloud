package com.mall.member.mapper;

import com.mall.member.model.UmsMemberLevel;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员等级数据访问接口
 */
public interface UmsMemberLevelMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberLevel row);

    int insertSelective(UmsMemberLevel row);
    UmsMemberLevel selectByPrimaryKey(Long id);

    List<UmsMemberLevel> selectByCondition(UmsMemberLevel record);

    int deleteByCondition(UmsMemberLevel record);

    int updateSelectiveByCondition(@Param("record") UmsMemberLevel record, @Param("condition") UmsMemberLevel condition);

    int updateByPrimaryKeySelective(UmsMemberLevel row);

    int updateByPrimaryKey(UmsMemberLevel row);
}