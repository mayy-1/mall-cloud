package com.mall.order.service;

import com.mall.order.model.OmsOrderSetting;

/**
 * 订单设置Service
 */
public interface IOrderSettingService {
    /**
     * 获取指定订单设置
     */
    OmsOrderSetting getItem(Long id);

    /**
     * 修改指定订单设置
     */
    int update(Long id, OmsOrderSetting orderSetting);
}
