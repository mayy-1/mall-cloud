package com.mall.order.service.impl;

import com.mall.order.mapper.OmsOrderSettingMapper;
import com.mall.order.model.OmsOrderSetting;
import com.mall.order.service.IOrderSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 订单设置管理Service实现类
 */
@Service
@RequiredArgsConstructor
public class OrderSettingServiceImpl implements IOrderSettingService {
    /** 订单设置Mapper */
    private final OmsOrderSettingMapper orderSettingMapper;

    @Override
    public OmsOrderSetting getItem(Long id) {
        return orderSettingMapper.selectByPrimaryKey(id);
    }

    @Override
    public int update(Long id, OmsOrderSetting orderSetting) {
        OmsOrderSetting omsOrderSetting = orderSettingMapper.selectByPrimaryKey(id);

        orderSetting.setId(id);
        if (omsOrderSetting == null) {

            return orderSettingMapper.insert(orderSetting);
        }
        return orderSettingMapper.updateByPrimaryKey(orderSetting);
    }
}
