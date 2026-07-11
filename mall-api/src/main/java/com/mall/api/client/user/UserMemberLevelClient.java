package com.mall.api.client.user;

import com.mall.api.dto.MemberLevelDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户服务 - 会员等级 Feign 接口
 */
@FeignClient(name = "user-service", contextId = "user-member-level")
public interface UserMemberLevelClient {

    /**
     * 根据默认状态查询会员等级列表
     *
     * @param defaultStatus 是否为默认等级：1->是，0->不是
     * @return 会员等级列表
     */
    @GetMapping("/memberLevel/list")
    CommonResult<List<MemberLevelDTO>> listByDefaultStatus(@RequestParam("defaultStatus") Integer defaultStatus);
}
