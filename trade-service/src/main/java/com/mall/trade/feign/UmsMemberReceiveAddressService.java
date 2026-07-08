package com.mall.trade.feign;

import com.mall.trade.model.UmsMemberReceiveAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 会员收货地址服务Feign客户端
 * 获取会员收货地址列表及详情
 */
@FeignClient("member-service")
public interface UmsMemberReceiveAddressService {

    /** 获取会员收货地址列表 */
    @GetMapping("/member/address/list")
    List<UmsMemberReceiveAddress> list();

    /** 根据ID获取收货地址 */
    @GetMapping("/member/address/{id}")
    UmsMemberReceiveAddress getItem(@PathVariable("id") Long id);
}
