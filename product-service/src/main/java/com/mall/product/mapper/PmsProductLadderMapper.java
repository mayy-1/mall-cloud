package com.mall.product.mapper;

import com.mall.product.model.PmsProductLadder;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 商品阶梯价格Mapper
 * 【管理端专用】商品创建/编辑时设置阶梯价格（如满3件打折）
 * 对应表: pms_product_ladder
 */
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

    /**
     * 批量插入商品阶梯价格
     */
    int insertList(@Param("list") List<PmsProductLadder> list);
}
