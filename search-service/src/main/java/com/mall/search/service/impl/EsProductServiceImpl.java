package com.mall.search.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.mall.search.mapper.EsProductMapper;
import com.mall.search.domain.EsProduct;
import com.mall.search.repository.EsProductRepository;
import com.mall.search.service.IEsProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索商品管理Service实现类
 * <p>
 * 核心职责：fullSync 全量同步 + 综合搜索；增量同步由 CanalProductReceiver（MQ 消费）调用 create/delete。
 */
@Service
@RequiredArgsConstructor
public class EsProductServiceImpl implements IEsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductServiceImpl.class);

    /** 商品搜索自定义Mapper（直连 mall_product 库） */
    private final EsProductMapper productMapper;
    /** ES商品Repository */
    private final EsProductRepository productRepository;
    /** ES操作模板 */
    private final ElasticsearchTemplate elasticsearchTemplate;

    /** 全量同步：从数据库导入全部上架商品到 ES */
    @Override
    public int fullSync() {
        List<EsProduct> esProductList = productMapper.getAllEsProductList(null);
        Iterable<EsProduct> esProductIterable = productRepository.saveAll(esProductList);
        Iterator<EsProduct> iterator = esProductIterable.iterator();
        int result = 0;
        while (iterator.hasNext()) {
            result++;
            iterator.next();
        }
        LOGGER.info("商品全量同步完成，共 {} 条", result);
        return result;
    }

    /** 根据 ID 从 ES 查询单个商品文档（内部调试） */
    @Override
    public EsProduct getEsProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    /** 根据 ID 删除 ES 中的商品 */
    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    /** 根据 ID 创建 ES 商品索引（重新查 MySQL 组装完整文档 upsert） */
    @Override
    public EsProduct create(Long id) {
        EsProduct result = null;
        List<EsProduct> esProductList = productMapper.getAllEsProductList(id);
        if (esProductList.size() > 0) {
            EsProduct esProduct = esProductList.get(0);
            result = productRepository.save(esProduct);
        }
        return result;
    }

    /** 根据 ID 列表批量删除 ES 商品 */
    @Override
    public void delete(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            List<EsProduct> esProductList = new ArrayList<>();
            for (Long id : ids) {
                EsProduct esProduct = new EsProduct();
                esProduct.setId(id);
                esProductList.add(esProduct);
            }
            productRepository.deleteAll(esProductList);
        }
    }

    /** 综合搜索：关键词+品牌/分类筛选+排序 */
    @Override
    public Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId,
                                  Integer pageNum, Integer pageSize, Integer sort) {
        // 入参为 1-based 分页（与前端/PageHelper 语义一致）；PageRequest 为 0-based，需 -1
        int page = (pageNum == null || pageNum < 1) ? 0 : pageNum - 1;
        int size = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        Pageable pageable = PageRequest.of(page, size);
        NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder();
        // 分页
        nativeQueryBuilder.withPageable(pageable);
        // 过滤：品牌 / 分类走 filter 上下文（精确、不算分）
        if (brandId != null || productCategoryId != null) {
            Query boolQuery = QueryBuilders.bool(builder -> {
                if (brandId != null) {
                    builder.must(QueryBuilders.term(b -> b.field("brandId").value(brandId)));
                }
                if (productCategoryId != null) {
                    builder.must(QueryBuilders.term(b -> b.field("productCategoryId").value(productCategoryId)));
                }
                return builder;
            });
            nativeQueryBuilder.withFilter(boolQuery);
        }
        if (StrUtil.isEmpty(keyword)) {
            nativeQueryBuilder.withQuery(QueryBuilders.matchAll(builder -> builder));
        } else {
            List<FunctionScore> functionScoreList = new ArrayList<>();
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("name").query(keyword)))
                    .weight(10.0)
                    .build());
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("brandName").query(keyword)))
                    .weight(6.0)
                    .build());
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("subTitle").query(keyword)))
                    .weight(5.0)
                    .build());
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("productCategoryName").query(keyword)))
                    .weight(3.0)
                    .build());
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("keywords").query(keyword)))
                    .weight(2.0)
                    .build());
            FunctionScoreQuery.Builder functionScoreQueryBuilder = QueryBuilders.functionScore()
                    .functions(functionScoreList)
                    .scoreMode(FunctionScoreMode.Sum)
                    .minScore(2.0);
            nativeQueryBuilder.withQuery(builder -> builder.functionScore(functionScoreQueryBuilder.build()));
        }
        // 排序
        if (sort == null) {
            sort = 0;
        }
        if (sort == 1) {
            // 按新品从新到旧
            nativeQueryBuilder.withSort(Sort.by(Sort.Order.desc("id")));
        } else if (sort == 2) {
            // 按销量从高到低
            nativeQueryBuilder.withSort(Sort.by(Sort.Order.desc("sale")));
        } else if (sort == 3) {
            // 按价格从低到高
            nativeQueryBuilder.withSort(Sort.by(Sort.Order.asc("price")));
        } else if (sort == 4) {
            // 按价格从高到低
            nativeQueryBuilder.withSort(Sort.by(Sort.Order.desc("price")));
        }
        // 按相关度（sort=0 时以 _score 兜底排序）
        nativeQueryBuilder.withSort(Sort.by(Sort.Order.desc("_score")));
        NativeQuery nativeQuery = nativeQueryBuilder.build();
        SearchHits<EsProduct> searchHits = elasticsearchTemplate.search(nativeQuery, EsProduct.class);
        if (searchHits.getTotalHits() <= 0) {
            return new PageImpl<>(ListUtil.empty(), pageable, 0);
        }
        List<EsProduct> searchProductList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return new PageImpl<>(searchProductList, pageable, searchHits.getTotalHits());
    }
}
