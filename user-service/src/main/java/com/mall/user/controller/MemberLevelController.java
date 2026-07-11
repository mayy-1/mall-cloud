package com.mall.user.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.user.model.UmsMemberLevel;
import com.mall.user.service.IMemberLevelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会员等级管理Controller
 */
@RestController
@Tag(name = "MemberLevelController", description = "会员等级管理")
@RequestMapping("/memberLevel")
@RequiredArgsConstructor
public class MemberLevelController {

    /** 会员等级服务 */
    private final IMemberLevelService memberLevelService;

    @GetMapping("/list")
    @Operation(summary = "查询所有会员等级")
    public CommonResult<List<UmsMemberLevel>> list(@RequestParam("defaultStatus") Integer defaultStatus) {
        List<UmsMemberLevel> memberLevelList = memberLevelService.list(defaultStatus);
        return CommonResult.success(memberLevelList);
    }
}
