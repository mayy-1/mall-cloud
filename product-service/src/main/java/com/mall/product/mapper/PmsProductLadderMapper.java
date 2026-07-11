package com.mall.product.mapper;

import com.mall.product.model.PmsProductLadder;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**商品阶梯价格Mapper */
@Primary
public interface PmsProductLadderMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductLadder row);

    int insertSelective(PmsProductLadder row);
    PmsProductLadder selectByPrimaryKey(Long id);

    List<PmsProductLadder> selectByCondition(PmsProductLadder record);

    int deleteByCondition(PmsProductLadder record);

    int updateSelectiveByCondition(@Param("record") PmsProductLadder record, @Param("condition") PmsProductLadder condition);

    int updateByPrimaryKeySelective(PmsProductLadder row);

    int updateByPrimaryKey(PmsProductLadder row);
}
