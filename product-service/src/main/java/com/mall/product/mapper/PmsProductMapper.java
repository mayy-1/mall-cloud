package com.mall.product.mapper;

import com.mall.product.model.PmsProduct;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**商品Mapper */
@Primary
public interface PmsProductMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProduct row);

    int insertSelective(PmsProduct row);
    PmsProduct selectByPrimaryKey(Long id);

    List<PmsProduct> selectByCondition(PmsProduct record);

    int deleteByCondition(PmsProduct record);

    int updateSelectiveByCondition(@Param("record") PmsProduct record, @Param("condition") PmsProduct condition);

    int updateByPrimaryKeySelective(PmsProduct row);

    int updateByPrimaryKey(PmsProduct row);
}
