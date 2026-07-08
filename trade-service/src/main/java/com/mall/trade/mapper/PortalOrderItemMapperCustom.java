package com.mall.trade.mapper;

import com.mall.trade.model.OmsOrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单商品信息管理自定义Mapper
 */
public interface PortalOrderItemMapperCustom {
    int insertList(@Param("list") List<OmsOrderItem> list);
}
