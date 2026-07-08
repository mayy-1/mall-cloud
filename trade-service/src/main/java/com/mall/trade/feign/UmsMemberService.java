package com.mall.trade.feign;

import com.mall.trade.model.UmsMember;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 会员服务Feign客户端
 * 通过远程调用获取会员信息及更新积分
 */
@FeignClient("member-service")
public interface UmsMemberService {

    /** 获取当前登录会员 */
    @GetMapping("/member/current")
    UmsMember getCurrentMember();

    /** 根据ID获取会员 */
    @GetMapping("/member/{id}")
    UmsMember getById(@PathVariable("id") Long id);

    /** 更新会员积分 */
    @PostMapping("/member/updateIntegration")
    void updateIntegration(@RequestParam Long id, @RequestParam Integer integration);
}
