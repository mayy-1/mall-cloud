package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsCouponProductRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义优惠券和商品关系关系Mapper
 * Created by macro on 2018/8/28.
 */
public interface SmsCouponProductRelationMapperCustom extends SmsCouponProductRelationMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list")List<SmsCouponProductRelation> productRelationList);
}
