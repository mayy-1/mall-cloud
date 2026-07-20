package com.mall.marketing.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.marketing.mapper.CmsSubjectProductRelationMapper;
import com.mall.marketing.model.CmsSubject;
import com.mall.marketing.model.CmsSubjectProductRelation;
import com.mall.marketing.service.ISubjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品专题管理Controller
 * Created by macro on 2018/6/1.
 */
@RestController
@Tag(name = "SubjectController", description = "商品专题管理")
@RequestMapping("/subject")
@RequiredArgsConstructor
public class SubjectController {
    /** 商品专题服务 */
    private final ISubjectService subjectService;
    /** 专题商品关联 Mapper */
    private final CmsSubjectProductRelationMapper subjectProductRelationMapper;

    @Operation(summary = "获取全部商品专题")
    @GetMapping("/listAll")
    public CommonResult<List<CmsSubject>> listAll() {
        List<CmsSubject> subjectList = subjectService.listAll();
        return CommonResult.success(subjectList);
    }

    @Operation(summary = "根据专题名称分页获取专题")
    @GetMapping("/list")
    public CommonResult<CommonPage<CmsSubject>> getList(@RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                        @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        List<CmsSubject> subjectList = subjectService.list(keyword, pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(subjectList));
    }

    /** 【Feign】根据商品ID查询专题关联 */
    @Operation(summary = "根据商品ID查询专题关联")
    @GetMapping("/productRelation/{productId}")
    public CommonResult<List<CmsSubjectProductRelation>> getProductRelations(@PathVariable Long productId) {
        CmsSubjectProductRelation condition = new CmsSubjectProductRelation();
        condition.setProductId(productId);
        return CommonResult.success(subjectProductRelationMapper.selectByCondition(condition));
    }
}
