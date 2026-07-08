package com.mall.member.mapper;

import com.mall.member.model.UmsGrowthChangeHistory;
import com.mall.member.model.UmsGrowthChangeHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 成长值变化历史数据访问接口
 */
public interface UmsGrowthChangeHistoryMapper {
    long countByExample(UmsGrowthChangeHistoryExample example);

    int deleteByExample(UmsGrowthChangeHistoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsGrowthChangeHistory row);

    int insertSelective(UmsGrowthChangeHistory row);

    List<UmsGrowthChangeHistory> selectByExample(UmsGrowthChangeHistoryExample example);

    UmsGrowthChangeHistory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") UmsGrowthChangeHistory row, @Param("example") UmsGrowthChangeHistoryExample example);

    int updateByExample(@Param("row") UmsGrowthChangeHistory row, @Param("example") UmsGrowthChangeHistoryExample example);

    int updateByPrimaryKeySelective(UmsGrowthChangeHistory row);

    int updateByPrimaryKey(UmsGrowthChangeHistory row);
}