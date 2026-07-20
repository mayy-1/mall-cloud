package com.mall.product.mapper;

import com.mall.product.model.PmsProductFullReduction;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 商品满减Mapper
 * 【管理端专用】商品创建/编辑时设置满减规则（如满100减20）
 * 对应表: pms_product_full_reduction
 */
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

    /**
     * 批量插入商品满减信息
     */
    int insertList(@Param("list") List<PmsProductFullReduction> list);
}
