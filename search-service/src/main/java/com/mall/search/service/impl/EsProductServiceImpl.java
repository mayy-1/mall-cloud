package com.mall.search.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.mall.search.mapper.EsProductMapperCustom;
import com.mall.search.domain.EsProduct;
import com.mall.search.domain.EsProductRelatedInfo;
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
 */
@Service
@RequiredArgsConstructor
public class EsProductServiceImpl implements IEsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductServiceImpl.class);
    /** 商品搜索自定义Mapper */
    private final EsProductMapperCustom productMapper;
    /** ES商品Repository */
    private final EsProductRepository productRepository;
    /** ES操作模板 */
    private final ElasticsearchTemplate elasticsearchTemplate;
    /** 从数据库导入全部商品到ES */
    @Override
    public int importAll() {
        List<EsProduct> esProductList = productMapper.getAllEsProductList(null);
        Iterable<EsProduct> esProductIterable = productRepository.saveAll(esProductList);
        Iterator<EsProduct> iterator = esProductIterable.iterator();
        int result = 0;
        while (iterator.hasNext()) {
            result++;
            iterator.next();
        }
        return result;
    }

    /** 根据ID删除ES中的商品 */
    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    /** 根据ID创建ES商品索引 */
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

    /** 根据ID列表批量删除ES商品 */
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

    /** 根据关键词在名称、副标题、关键词中搜索 */
    @Override
    public Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return productRepository.findByNameOrSubTitleOrKeywords(keyword, keyword, keyword, pageable);
    }

    /** 综合搜索：关键词+品牌/分类筛选+排序 */
    @Override
    public Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize,Integer sort) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder();
        //分页
        nativeQueryBuilder.withPageable(pageable);
        //过滤
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
        //搜索
        if (StrUtil.isEmpty(keyword)) {
            nativeQueryBuilder.withQuery(QueryBuilders.matchAll(builder -> builder));
        } else {
            List<FunctionScore> functionScoreList = new ArrayList<>();
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("name").query(keyword)))
                    .weight(10.0)
                    .build());
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("subTitle").query(keyword)))
                    .weight(5.0)
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
        //排序
        if(sort==1){
            //按新品从新到旧
            nativeQueryBuilder.withSort(Sort.by(Sort.Order.desc("id")));
        }else if(sort==2){
            //按销量从高到低
            nativeQueryBuilder.withSort(Sort.by(Sort.Order.desc("sale")));
        }else if(sort==3){
            //按价格从低到高
            nativeQueryBuilder.withSort(Sort.by(Sort.Order.asc("price")));
        }else if(sort==4){
            //按价格从高到低
            nativeQueryBuilder.withSort(Sort.by(Sort.Order.desc("price")));
        }
        //按相关度
        nativeQueryBuilder.withSort(Sort.by(Sort.Order.desc("_score")));
        NativeQuery nativeQuery = nativeQueryBuilder.build();
        LOGGER.info("DSL:{}", nativeQuery.getQuery().toString());
        SearchHits<EsProduct> searchHits = elasticsearchTemplate.search(nativeQuery, EsProduct.class);
        if(searchHits.getTotalHits()<=0){
            return new PageImpl<>(ListUtil.empty(),pageable,0);
        }
        List<EsProduct> searchProductList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return new PageImpl<>(searchProductList,pageable,searchHits.getTotalHits());
    }

    /** 根据商品ID推荐相关商品（基于名称、品牌、分类的相关度打分） */
    @Override
    public Page<EsProduct> recommend(Long id, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        List<EsProduct> esProductList = productMapper.getAllEsProductList(id);
        if (esProductList.size() > 0) {
            EsProduct esProduct = esProductList.get(0);
            String keyword = esProduct.getName();
            Long brandId = esProduct.getBrandId();
            Long productCategoryId = esProduct.getProductCategoryId();
            //构建查询条件
            NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder();
            //分页
            nativeQueryBuilder.withPageable(pageable);
            //用于过滤掉相同的商品
            nativeQueryBuilder.withFilter(QueryBuilders.bool(build -> build.mustNot(QueryBuilders.term(b->b.field("id").value(id)))));
            //根据商品标题、品牌、分类进行搜索
            List<FunctionScore> functionScoreList = new ArrayList<>();
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("name").query(keyword)))
                    .weight(8.0)
                    .build());
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("subTitle").query(keyword)))
                    .weight(2.0)
                    .build());
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("keywords").query(keyword)))
                    .weight(2.0)
                    .build());
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("brandId").query(brandId)))
                    .weight(5.0)
                    .build());
            functionScoreList.add(new FunctionScore.Builder()
                    .filter(QueryBuilders.match(builder -> builder.field("productCategoryId").query(productCategoryId)))
                    .weight(3.0)
                    .build());
            FunctionScoreQuery.Builder functionScoreQueryBuilder = QueryBuilders.functionScore()
                    .functions(functionScoreList)
                    .scoreMode(FunctionScoreMode.Sum)
                    .minScore(2.0);
            nativeQueryBuilder.withQuery(builder -> builder.functionScore(functionScoreQueryBuilder.build()));
            NativeQuery nativeQuery = nativeQueryBuilder.build();
            LOGGER.info("DSL:{}", nativeQuery.getQuery().toString());
            SearchHits<EsProduct> searchHits = elasticsearchTemplate.search(nativeQuery, EsProduct.class);
            if(searchHits.getTotalHits()<=0){
                return new PageImpl<>(ListUtil.empty(),pageable,0);
            }
            List<EsProduct> searchProductList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
            return new PageImpl<>(searchProductList,pageable,searchHits.getTotalHits());
        }
        return new PageImpl<>(ListUtil.empty());
    }

    /** 获取搜索词的关联品牌、分类及属性信息（ES聚合待迁移） */
    @Override
    public EsProductRelatedInfo searchRelatedInfo(String keyword) {
        // TODO: ES聚合API升级后需要重写searchRelatedInfo，当前返回空结果
        LOGGER.warn("searchRelatedInfo is not yet migrated to new ES aggregation API, keyword: {}", keyword);
        return new EsProductRelatedInfo();
    }

    /**
     * 将返回结果转换为对象（TODO: ES聚合API升级后需要重写）
     */
    private EsProductRelatedInfo convertProductRelatedInfo(SearchHits<EsProduct> response) {
        LOGGER.warn("convertProductRelatedInfo is not yet migrated to new ES aggregation API");
        return new EsProductRelatedInfo();
    }
}
