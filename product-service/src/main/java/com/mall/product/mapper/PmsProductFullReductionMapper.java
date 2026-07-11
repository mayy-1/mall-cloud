package com.mall.product.mapper;

import com.mall.product.model.PmsProductFullReduction;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**满减Mapper */
@Primary
public interface PmsProductFullReductionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductFullReduction row);

    int insertSelective(PmsProductFullReduction row);
    PmsProductFullReduction selectByPrimaryKey(Long id);

    List<PmsProductFullReduction> selectByCondition(PmsProductFullReduction record);

    int deleteByCondition(PmsProductFullReduction record);

    int updateSelectiveByCondition(@Param("record") PmsProductFullReduction record, @Param("condition") PmsProductFullReduction condition);

    int updateByPrimaryKeySelective(PmsProductFullReduction row);

    int updateByPrimaryKey(PmsProductFullReduction row);
}
