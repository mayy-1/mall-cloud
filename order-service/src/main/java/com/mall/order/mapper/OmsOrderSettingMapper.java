package com.mall.order.mapper;

import com.mall.order.model.OmsOrderSetting;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 订单设置Mapper
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