package com.mall.member.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.mall.member.mapper.UmsIntegrationConsumeSettingMapper;
import com.mall.member.model.UmsIntegrationConsumeSetting;
import com.mym.mall.common.api.CommonResult;
import com.mall.member.model.UmsMember;
import com.mall.member.service.IMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 会员登录注册管理Controller、
 */
@RestController
@Tag(name = "MemberController", description = "会员登录注册管理")
@RequestMapping("/sso")
@RequiredArgsConstructor
public class MemberController {
    /** 会员业务服务 */
    private final IMemberService memberService;
    /** 积分消费设置Mapper */
    private final UmsIntegrationConsumeSettingMapper integrationConsumeSettingMapper;
    /** JWT Token前缀 */
    @Value("${sa-token.token-prefix}")
    private String tokenHead;

    @Operation(summary = "会员注册")
    @PostMapping("/register")
    public CommonResult register(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String telephone,
                                 @RequestParam String authCode) {
        memberService.register(username, password, telephone, authCode);
        return CommonResult.success(null,"注册成功");
    }

    @Operation(summary = "会员登录")
    @PostMapping("/login")
    public CommonResult login(@RequestParam String username,
                              @RequestParam String password) {
        SaTokenInfo saTokenInfo  = memberService.login(username, password);
        if (saTokenInfo  == null) {
            return CommonResult.validateFailed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", saTokenInfo.getTokenValue() );
        tokenMap.put("tokenHead", tokenHead+" ");
        return CommonResult.success(tokenMap);
    }

    @Operation(summary = "获取已登录会员信息")
    @GetMapping("/info")
    public CommonResult info() {
        UmsMember member = memberService.getCurrentMember();
        return CommonResult.success(member);
    }

    @Operation(summary = "通过Feign获取会员信息")
    @GetMapping("/{id}")
    public CommonResult getById(@PathVariable("id") Long id) {
        UmsMember member = memberService.getById(id);
        return CommonResult.success(member);
    }

    @Operation(summary = "登出功能")
    @PostMapping("/logout")
    public CommonResult logout() {
        memberService.logout();
        return CommonResult.success(null);
    }

    @Operation(summary = "获取验证码")
    @GetMapping("/getAuthCode")
    public CommonResult getAuthCode(@RequestParam String telephone) {
        memberService.generateAuthCode(telephone);
        return CommonResult.success("获取验证码成功");
    }

    @Operation(summary = "修改密码")
    @PostMapping("/updatePassword")
    public CommonResult updatePassword(@RequestParam String telephone,
                                 @RequestParam String password,
                                 @RequestParam String authCode) {
        memberService.updatePassword(telephone,password,authCode);
        return CommonResult.success(null,"密码修改成功");
    }
}
