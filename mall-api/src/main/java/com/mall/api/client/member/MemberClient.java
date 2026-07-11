package com.mall.api.client.member;

import com.mall.api.dto.IntegrationConsumeSettingDTO;
import com.mall.api.dto.MemberDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 会员服务 Feign 接口
 */
@FeignClient(name = "member-service")
public interface MemberClient {

    /** 获取当前登录会员 */
    @GetMapping("/member/current")
    CommonResult<MemberDTO> getCurrentMember();

    /** 根据ID查询会员信息 */
    @GetMapping("/member/{id}")
    CommonResult<MemberDTO> getById(@PathVariable Long id);

    /** 更新会员积分 */
    @PostMapping("/member/updateIntegration")
    void updateIntegration(@RequestParam Long id, @RequestParam Integer integration);

    /** 获取积分消费设置 */
    @GetMapping("/member/integrationConsumeSetting")
    CommonResult<IntegrationConsumeSettingDTO> getIntegrationConsumeSetting();

    /** 前台会员登录，返回 token */
    @PostMapping("/sso/login")
    CommonResult<Map<String, String>> login(@RequestParam String username,
                                            @RequestParam String password);
}
