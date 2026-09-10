package com.mall.search.repository;

import com.mall.search.domain.EsProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 搜索商品ES操作类
 * 继承 ElasticsearchRepository 提供 findById/save/deleteAll 等能力
 */
public interface EsProductRepository extends ElasticsearchRepository<EsProduct, Long> {
}
