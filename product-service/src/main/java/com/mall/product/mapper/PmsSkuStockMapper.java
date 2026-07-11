package com.mall.product.mapper;

import com.mall.product.model.PmsSkuStock;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**SKU库存Mapper */
@Primary
public interface PmsSkuStockMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsSkuStock row);

    int insertSelective(PmsSkuStock row);
    PmsSkuStock selectByPrimaryKey(Long id);

    List<PmsSkuStock> selectByCondition(PmsSkuStock record);

    int deleteByCondition(PmsSkuStock record);

    int updateSelectiveByCondition(@Param("record") PmsSkuStock record, @Param("condition") PmsSkuStock condition);

    int updateByPrimaryKeySelective(PmsSkuStock row);

    int updateByPrimaryKey(PmsSkuStock row);
}
