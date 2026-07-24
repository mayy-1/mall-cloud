package com.mall.order.mapper;

import com.mall.order.domain.dto.OmsOrderDetail;
import com.mall.order.model.OmsOrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 前台订单自定义Mapper
 */
public interface PortalOrderMapper {
    OmsOrderDetail getDetail(@Param("id") Long id);

    int updateSkuStock(@Param("itemList") List<OmsOrderItem> orderItemList);

    List<OmsOrderDetail> getTimeOutOrders(@Param("minute") Integer minute);

    int updateOrderStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    int releaseSkuStockLock(@Param("itemList") List<OmsOrderItem> orderItemList);
}

