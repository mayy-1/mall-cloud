package com.mall.search.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.search.domain.EsProduct;
import com.mall.search.service.IEsProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索商品管理Controller（对外接口收敛）
 * <p>
 * 对外只暴露：
 * 1. {@code POST /fullSync} 全量同步（手动初始化 / 兜底，返回同步条数）
 * 2. {@code GET  /search}    用户端商品综合搜索（关键词/分类/品牌 + 排序分页）
 * 3. {@code GET  /{id}}      按 id 查 ES 单条文档（内部调试）
 * 4. {@code DELETE /{id}}    按 id 删除 ES 文档（内部调试/测试）
 * <p>
 * 增量（增/改/删）不提供 HTTP，由 search-service 内部消费 Canal → MQ 消息完成。
 */
@RestController
@Tag(name = "EsProductController", description = "商品搜索管理")
@RequestMapping("/esProduct")
@RequiredArgsConstructor
public class EsProductController {

    private final IEsProductService esProductService;

    @Operation(summary = "全量同步：导入所有上架商品到ES（手动初始化/兜底）")
    @PostMapping("/fullSync")
    public CommonResult<Integer> fullSync() {
        int count = esProductService.fullSync();
        return CommonResult.success(count, "全量同步成功，共 " + count + " 条");
    }

    @Operation(summary = "商品综合搜索（关键词/分类/品牌 + 排序分页）")
    @Parameter(name = "sort", description = "排序：0-相关度；1-新品；2-销量；3-价格升；4-价格降",
            in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0"))
    @GetMapping("/search")
    public CommonResult<CommonPage<EsProduct>> search(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) Long brandId,
                                                      @RequestParam(required = false) Long productCategoryId,
                                                      @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                      @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                                      @RequestParam(required = false, defaultValue = "0") Integer sort) {
        Page<EsProduct> esProductPage = esProductService.search(keyword, brandId, productCategoryId, pageNum, pageSize, sort);
        return CommonResult.success(CommonPage.restPage(esProductPage));
    }

    @Operation(summary = "根据id查询ES商品文档（内部调试/排查）")
    @GetMapping("/{id}")
    public CommonResult<EsProduct> getById(@PathVariable Long id) {
        EsProduct esProduct = esProductService.getEsProductById(id);
        if (esProduct != null) {
            return CommonResult.success(esProduct);
        }
        return CommonResult.failed("ES中不存在该商品, id=" + id);
    }

    @Operation(summary = "根据id删除ES商品文档（调试/测试用，生产增量删除走MQ消费）")
    @DeleteMapping("/{id}")
    public CommonResult<Object> delete(@PathVariable Long id) {
        esProductService.delete(id);
        return CommonResult.success(null, "删除成功");
    }
}
