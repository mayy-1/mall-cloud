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

    @Operation(summary = "后台：获取全部商品专题")
    @GetMapping("/listAll")
    public CommonResult<List<CmsSubject>> listAll() {
        List<CmsSubject> subjectList = subjectService.listAll();
        return CommonResult.success(subjectList);
    }

    @Operation(summary = "前台：获取推荐+上架商品专题")
    @GetMapping("/listSome")
    public CommonResult<List<CmsSubject>> listSome() {
        List<CmsSubject> subjectList = subjectService.listSome();
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

    @Operation(summary = "新增专题")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody CmsSubject subject) {
        int count = subjectService.create(subject);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    @Operation(summary = "修改专题")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody CmsSubject subject) {
        int count = subjectService.update(id, subject);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    @Operation(summary = "删除专题")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = subjectService.delete(id);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /** 【Feign】根据商品ID查询专题关联 */
    @Operation(summary = "根据商品ID查询专题关联")
    @GetMapping("/productRelation/{productId}")
    public CommonResult<List<CmsSubjectProductRelation>> getProductRelations(@PathVariable Long productId) {
        CmsSubjectProductRelation condition = new CmsSubjectProductRelation();
        condition.setProductId(productId);
        return CommonResult.success(subjectProductRelationMapper.selectByCondition(condition));
    }

    /** 【Feign】根据商品ID删除所有专题关联 */
    @Operation(summary = "根据商品ID删除专题关联")
    @DeleteMapping("/productRelation/product/{productId}")
    public CommonResult<Void> deleteByProductId(@PathVariable Long productId) {
        CmsSubjectProductRelation condition = new CmsSubjectProductRelation();
        condition.setProductId(productId);
        subjectProductRelationMapper.deleteByCondition(condition);
        return CommonResult.success(null);
    }

    /** 【Feign】批量新增专题商品关联 */
    @Operation(summary = "批量新增专题商品关联")
    @PostMapping("/productRelation/batch")
    public CommonResult<Void> batchInsert(@RequestBody List<CmsSubjectProductRelation> list) {
        if (list != null && !list.isEmpty()) {
            subjectProductRelationMapper.insertList(list);
        }
        return CommonResult.success(null);
    }
}
