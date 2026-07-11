package com.mall.marketing.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.marketing.model.CmsPrefrenceArea;
import com.mall.marketing.service.IPrefrenceAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 商品优选管理Controller
 * Created by macro on 2018/6/1.
 */
@RestController
@Tag(name = "PrefrenceAreaController", description = "商品优选管理")
@RequestMapping("/prefrenceArea")
@RequiredArgsConstructor
public class PrefrenceAreaController {
    /** 商品优选服务 */
    private final IPrefrenceAreaService prefrenceAreaService;

    @Operation(summary = "获取所有商品优选")
    @GetMapping("/listAll")
    public CommonResult<List<CmsPrefrenceArea>> listAll() {
        List<CmsPrefrenceArea> prefrenceAreaList = prefrenceAreaService.listAll();
        return CommonResult.success(prefrenceAreaList);
    }
}
