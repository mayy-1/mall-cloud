package com.mall.order.mapper;

import com.mall.order.model.OmsOrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单商品信息管理自定义Mapper
 */
public interface PortalOrderItemMapper {
    int insertList(@Param("list") List<OmsOrderItem> list);
}

