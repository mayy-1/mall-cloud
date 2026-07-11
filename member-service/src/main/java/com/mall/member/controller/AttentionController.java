package com.mall.member.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.member.domain.MemberBrandAttention;
import com.mall.member.service.IAttentionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 会员关注品牌管理Controller
 */
@RestController
@Tag(name = "AttentionController", description = "会员关注品牌管理")
@RequestMapping("/member/attention")
@RequiredArgsConstructor
public class AttentionController {
    /** 品牌关注业务服务 */
    private final IAttentionService attentionService;

    @Operation(summary = "添加品牌关注")
    @PostMapping("/add")
    public CommonResult add(@RequestBody MemberBrandAttention memberBrandAttention) {
        int count = attentionService.add(memberBrandAttention);
        if(count>0){
            return CommonResult.success(count);
        }else{
            return CommonResult.failed();
        }
    }

    @Operation(summary = "取消关注")
    @PostMapping("/delete")
    public CommonResult delete(Long brandId) {
        int count = attentionService.delete(brandId);
        if(count>0){
            return CommonResult.success(count);
        }else{
            return CommonResult.failed();
        }
    }

    @Operation(summary = "显示关注列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<MemberBrandAttention>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                               @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<MemberBrandAttention> page = attentionService.list(pageNum,pageSize);
        return CommonResult.success(CommonPage.restPage(page));
    }

    @Operation(summary = "显示关注品牌详情")
    @GetMapping("/detail")
    public CommonResult<MemberBrandAttention> detail(@RequestParam Long brandId) {
        MemberBrandAttention memberBrandAttention = attentionService.detail(brandId);
        return CommonResult.success(memberBrandAttention);
    }

    @Operation(summary = "清空关注列表")
    @PostMapping("/clear")
    public CommonResult clear() {
        attentionService.clear();
        return CommonResult.success(null);
    }
}
