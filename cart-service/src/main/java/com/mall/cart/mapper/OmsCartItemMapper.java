package com.mall.cart.mapper;

import com.mall.cart.model.OmsCartItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 购物车商品项Mapper接口（MyBatis Generator自动生成）
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