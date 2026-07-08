package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsCouponProductCategoryRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义优惠券和商品分类关系管理Mapper
 * Created by macro on 2018/8/28.
 */
public interface SmsCouponProductCategoryRelationMapperCustom extends SmsCouponProductCategoryRelationMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list")List<SmsCouponProductCategoryRelation> productCategoryRelationList);
}
