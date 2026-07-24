package com.mall.order.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.order.model.OmsCompanyAddress;
import com.mall.order.service.ICompanyAddressService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收货地址管理Controller
 */
@RestController
@Tag(name = "CompanyAddressController", description = "收货地址管理")
@RequestMapping("/companyAddress")
@RequiredArgsConstructor
public class CompanyAddressController {
    /** 收货地址服务 */
    private final ICompanyAddressService companyAddressService;

    @Operation(summary = "获取所有收货地址")
    @GetMapping("/list")
    public CommonResult<List<OmsCompanyAddress>> list() {
        List<OmsCompanyAddress> companyAddressList = companyAddressService.list();
        return CommonResult.success(companyAddressList);
    }
}
