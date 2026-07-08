package com.mall.product.mapper;

import com.mall.product.domain.dto.PmsProductResult;
import org.apache.ibatis.annotations.Param;


/**
 * 自定义商品管理Mapper
 * Created by macro on 2018/4/26.
 */
public interface PmsProductMapperCustom extends PmsProductMapper {
    /**
     * 获取商品编辑信息
     */
    PmsProductResult getUpdateInfo(@Param("id") Long id);
}
