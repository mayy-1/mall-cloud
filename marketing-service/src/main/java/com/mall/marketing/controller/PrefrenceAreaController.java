package com.mall.marketing.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.marketing.mapper.CmsPrefrenceAreaProductRelationMapper;
import com.mall.marketing.model.CmsPrefrenceArea;
import com.mall.marketing.model.CmsPrefrenceAreaProductRelation;
import com.mall.marketing.service.IPrefrenceAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品优选管理Controller
 */
@RestController
@Tag(name = "PrefrenceAreaController", description = "商品优选管理")
@RequestMapping("/prefrenceArea")
@RequiredArgsConstructor
public class PrefrenceAreaController {
    /** 商品优选服务 */
    private final IPrefrenceAreaService prefrenceAreaService;
    /** 优选商品关联 Mapper */
    private final CmsPrefrenceAreaProductRelationMapper areaProductRelationMapper;

    @Operation(summary = "获取所有商品优选")
    @GetMapping("/listAll")
    public CommonResult<List<CmsPrefrenceArea>> listAll() {
        return CommonResult.success(prefrenceAreaService.listAll());
    }

    /** 【Feign】根据商品ID查询优选专区关联 */
    @Operation(summary = "根据商品ID查询优选专区关联")
    @GetMapping("/productRelation/{productId}")
    public CommonResult<List<CmsPrefrenceAreaProductRelation>> getProductRelations(@PathVariable Long productId) {
        CmsPrefrenceAreaProductRelation condition = new CmsPrefrenceAreaProductRelation();
        condition.setProductId(productId);
        return CommonResult.success(areaProductRelationMapper.selectByCondition(condition));
    }
}
