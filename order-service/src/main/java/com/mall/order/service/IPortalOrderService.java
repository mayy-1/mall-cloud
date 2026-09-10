package com.mall.order.service;

import com.mym.mall.common.api.CommonPage;
import com.mall.order.domain.dto.BuyNowParam;
import com.mall.order.domain.dto.ConfirmOrderResult;
import com.mall.order.domain.dto.OmsOrderDetail;
import com.mall.order.domain.dto.OrderParam;
import com.mall.order.domain.dto.OrderPostMessage;

import java.util.List;
import java.util.Map;

/**
 * 前台订单管理Service
 */
public interface IPortalOrderService {

    ConfirmOrderResult generateConfirmOrder(List<Long> cartIds);

    Map<String, Object> generateOrder(OrderParam orderParam);

    ConfirmOrderResult buyNowConfirm(BuyNowParam buyNowParam);

    Map<String, Object> buyNow(OrderParam orderParam, BuyNowParam buyNowParam);

    Integer paySuccess(Long orderId, Integer payType);

    void cancelOrder(Long orderId);

    void sendDelayMessageCancelOrder(Long orderId);

    void confirmReceiveOrder(Long orderId);

    CommonPage<OmsOrderDetail> list(List<Integer> status, Integer pageNum, Integer pageSize);

    OmsOrderDetail detail(Long orderId);

    void deleteOrder(Long orderId);

    void paySuccessByOrderSn(String orderSn, Integer payType);

    void handlePostOrder(OrderPostMessage message);
}
