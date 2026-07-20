package com.mall.api.client.user;

import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 用户权限服务 Feign 接口（后台管理员）
 */
@FeignClient(name = "user-service", path = "/admin", contextId = "user-admin")
public interface UserClient {

    /** 根据ID查询管理员 */
    @GetMapping("/{id}")
    CommonResult<?> getById(@PathVariable Long id);

    /** 根据用户名加载管理员信息 */
    @GetMapping("/loadByUsername")
    CommonResult<?> loadByUsername(@RequestParam String username);

    /** 后台管理员登录，返回 token */
    @PostMapping("/login")
    CommonResult<Map<String, String>> login(@RequestBody Map<String, String> params);
}
