package com.mall.order.mapper;

import com.mall.order.model.OmsCartItem;
import com.mall.order.model.OmsCartItemExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 购物车商品Mapper
 * 提供购物车商品的增删改查操作
 */
public interface OmsCartItemMapper {
    long countByExample(OmsCartItemExample example);

    int deleteByExample(OmsCartItemExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OmsCartItem row);

    int insertSelective(OmsCartItem row);

    List<OmsCartItem> selectByExample(OmsCartItemExample example);

    OmsCartItem selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") OmsCartItem row, @Param("example") OmsCartItemExample example);

    int updateByExample(@Param("row") OmsCartItem row, @Param("example") OmsCartItemExample example);

    int updateByPrimaryKeySelective(OmsCartItem row);

    int updateByPrimaryKey(OmsCartItem row);
}