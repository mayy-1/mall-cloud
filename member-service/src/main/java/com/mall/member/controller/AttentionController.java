package com.mall.member.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.member.domain.MemberBrandAttention;
import com.mall.member.service.IAttentionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 会员关注品牌管理Controller
 * Created by macro on 2018/8/2.
 */
@Controller
@Tag(name = "AttentionController", description = "会员关注品牌管理")
@RequestMapping("/member/attention")
@RequiredArgsConstructor
public class AttentionController {
    /** 品牌关注业务服务 */
    private final IAttentionService attentionService;

    @Operation(summary = "添加品牌关注")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult add(@RequestBody MemberBrandAttention memberBrandAttention) {
        int count = attentionService.add(memberBrandAttention);
        if(count>0){
            return CommonResult.success(count);
        }else{
            return CommonResult.failed();
        }
    }

    @Operation(summary = "取消关注")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(Long brandId) {
        int count = attentionService.delete(brandId);
        if(count>0){
            return CommonResult.success(count);
        }else{
            return CommonResult.failed();
        }
    }

    @Operation(summary = "显示关注列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<MemberBrandAttention>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                               @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<MemberBrandAttention> page = attentionService.list(pageNum,pageSize);
        return CommonResult.success(CommonPage.restPage(page));
    }

    @Operation(summary = "显示关注品牌详情")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<MemberBrandAttention> detail(@RequestParam Long brandId) {
        MemberBrandAttention memberBrandAttention = attentionService.detail(brandId);
        return CommonResult.success(memberBrandAttention);
    }

    @Operation(summary = "清空关注列表")
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult clear() {
        attentionService.clear();
        return CommonResult.success(null);
    }
}
