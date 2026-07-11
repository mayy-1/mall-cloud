package com.mall.member.mapper;

import com.mall.member.model.UmsIntegrationChangeHistory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 积分变化历史数据访问接口
 */
public interface UmsIntegrationChangeHistoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsIntegrationChangeHistory row);

    int insertSelective(UmsIntegrationChangeHistory row);
    UmsIntegrationChangeHistory selectByPrimaryKey(Long id);

    List<UmsIntegrationChangeHistory> selectByCondition(UmsIntegrationChangeHistory record);

    int deleteByCondition(UmsIntegrationChangeHistory record);

    int updateSelectiveByCondition(@Param("record") UmsIntegrationChangeHistory record, @Param("condition") UmsIntegrationChangeHistory condition);

    int updateByPrimaryKeySelective(UmsIntegrationChangeHistory row);

    int updateByPrimaryKey(UmsIntegrationChangeHistory row);
}