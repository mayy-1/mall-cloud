package com.mall.order.mapper;

import com.mall.order.model.OmsOrderSetting;
import com.mall.order.model.OmsOrderSettingExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 订单设置Mapper
 * 提供订单设置的增删改查操作
 */
public interface OmsOrderSettingMapper {
    long countByExample(OmsOrderSettingExample example);

    int deleteByExample(OmsOrderSettingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrderSetting row);

    int insertSelective(OmsOrderSetting row);

    List<OmsOrderSetting> selectByExample(OmsOrderSettingExample example);

    OmsOrderSetting selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") OmsOrderSetting row, @Param("example") OmsOrderSettingExample example);

    int updateByExample(@Param("row") OmsOrderSetting row, @Param("example") OmsOrderSettingExample example);

    int updateByPrimaryKeySelective(OmsOrderSetting row);

    int updateByPrimaryKey(OmsOrderSetting row);
}