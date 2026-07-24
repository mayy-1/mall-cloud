package com.mall.api.client.marketing;

import com.mall.api.dto.CmsSubjectProductRelationDTO;
import com.mall.api.dto.SubjectDTO;
import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专题服务 Feign 接口
 */
@FeignClient(name = "marketing-service", path = "/subject", contextId = "marketing-subject")
public interface SubjectClient {

    /** 获取前台展示专题 */
    @GetMapping("/listSome")
    CommonResult<List<SubjectDTO>> listSome();

    /** 分页搜索专题 */
    @GetMapping("/list")
    CommonResult<CommonPage<SubjectDTO>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                              @RequestParam("pageNum") Integer pageNum,
                                              @RequestParam("pageSize") Integer pageSize);

    /** 根据商品ID查询专题关联 */
    @GetMapping("/productRelation/{productId}")
    CommonResult<List<CmsSubjectProductRelationDTO>> getProductRelations(@PathVariable Long productId);

    /** 根据商品ID删除所有专题关联 */
    @DeleteMapping("/productRelation/product/{productId}")
    CommonResult<Void> deleteProductRelations(@PathVariable Long productId);

    /** 批量新增专题商品关联 */
    @PostMapping("/productRelation/batch")
    CommonResult<Void> batchInsertProductRelations(@RequestBody List<CmsSubjectProductRelationDTO> list);
}
