package com.mall.product.mapper;

import com.mall.product.model.PmsProductFullReduction;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义商品满减Mapper
 * Created by macro on 2018/4/26.
 */
public interface PmsProductFullReductionMapperCustom extends PmsProductFullReductionMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list") List<PmsProductFullReduction> productFullReductionList);
}
