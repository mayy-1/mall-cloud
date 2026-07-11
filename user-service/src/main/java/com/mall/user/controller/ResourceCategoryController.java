package com.mall.user.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.user.model.UmsResourceCategory;
import com.mall.user.service.IResourceCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台资源分类管理Controller
 */
@RestController
@Tag(name = "ResourceCategoryController", description = "后台资源分类管理")
@RequestMapping("/resourceCategory")
@RequiredArgsConstructor
public class ResourceCategoryController {

    /** 资源分类服务 */
    private final IResourceCategoryService resourceCategoryService;

    @Operation(summary = "查询所有后台资源分类")
    @GetMapping("/listAll")
    public CommonResult<List<UmsResourceCategory>> listAll() {
        List<UmsResourceCategory> resourceList = resourceCategoryService.listAll();
        return CommonResult.success(resourceList);
    }
}
