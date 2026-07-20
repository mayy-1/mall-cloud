package com.mall.product.mapper;

import com.mall.product.domain.dto.PmsProductCategoryFlatItem;
import com.mall.product.model.PmsProductCategory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 商品分类Mapper
 * 【管理端+用户端】分类查询为共用，增删改为管理端
 * 对应表: pms_product_category
 */
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

    /**
     * 批量按ID更新（仅更新navStatus/showStatus等非空字段）
     */
    int updateByIds(@Param("record") PmsProductCategory record, @Param("ids") List<Long> ids);

    /**
     * 获取所有分类及其子分类（扁平结构，由 Service 组装成树）
     */
    List<PmsProductCategoryFlatItem> listWithChildren();
}