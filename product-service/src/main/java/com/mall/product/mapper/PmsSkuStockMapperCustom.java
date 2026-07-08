package com.mall.product.mapper;

import com.mall.product.model.PmsSkuStock;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义商品SKU管理Mapper
 * Created by macro on 2018/4/26.
 */
public interface PmsSkuStockMapperCustom extends PmsSkuStockMapper {
    /**
     * 批量插入操作
     */
    int insertList(@Param("list")List<PmsSkuStock> skuStockList);

    /**
     * 批量插入或替换操作
     */
    int replaceList(@Param("list")List<PmsSkuStock> skuStockList);
}
