package com.mall.product.mapper;

import com.mall.product.model.PmsSkuStock;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * SKU库存Mapper
 * 【管理端+订单系统】
 * - 管理端：insertList/replaceList(批量编辑库存)
 * - 订单系统：selectByPrimaryKey/updateByPrimaryKeySelective(扣减/锁定/释放库存)
 * 对应表: pms_sku_stock
 */
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

    /**
     * 批量插入SKU库存
     */
    int insertList(@Param("list") List<PmsSkuStock> list);

    /**
     * 批量替换SKU库存（REPLACE INTO，存在则更新，不存在则插入）
     */
    int replaceList(@Param("list") List<PmsSkuStock> list);

    /**
     * 批量删除SKU
     */
    int deleteByIds(@Param("ids") List<Long> ids);
}
