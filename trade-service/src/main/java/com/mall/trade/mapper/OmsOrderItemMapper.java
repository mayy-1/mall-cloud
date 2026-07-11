package com.mall.trade.mapper;

import com.mall.trade.model.OmsOrderItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 订单商品项数据访问接口
 */
public interface OmsOrderItemMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrderItem row);

    int insertSelective(OmsOrderItem row);
    OmsOrderItem selectByPrimaryKey(Long id);

    List<OmsOrderItem> selectByCondition(OmsOrderItem record);

    int deleteByCondition(OmsOrderItem record);

    int updateSelectiveByCondition(@Param("record") OmsOrderItem record, @Param("condition") OmsOrderItem condition);

    int updateByPrimaryKeySelective(OmsOrderItem row);

    int updateByPrimaryKey(OmsOrderItem row);
}