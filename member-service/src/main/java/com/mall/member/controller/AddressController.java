package com.mall.member.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.member.model.UmsMemberReceiveAddress;
import com.mall.member.service.IAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员收货地址管理Controller
 */
@RestController
@Tag(name = "AddressController", description = "会员收货地址管理")
@RequestMapping("/member/address")
@RequiredArgsConstructor
public class AddressController {
    /** 收货地址业务服务 */
    private final IAddressService addressService;

    @Operation(summary = "添加收货地址")
    @PostMapping("/add")
    public CommonResult add(@RequestBody UmsMemberReceiveAddress address) {
        int count = addressService.add(address);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "删除收货地址")
    @PostMapping("/delete/{id}")
    public CommonResult delete(@PathVariable Long id) {
        int count = addressService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改收货地址")
    @PostMapping("/update/{id}")
    public CommonResult update(@PathVariable Long id, @RequestBody UmsMemberReceiveAddress address) {
        int count = addressService.update(id, address);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "显示所有收货地址")
    @GetMapping("/list")
    public CommonResult<List<UmsMemberReceiveAddress>> list() {
        List<UmsMemberReceiveAddress> addressList = addressService.list();
        return CommonResult.success(addressList);
    }

    @Operation(summary = "获取收货地址详情")
    @GetMapping("/{id}")
    public CommonResult<UmsMemberReceiveAddress> getItem(@PathVariable Long id) {
        UmsMemberReceiveAddress address = addressService.getItem(id);
        return CommonResult.success(address);
    }
}
