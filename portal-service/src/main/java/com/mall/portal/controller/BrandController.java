package com.mall.portal.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.portal.model.PmsBrand;
import com.mall.portal.model.PmsProduct;
import com.mall.portal.service.IBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页品牌推荐管理Controller
 * Created by macro on 2020/5/15.
 */
@RestController
@Tag(name = "BrandController", description = "前台品牌管理")
@RequestMapping("/brand")
@RequiredArgsConstructor
public class BrandController {

    /** 品牌业务服务 */
    private final IBrandService brandService;

    @Operation(summary = "分页获取推荐品牌")
    @GetMapping("/recommendList")
    public CommonResult<List<PmsBrand>> recommendList(@RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<PmsBrand> brandList = brandService.recommendList(pageNum, pageSize);
        return CommonResult.success(brandList);
    }

    @Operation(summary = "获取品牌详情")
    @GetMapping("/detail/{brandId}")
    public CommonResult<PmsBrand> detail(@PathVariable Long brandId) {
        PmsBrand brand = brandService.detail(brandId);
        return CommonResult.success(brand);
    }

    @Operation(summary = "分页获取品牌相关商品")
    @GetMapping("/productList")
    public CommonResult<CommonPage<PmsProduct>> productList(@RequestParam Long brandId,
                                                            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        CommonPage<PmsProduct> result = brandService.productList(brandId, pageNum, pageSize);
        return CommonResult.success(result);
    }
}
