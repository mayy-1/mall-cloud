package com.mall.marketing.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.marketing.model.SmsHomeNewProduct;
import com.mall.marketing.service.IHomeNewProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页新品管理Controller
 */
@RestController
@Tag(name = "HomeNewProductController", description = "首页新品管理")
@RequestMapping("/home/newProduct")
@RequiredArgsConstructor
public class HomeNewProductController {
    /** 首页新品服务 */
    private final IHomeNewProductService homeNewProductService;

    @Operation(summary = "添加首页推荐品牌")
    @PostMapping("/create")
    public CommonResult create(@RequestBody List<SmsHomeNewProduct> homeBrandList) {
        int count = homeNewProductService.create(homeBrandList);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改推荐排序")
    @PostMapping("/update/sort/{id}")
    public CommonResult updateSort(@PathVariable Long id, Integer sort) {
        int count = homeNewProductService.updateSort(id, sort);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "批量删除推荐")
    @PostMapping("/delete")
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        int count = homeNewProductService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "批量修改推荐状态")
    @PostMapping("/update/recommendStatus")
    public CommonResult updateRecommendStatus(@RequestParam("ids") List<Long> ids, @RequestParam Integer recommendStatus) {
        int count = homeNewProductService.updateRecommendStatus(ids, recommendStatus);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "分页查询推荐")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsHomeNewProduct>> list(@RequestParam(value = "productName", required = false) String productName,
                                                            @RequestParam(value = "recommendStatus", required = false) Integer recommendStatus,
                                                            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<SmsHomeNewProduct> homeBrandList = homeNewProductService.list(productName, recommendStatus, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(homeBrandList));
    }

    /** 【Feign】获取所有启用的首页新品商品ID */
    @Operation(summary = "获取启用的首页新品商品ID")
    @GetMapping("/activeIds")
    public CommonResult<List<Long>> getActiveProductIds() {
        return CommonResult.success(homeNewProductService.getActiveProductIds());
    }
}
