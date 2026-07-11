package com.mall.member.controller;

import com.mall.member.mapper.UmsIntegrationConsumeSettingMapper;
import com.mall.member.model.UmsIntegrationConsumeSetting;
import com.mall.member.model.UmsMember;
import com.mall.member.service.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 会员内部服务 Feign 接口 Controller
 *
 * <p>暴露 /member 路径端点供 cart-service、trade-service 等内部微服务通过 Feign 调用，
 * 实现跨服务的会员信息获取和积分更新，避免直接访问 member-service 的数据库。</p>
 *
 * <p>这些接口不是对外暴露的 API，不经过网关鉴权，仅供服务间内部通信。</p>
 *
 * @see com.mall.api.client.MemberClient     mall-api 中的 Feign 接口定义
 * @see com.mall.trade.feign                 trade-service 中的本地 Feign 调用端（已弃用，统一使用 mall-api）
 * @see com.mall.cart.feign                  cart-service 中的本地 Feign 调用端（已弃用，统一使用 mall-api）
 */
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberFeignController {

    /** 会员业务服务 */
    private final IMemberService memberService;
    /** 积分消费设置Mapper */
    private final UmsIntegrationConsumeSettingMapper integrationConsumeSettingMapper;

    /**
     * 获取当前登录会员信息
     *
     * <p>从 Sa-Token Session 中读取当前登录会员的 ID，查询会员详情后返回。
     * 调用方：cart-service 的 CartServiceImpl.add() 方法，用于获取会员昵称。</p>
     *
     * @return 当前登录会员的完整信息
     */
    @GetMapping("/current")
    public UmsMember getCurrentMember() {
        return memberService.getCurrentMember();
    }

    /**
     * 根据会员 ID 查询会员信息
     *
     * <p>直接通过主键查询会员，不依赖登录状态。
     * 调用方：trade-service 的 OrderServiceImpl，用于订单取消/超时处理时查询会员积分。</p>
     *
     * @param id 会员 ID
     * @return 会员信息
     */
    @GetMapping("/{id}")
    public UmsMember getById(@PathVariable("id") Long id) {
        return memberService.getById(id);
    }

    /**
     * 更新会员积分
     *
     * <p>用于下单扣减积分或订单退款恢复积分。直接修改数据库中的 integration 字段。
     * 调用方：trade-service 的 OrderServiceImpl，在下单、订单超时、取消订单时调用。</p>
     *
     * @param id          会员 ID
     * @param integration 更新后的积分值
     */
    @PostMapping("/updateIntegration")
    public void updateIntegration(@RequestParam Long id, @RequestParam Integer integration) {
        memberService.updateIntegration(id, integration);
    }

    /**
     * 获取积分消费设置
     *
     * <p>返回积分抵扣规则配置（抵扣比例、最小单位、优惠券共用设置等）。
     * 调用方：trade-service 的 OrderServiceImpl，用于下单时计算积分抵扣金额。</p>
     *
     * @return 积分消费设置
     */
    @GetMapping("/integrationConsumeSetting")
    public UmsIntegrationConsumeSetting getIntegrationConsumeSetting() {
        return integrationConsumeSettingMapper.selectByPrimaryKey(1L);
    }
}
