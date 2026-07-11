package com.mall.product.mapper;

import com.mall.product.model.PmsProductCategory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**商品分类Mapper */
@Primary
public interface PmsProductCategoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductCategory row);

    int insertSelective(PmsProductCategory row);
    PmsProductCategory selectByPrimaryKey(Long id);

    List<PmsProductCategory> selectByCondition(PmsProductCategory record);

    int deleteByCondition(PmsProductCategory record);

    int updateSelectiveByCondition(@Param("record") PmsProductCategory record, @Param("condition") PmsProductCategory condition);

    int updateByPrimaryKeySelective(PmsProductCategory row);

    int updateByPrimaryKey(PmsProductCategory row);

    List<PmsProductCategoryWithChildrenItem> listWithChildren();
}