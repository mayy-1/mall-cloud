package com.mall.order.service.impl;

import com.mall.order.mapper.OmsCompanyAddressMapper;
import com.mall.order.model.OmsCompanyAddress;import com.mall.order.service.ICompanyAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收货地址管理Service实现类
 */
@Service
@RequiredArgsConstructor
public class CompanyAddressServiceImpl implements ICompanyAddressService {
    /** 收货地址Mapper */
    private final OmsCompanyAddressMapper companyAddressMapper;
    @Override
    public List<OmsCompanyAddress> list() {
        return companyAddressMapper.selectByCondition(new OmsCompanyAddress());
    }
}
