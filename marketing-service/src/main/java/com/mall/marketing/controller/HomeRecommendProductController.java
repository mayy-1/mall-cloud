package com.mall.marketing.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.marketing.model.SmsHomeRecommendProduct;
import com.mall.marketing.service.IHomeRecommendProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页人气推荐管理Controller
 */
@RestController
@Tag(name = "HomeRecommendProductController", description = "首页人气推荐管理")
@RequestMapping("/home/recommendProduct")
@RequiredArgsConstructor
public class HomeRecommendProductController {
    /** 首页人气推荐服务 */
    private final IHomeRecommendProductService recommendProductService;

    @Operation(summary = "添加首页推荐")
    @PostMapping("/create")
    public CommonResult create(@RequestBody List<SmsHomeRecommendProduct> homeBrandList) {
        int count = recommendProductService.create(homeBrandList);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改推荐排序")
    @PostMapping("/update/sort/{id}")
    public CommonResult updateSort(@PathVariable Long id, Integer sort) {
        int count = recommendProductService.updateSort(id, sort);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "批量删除推荐")
    @PostMapping("/delete")
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        int count = recommendProductService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "批量修改推荐状态")
    @PostMapping("/update/recommendStatus")
    public CommonResult updateRecommendStatus(@RequestParam("ids") List<Long> ids, @RequestParam Integer recommendStatus) {
        int count = recommendProductService.updateRecommendStatus(ids, recommendStatus);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "分页查询推荐")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsHomeRecommendProduct>> list(@RequestParam(value = "productName", required = false) String productName,
                                                                  @RequestParam(value = "recommendStatus", required = false) Integer recommendStatus,
                                                                  @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<SmsHomeRecommendProduct> homeBrandList = recommendProductService.list(productName, recommendStatus, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(homeBrandList));
    }

    /** 【Feign】获取所有启用的人气推荐商品ID */
    @Operation(summary = "获取启用人气推荐商品ID")
    @GetMapping("/activeIds")
    public CommonResult<List<Long>> getActiveProductIds() {
        return CommonResult.success(recommendProductService.getActiveProductIds());
    }
}
