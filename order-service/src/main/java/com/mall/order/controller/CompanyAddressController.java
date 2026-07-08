package com.mall.order.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.order.model.OmsCompanyAddress;
import com.mall.order.service.ICompanyAddressService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 收货地址管理Controller
 * Created by macro on 2018/10/18.
 */
@Controller
@Tag(name = "CompanyAddressController", description = "收货地址管理")
@RequestMapping("/companyAddress")
@RequiredArgsConstructor
public class CompanyAddressController {
    /** 收货地址服务 */
    private final ICompanyAddressService companyAddressService;

    @Operation(summary = "获取所有收货地址")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<OmsCompanyAddress>> list() {
        List<OmsCompanyAddress> companyAddressList = companyAddressService.list();
        return CommonResult.success(companyAddressList);
    }
}
