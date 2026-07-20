package com.mall.product.mapper;

import com.mall.product.model.PmsProductAttributeValue;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**商品属性值Mapper */
@Primary
public interface PmsProductAttributeValueMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductAttributeValue row);

    int insertSelective(PmsProductAttributeValue row);
    PmsProductAttributeValue selectByPrimaryKey(Long id);

    List<PmsProductAttributeValue> selectByCondition(PmsProductAttributeValue record);

    int deleteByCondition(PmsProductAttributeValue record);

    int updateSelectiveByCondition(@Param("record") PmsProductAttributeValue record, @Param("condition") PmsProductAttributeValue condition);

    int updateByPrimaryKeySelective(PmsProductAttributeValue row);

    int updateByPrimaryKey(PmsProductAttributeValue row);

    /**
     * 批量插入商品属性值
     */
    int insertList(@Param("list") List<PmsProductAttributeValue> list);
}
