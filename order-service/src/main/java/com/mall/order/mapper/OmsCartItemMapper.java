package com.mall.order.mapper;

import com.mall.order.model.OmsCartItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 购物车商品Mapper
 * 提供购物车商品的增删改查操作
 */
public interface OmsCartItemMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OmsCartItem row);

    int insertSelective(OmsCartItem row);
    OmsCartItem selectByPrimaryKey(Long id);

    List<OmsCartItem> selectByCondition(OmsCartItem record);

    int deleteByCondition(OmsCartItem record);

    int updateSelectiveByCondition(@Param("record") OmsCartItem record, @Param("condition") OmsCartItem condition);

    int updateByPrimaryKeySelective(OmsCartItem row);

    int updateByPrimaryKey(OmsCartItem row);
}