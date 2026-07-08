package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsCouponProductRelation;
import com.mall.marketing.model.SmsCouponProductRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 优惠券商品关联Mapper
 * 提供优惠券与商品关联关系的增删改查操作
 */
@Primary
public interface SmsCouponProductRelationMapper {
    long countByExample(SmsCouponProductRelationExample example);

    int deleteByExample(SmsCouponProductRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsCouponProductRelation row);

    int insertSelective(SmsCouponProductRelation row);

    List<SmsCouponProductRelation> selectByExample(SmsCouponProductRelationExample example);

    SmsCouponProductRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsCouponProductRelation row, @Param("example") SmsCouponProductRelationExample example);

    int updateByExample(@Param("row") SmsCouponProductRelation row, @Param("example") SmsCouponProductRelationExample example);

    int updateByPrimaryKeySelective(SmsCouponProductRelation row);

    int updateByPrimaryKey(SmsCouponProductRelation row);
}
