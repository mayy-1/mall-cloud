package com.mall.marketing.mapper;

import com.mall.marketing.model.PmsProduct;

/**
 * 商品Mapper（营销服务本地引用，仅用于DAO XML的resultMap关联）
 */
public interface PmsProductMapper {
    PmsProduct selectByPrimaryKey(Long id);
}
