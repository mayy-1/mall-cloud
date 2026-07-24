package com.mall.product.controller;

import com.mall.api.dto.ProductDTO;
import com.mall.api.dto.ProductCategoryDTO;
import com.mall.product.domain.vo.HomeContentResult;
import com.mall.product.service.IHomeService;
import com.mym.mall.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台首页内容 Controller
 */
@RestController
@Tag(name = "HomeController", description = "前台首页内容管理")
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final IHomeService homeService;

    @Operation(summary = "首页内容页信息展示")
    @GetMapping("/content")
    public CommonResult<HomeContentResult> content() {
        HomeContentResult contentResult = homeService.content();
        return CommonResult.success(contentResult);
    }

    @Operation(summary = "猜你喜欢")
    @GetMapping("/recommendProductList")
    public CommonResult<List<ProductDTO>> recommendProductList(
            @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        return CommonResult.success(homeService.recommendProductList(pageSize, pageNum));
    }

    @Operation(summary = "商品分类")
    @GetMapping("/productCateList/{parentId}")
    public CommonResult<List<ProductCategoryDTO>> getProductCateList(@PathVariable Long parentId) {
        return CommonResult.success(homeService.getProductCateList(parentId));
    }

    @Operation(summary = "人气推荐")
    @GetMapping("/hotProductList")
    public CommonResult<List<ProductDTO>> hotProductList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        return CommonResult.success(homeService.hotProductList(pageNum, pageSize));
    }

    @Operation(summary = "新鲜好物")
    @GetMapping("/newProductList")
    public CommonResult<List<ProductDTO>> newProductList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        return CommonResult.success(homeService.newProductList(pageNum, pageSize));
    }
}
