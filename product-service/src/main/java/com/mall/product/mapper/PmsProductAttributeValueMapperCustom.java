package com.mall.product.mapper;

import com.mall.product.model.PmsProductAttributeValue;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品参数，商品自定义规格属性Mapper
 * Created by macro on 2018/4/26.
 */
public interface PmsProductAttributeValueMapperCustom extends PmsProductAttributeValueMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list")List<PmsProductAttributeValue> productAttributeValueList);
}
