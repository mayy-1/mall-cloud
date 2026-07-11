package com.mall.product.mapper;

import com.mall.product.model.PmsProductAttribute;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**商品属性Mapper */
@Primary
public interface PmsProductAttributeMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductAttribute row);

    int insertSelective(PmsProductAttribute row);
    PmsProductAttribute selectByPrimaryKey(Long id);

    List<PmsProductAttribute> selectByCondition(PmsProductAttribute record);

    int deleteByCondition(PmsProductAttribute record);

    int updateSelectiveByCondition(@Param("record") PmsProductAttribute record, @Param("condition") PmsProductAttribute condition);

    int updateByPrimaryKeySelective(PmsProductAttribute row);

    int updateByPrimaryKey(PmsProductAttribute row);
}
