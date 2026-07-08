package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsCouponProductCategoryRelation;
import com.mall.marketing.model.SmsCouponProductCategoryRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 优惠券商品分类关联Mapper
 * 提供优惠券与商品分类关联关系的增删改查操作
 */
@Primary
public interface SmsCouponProductCategoryRelationMapper {
    long countByExample(SmsCouponProductCategoryRelationExample example);

    int deleteByExample(SmsCouponProductCategoryRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsCouponProductCategoryRelation row);

    int insertSelective(SmsCouponProductCategoryRelation row);

    List<SmsCouponProductCategoryRelation> selectByExample(SmsCouponProductCategoryRelationExample example);

    SmsCouponProductCategoryRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsCouponProductCategoryRelation row, @Param("example") SmsCouponProductCategoryRelationExample example);

    int updateByExample(@Param("row") SmsCouponProductCategoryRelation row, @Param("example") SmsCouponProductCategoryRelationExample example);

    int updateByPrimaryKeySelective(SmsCouponProductCategoryRelation row);

    int updateByPrimaryKey(SmsCouponProductCategoryRelation row);
}
