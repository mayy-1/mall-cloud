package com.mall.product.mapper;

import com.mall.product.model.PmsProduct;
import com.mall.product.model.PmsProductExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**商品Mapper */
@Primary
public interface PmsProductMapper {
    long countByExample(PmsProductExample example);

    int deleteByExample(PmsProductExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PmsProduct row);

    int insertSelective(PmsProduct row);

    List<PmsProduct> selectByExample(PmsProductExample example);

    PmsProduct selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") PmsProduct row, @Param("example") PmsProductExample example);

    int updateByExample(@Param("row") PmsProduct row, @Param("example") PmsProductExample example);

    int updateByPrimaryKeySelective(PmsProduct row);

    int updateByPrimaryKey(PmsProduct row);
}
