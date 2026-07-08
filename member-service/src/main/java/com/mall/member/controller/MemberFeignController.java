package com.mall.member.controller;

import com.mall.member.model.UmsMember;
import com.mall.member.service.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 会员Feign调用Controller
 * 暴露/member路径端点供trade-service和cart-service通过Feign调用
 */
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberFeignController {

    /** 会员业务服务 */
    private final IMemberService memberService;

    @GetMapping("/current")
    public UmsMember getCurrentMember() {
        return memberService.getCurrentMember();
    }

    @GetMapping("/{id}")
    public UmsMember getById(@PathVariable("id") Long id) {
        return memberService.getById(id);
    }

    @PostMapping("/updateIntegration")
    public void updateIntegration(@RequestParam Long id, @RequestParam Integer integration) {
        memberService.updateIntegration(id, integration);
    }
}
