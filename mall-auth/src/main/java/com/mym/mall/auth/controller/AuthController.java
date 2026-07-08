package com.mym.mall.auth.controller;

import com.mall.api.client.MemberClient;
import com.mall.api.client.UserClient;
import com.mym.mall.common.api.CommonResult;
import com.mym.mall.common.constant.AuthConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一认证授权接口
 * 根据 clientId 区分后台管理员和前台会员，分别调用 user-service / member-service 完成登录
 */
@RestController
@Tag(name = "AuthController", description = "统一认证授权接口")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 后台用户 Feign（user-service） */
    private final UserClient userClient;
    /** 前台会员 Feign（member-service） */
    private final MemberClient memberClient;

    @Operation(summary = "登录以后返回token")
    @PostMapping("/login")
    public CommonResult<Map<String, String>> login(@RequestParam String clientId,
                                                    @RequestParam String username,
                                                    @RequestParam String password) {
        if (AuthConstant.ADMIN_CLIENT_ID.equals(clientId)) {
            Map<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("password", password);
            return userClient.login(params);
        } else if (AuthConstant.PORTAL_CLIENT_ID.equals(clientId)) {
            return memberClient.login(username, password);
        } else {
            return CommonResult.failed("clientId不正确");
        }
    }
}
