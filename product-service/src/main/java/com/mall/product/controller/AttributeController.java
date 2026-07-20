package com.mall.product.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.product.domain.dto.PmsProductAttributeParam;
import com.mall.product.domain.dto.ProductAttrInfo;
import com.mall.product.model.PmsProductAttribute;
import com.mall.product.service.IAttributeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品属性管理 Controller
 * 【管理端专用】所有接口均面向后台管理，用于商品属性/参数的创建、修改、删除和查询。
 * 属性/参数值作为商品编辑时的备选项，由管理后台维护，用户端不直接调用。
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "AttributeController", description = "商品属性管理")
@RequestMapping("/productAttribute")
public class AttributeController {
    private final IAttributeService productAttributeService;

    /** 【管理端】根据分类查询属性列表或参数列表（type=0属性，type=1参数） */
    @Operation(summary = "根据分类查询属性列表或参数列表")
    @Parameters({@Parameter(name = "type", description = "0表示属性，1表示参数", required = true,in = ParameterIn.QUERY, schema = @Schema(type = "integer"))})
    @GetMapping("/list/{cid}")
    public CommonResult<CommonPage<PmsProductAttribute>> getList(@PathVariable Long cid,
                                                                 @RequestParam(value = "type") Integer type,
                                                                 @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<PmsProductAttribute> productAttributeList = productAttributeService.getList(cid, type, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(productAttributeList));
    }

    /** 【管理端】添加商品属性信息 */
    @Operation(summary = "添加商品属性信息")
    @PostMapping("/create")
    public CommonResult create(@RequestBody PmsProductAttributeParam productAttributeParam) {
        int count = productAttributeService.create(productAttributeParam);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /** 【管理端】修改商品属性信息 */
    @Operation(summary = "修改商品属性信息")
    @PostMapping("/update/{id}")
    public CommonResult update(@PathVariable Long id, @RequestBody PmsProductAttributeParam productAttributeParam) {
        int count = productAttributeService.update(id, productAttributeParam);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /** 【管理端】查询单个商品属性 */
    @Operation(summary = "查询单个商品属性")
    @GetMapping("/{id}")
    public CommonResult<PmsProductAttribute> getItem(@PathVariable Long id) {
        PmsProductAttribute productAttribute = productAttributeService.getItem(id);
        return CommonResult.success(productAttribute);
    }

    /** 【管理端】批量删除商品属性 */
    @Operation(summary = "批量删除商品属性")
    @PostMapping("/delete")
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        int count = productAttributeService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /** 【管理端】根据商品分类ID获取商品属性及属性分类（商品编辑时用） */
    @Operation(summary = "根据商品分类的id获取商品属性及属性分类")
    @GetMapping("/attrInfo/{productCategoryId}")
    public CommonResult<List<ProductAttrInfo>> getAttrInfo(@PathVariable Long productCategoryId) {
        List<ProductAttrInfo> productAttrInfoList = productAttributeService.getProductAttrInfo(productCategoryId);
        return CommonResult.success(productAttrInfoList);
    }
}
