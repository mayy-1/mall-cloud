package com.mall.member.mapper;

import com.mall.member.model.UmsIntegrationConsumeSetting;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 积分消费设置数据访问接口
 */
public interface UmsIntegrationConsumeSettingMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsIntegrationConsumeSetting row);

    int insertSelective(UmsIntegrationConsumeSetting row);
    UmsIntegrationConsumeSetting selectByPrimaryKey(Long id);

    List<UmsIntegrationConsumeSetting> selectByCondition(UmsIntegrationConsumeSetting record);

    int deleteByCondition(UmsIntegrationConsumeSetting record);

    int updateSelectiveByCondition(@Param("record") UmsIntegrationConsumeSetting record, @Param("condition") UmsIntegrationConsumeSetting condition);

    int updateByPrimaryKeySelective(UmsIntegrationConsumeSetting row);

    int updateByPrimaryKey(UmsIntegrationConsumeSetting row);
}