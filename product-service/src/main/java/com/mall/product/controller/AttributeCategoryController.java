package com.mall.product.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.product.domain.dto.PmsProductAttributeCategoryItem;
import com.mall.product.model.PmsProductAttributeCategory;
import com.mall.product.service.IAttributeCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品属性分类管理 Controller
 * 【管理端专用】所有接口均面向后台管理，用于商品属性分类的增删改查。
 * 用户端不直接调用此类接口。
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "AttributeCategoryController", description = "商品属性分类管理")
@RequestMapping("/productAttribute/category")
public class AttributeCategoryController {
    private final IAttributeCategoryService productAttributeCategoryService;

    /** 【管理端】添加商品属性分类 */
    @PostMapping("/create")
    public CommonResult create(@RequestParam String name) {
        int count = productAttributeCategoryService.create(name);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary = "修改商品属性分类")
    // 【管理端】
    @PostMapping("/update/{id}")
    public CommonResult update(@PathVariable Long id, @RequestParam String name) {
        int count = productAttributeCategoryService.update(id, name);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /** 【管理端】删除单个商品属性分类 */
    @Operation(summary = "删除单个商品属性分类")
    @GetMapping("/delete/{id}")
    public CommonResult delete(@PathVariable Long id) {
        int count = productAttributeCategoryService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /** 【管理端】获取单个商品属性分类信息 */
    @Operation(summary = "获取单个商品属性分类信息")
    @GetMapping("/{id}")
    public CommonResult<PmsProductAttributeCategory> getItem(@PathVariable Long id) {
        PmsProductAttributeCategory productAttributeCategory = productAttributeCategoryService.getItem(id);
        return CommonResult.success(productAttributeCategory);
    }

    /** 【管理端】分页获取所有商品属性分类 */
    @Operation(summary = "分页获取所有商品属性分类")
    @GetMapping("/list")
    public CommonResult<CommonPage<PmsProductAttributeCategory>> getList(@RequestParam(defaultValue = "5") Integer pageSize, @RequestParam(defaultValue = "1") Integer pageNum) {
        List<PmsProductAttributeCategory> productAttributeCategoryList = productAttributeCategoryService.getList(pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(productAttributeCategoryList));
    }

    /** 【管理端】获取所有商品属性分类及其下属性 */
    @Operation(summary = "获取所有商品属性分类及其下属性")
    @GetMapping("/list/withAttr")
    public CommonResult<List<PmsProductAttributeCategoryItem>> getListWithAttr() {
        List<PmsProductAttributeCategoryItem> productAttributeCategoryResultList = productAttributeCategoryService.getListWithAttr();
        return CommonResult.success(productAttributeCategoryResultList);
    }
}
