package com.mall.member.mapper;

import com.mall.member.model.UmsGrowthChangeHistory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 成长值变化历史数据访问接口
 */
public interface UmsGrowthChangeHistoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsGrowthChangeHistory row);

    int insertSelective(UmsGrowthChangeHistory row);
    UmsGrowthChangeHistory selectByPrimaryKey(Long id);

    List<UmsGrowthChangeHistory> selectByCondition(UmsGrowthChangeHistory record);

    int deleteByCondition(UmsGrowthChangeHistory record);

    int updateSelectiveByCondition(@Param("record") UmsGrowthChangeHistory record, @Param("condition") UmsGrowthChangeHistory condition);

    int updateByPrimaryKeySelective(UmsGrowthChangeHistory row);

    int updateByPrimaryKey(UmsGrowthChangeHistory row);
}