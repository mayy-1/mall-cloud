package com.mall.order.mapper;

import com.mall.order.domain.dto.OmsOrderDeliveryParam;
import com.mall.order.domain.dto.OmsOrderDetail;
import com.mall.order.domain.dto.OmsOrderQueryParam;
import com.mall.order.model.OmsOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单自定义查询Mapper
 * Created by macro on 2018/10/12.
 */
public interface OmsOrderMapperCustom extends OmsOrderMapper {
    /**
     * 条件查询订单
     */
    List<OmsOrder> getList(@Param("queryParam") OmsOrderQueryParam queryParam);

    /**
     * 批量发货
     */
    int delivery(@Param("list") List<OmsOrderDeliveryParam> deliveryParamList);

    /**
     * 获取订单详情
     */
    OmsOrderDetail getDetail(@Param("id") Long id);
}
