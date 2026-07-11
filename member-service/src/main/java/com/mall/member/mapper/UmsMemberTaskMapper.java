package com.mall.member.mapper;

import com.mall.member.model.UmsMemberTask;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员任务数据访问接口
 */
public interface UmsMemberTaskMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberTask row);

    int insertSelective(UmsMemberTask row);
    UmsMemberTask selectByPrimaryKey(Long id);

    List<UmsMemberTask> selectByCondition(UmsMemberTask record);

    int deleteByCondition(UmsMemberTask record);

    int updateSelectiveByCondition(@Param("record") UmsMemberTask record, @Param("condition") UmsMemberTask condition);

    int updateByPrimaryKeySelective(UmsMemberTask row);

    int updateByPrimaryKey(UmsMemberTask row);
}