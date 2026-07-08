package com.mall.trade.mapper;

import com.mall.trade.model.UmsIntegrationConsumeSetting;
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

    int updateByPrimaryKeySelective(UmsIntegrationConsumeSetting row);

    int updateByPrimaryKey(UmsIntegrationConsumeSetting row);
}
