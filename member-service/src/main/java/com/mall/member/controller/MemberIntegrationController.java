package com.mall.member.controller;

import com.mall.member.mapper.UmsIntegrationConsumeSettingMapper;
import com.mall.member.model.UmsIntegrationConsumeSetting;
import com.mall.member.service.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 会员积分内部 Feign Controller
 *
 * <p>提供积分扣减、积分规则查询等端点，
 * 供 trade-service 在下单、取消订单、超时处理时通过 Feign 调用。</p>
 */
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberIntegrationController {

    private final IMemberService memberService;
    private final UmsIntegrationConsumeSettingMapper integrationConsumeSettingMapper;

    /**
     * 更新会员积分（同时记录变动历史）
     *
     * @param id          会员 ID
     * @param integration 更新后的积分值
     * @param sourceType  积分来源：0=购物，1=管理员修改
     */
    @PostMapping("/updateIntegration")
    public void updateIntegration(@RequestParam Long id,
                                  @RequestParam Integer integration,
                                  @RequestParam(defaultValue = "0") Integer sourceType) {
        memberService.updateIntegration(id, integration, sourceType);
    }

    /**
     * 获取积分消费设置（抵扣比例、最小单位、能否与优惠券共用）
     */
    @GetMapping("/integrationConsumeSetting")
    public UmsIntegrationConsumeSetting getIntegrationConsumeSetting() {
        return integrationConsumeSettingMapper.selectByPrimaryKey(1L);
    }
}
