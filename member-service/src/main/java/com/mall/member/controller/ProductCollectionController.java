package com.mall.member.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.member.domain.MemberProductCollection;
import com.mall.member.service.IProductCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 会员收藏管理Controller
 * Created by macro on 2018/8/2.
 */
@Controller
@Tag(name = "ProductCollectionController", description = "会员收藏管理")
@RequestMapping("/member/productCollection")
@RequiredArgsConstructor
public class ProductCollectionController {
    /** 商品收藏业务服务 */
    private final IProductCollectionService productCollectionService;

    @Operation(summary = "添加商品收藏")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult add(@RequestBody MemberProductCollection productCollection) {
        int count = productCollectionService.add(productCollection);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary = "删除收藏商品")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(Long productId) {
        int count = productCollectionService.delete(productId);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary = "显示收藏列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<MemberProductCollection>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                                  @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<MemberProductCollection> page = productCollectionService.list(pageNum,pageSize);
        return CommonResult.success(CommonPage.restPage(page));
    }

    @Operation(summary = "显示收藏商品详情")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<MemberProductCollection> detail(@RequestParam Long productId) {
        MemberProductCollection memberProductCollection = productCollectionService.detail(productId);
        return CommonResult.success(memberProductCollection);
    }

    @Operation(summary = "清空收藏列表")
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult clear() {
        productCollectionService.clear();
        return CommonResult.success(null);
    }
}
