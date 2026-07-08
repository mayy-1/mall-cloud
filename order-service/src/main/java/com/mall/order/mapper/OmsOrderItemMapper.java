package com.mall.order.mapper;

import com.mall.order.model.OmsOrderItem;
import com.mall.order.model.OmsOrderItemExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 订单商品项Mapper
 * 提供订单商品项的增删改查操作
 */
public interface OmsOrderItemMapper {
    long countByExample(OmsOrderItemExample example);

    int deleteByExample(OmsOrderItemExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrderItem row);

    int insertSelective(OmsOrderItem row);

    List<OmsOrderItem> selectByExample(OmsOrderItemExample example);

    OmsOrderItem selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") OmsOrderItem row, @Param("example") OmsOrderItemExample example);

    int updateByExample(@Param("row") OmsOrderItem row, @Param("example") OmsOrderItemExample example);

    int updateByPrimaryKeySelective(OmsOrderItem row);

    int updateByPrimaryKey(OmsOrderItem row);
}