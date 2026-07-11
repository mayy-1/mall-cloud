package com.mall.api.client.member;

import com.mall.api.dto.MemberAddressDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 会员收货地址服务 Feign 接口
 */
@FeignClient(name = "member-service", path = "/member/address")
public interface MemberAddressClient {

    /** 获取会员收货地址列表 */
    @GetMapping("/list")
    CommonResult<List<MemberAddressDTO>> list();

    /** 根据ID获取收货地址 */
    @GetMapping("/{id}")
    CommonResult<MemberAddressDTO> getItem(@PathVariable("id") Long id);
}
