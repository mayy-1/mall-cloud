package com.mall.product.mapper;

import com.mall.product.model.PmsProductCategory;
import com.mall.product.model.PmsProductCategoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**商品分类Mapper */
@Primary
public interface PmsProductCategoryMapper {
    long countByExample(PmsProductCategoryExample example);

    int deleteByExample(PmsProductCategoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductCategory row);

    int insertSelective(PmsProductCategory row);

    List<PmsProductCategory> selectByExample(PmsProductCategoryExample example);

    PmsProductCategory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") PmsProductCategory row, @Param("example") PmsProductCategoryExample example);

    int updateByExample(@Param("row") PmsProductCategory row, @Param("example") PmsProductCategoryExample example);

    int updateByPrimaryKeySelective(PmsProductCategory row);

    int updateByPrimaryKey(PmsProductCategory row);
}
