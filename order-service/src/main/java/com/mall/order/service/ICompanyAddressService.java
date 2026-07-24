package com.mall.order.service;

import com.mall.order.model.OmsCompanyAddress;

import java.util.List;

/**
 * 收货地址管Service
 */
public interface ICompanyAddressService {
    /**
     * 获取全部收货地址
     */
    List<OmsCompanyAddress> list();
}
