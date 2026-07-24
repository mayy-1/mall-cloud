package com.mall.trade.mapper;

import com.mall.trade.model.OmsOrderSetting;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 订单设置数据访问接口
 */
public interface OmsOrderSettingMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrderSetting row);

    int insertSelective(OmsOrderSetting row);
    OmsOrderSetting selectByPrimaryKey(Long id);

    List<OmsOrderSetting> selectByCondition(OmsOrderSetting record);

    int deleteByCondition(OmsOrderSetting record);

    int updateSelectiveByCondition(@Param("record") OmsOrderSetting record, @Param("condition") OmsOrderSetting condition);

    int updateByPrimaryKeySelective(OmsOrderSetting row);

    int updateByPrimaryKey(OmsOrderSetting row);
}
