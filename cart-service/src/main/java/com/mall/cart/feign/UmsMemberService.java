package com.mall.cart.feign;

import com.mall.cart.model.UmsMember;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 会员服务 Feign 接口
 */
@FeignClient("member-service")
public interface UmsMemberService {

    @GetMapping("/member/current")
    UmsMember getCurrentMember();

    @GetMapping("/member/{id}")
    UmsMember getById(@PathVariable("id") Long id);
}
