package com.mall.product.mapper;

import com.mall.product.domain.dto.PmsProductResult;
import com.mall.product.model.PmsProduct;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 商品Mapper
 * 对应表: pms_product
 */
@Primary
public interface PmsProductMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProduct row);

    int insertSelective(PmsProduct row);

    PmsProduct selectByPrimaryKey(Long id);

    List<PmsProduct> selectByIds(@Param("ids") List<Long> ids);

    List<PmsProduct> selectByCondition(PmsProduct record);

    List<PmsProduct> selectBySearch(@Param("keyword") String keyword,
                                    @Param("sort") Integer sort);

    int deleteByCondition(PmsProduct record);

    int updateSelectiveByCondition(@Param("record") PmsProduct record, @Param("condition") PmsProduct condition);

    int updateByPrimaryKeySelective(PmsProduct row);

    int updateByPrimaryKey(PmsProduct row);

    /**
     * 批量按ID更新（仅更新非空字段）
     */
    int updateByIds(@Param("record") PmsProduct record, @Param("ids") List<Long> ids);

    /**
     * 获取商品更新信息（含阶梯价格、满减、会员价格、SKU、属性值、专题/优选关系）
     */
    PmsProductResult getUpdateInfo(Long id);
}
