package com.mall.trade.mapper;

import com.mall.trade.model.UmsIntegrationConsumeSetting;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 积分消费设置数据访问接口
 */
public interface UmsIntegrationConsumeSettingMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UmsIntegrationConsumeSetting row);

    int insertSelective(UmsIntegrationConsumeSetting row);

    UmsIntegrationConsumeSetting selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UmsIntegrationConsumeSetting row);

    int updateByPrimaryKey(UmsIntegrationConsumeSetting row);

    List<UmsIntegrationConsumeSetting> selectByCondition(UmsIntegrationConsumeSetting record);

    int deleteByCondition(UmsIntegrationConsumeSetting record);

    int updateSelectiveByCondition(@Param("record") UmsIntegrationConsumeSetting record, @Param("condition") UmsIntegrationConsumeSetting condition);

}