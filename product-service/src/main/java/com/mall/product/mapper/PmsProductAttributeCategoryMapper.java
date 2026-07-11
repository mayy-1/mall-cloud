package com.mall.product.mapper;

import com.mall.product.model.PmsProductAttributeCategory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**商品属性分类Mapper */
@Primary
public interface PmsProductAttributeCategoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductAttributeCategory row);

    int insertSelective(PmsProductAttributeCategory row);
    PmsProductAttributeCategory selectByPrimaryKey(Long id);

    List<PmsProductAttributeCategory> selectByCondition(PmsProductAttributeCategory record);

    int deleteByCondition(PmsProductAttributeCategory record);

    int updateSelectiveByCondition(@Param("record") PmsProductAttributeCategory record, @Param("condition") PmsProductAttributeCategory condition);

    int updateByPrimaryKeySelective(PmsProductAttributeCategory row);

    int updateByPrimaryKey(PmsProductAttributeCategory row);

    List<PmsProductAttributeCategoryItem> getListWithAttr();
}