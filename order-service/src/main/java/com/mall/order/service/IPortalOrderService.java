package com.mall.order.service;

import com.mym.mall.common.api.CommonPage;
import com.mall.order.domain.dto.BuyNowParam;
import com.mall.order.domain.dto.ConfirmOrderResult;
import com.mall.order.domain.dto.OmsOrderDetail;
import com.mall.order.domain.dto.OrderParam;

import java.util.List;
import java.util.Map;

/**
 * 前台订单管理Service
 */
public interface IPortalOrderService {
    /** 根据购物车生成确认单 */
    ConfirmOrderResult generateConfirmOrder(List<Long> cartIds);

    /** 生成订单 */
    Map<String, Object> generateOrder(OrderParam orderParam);

    /** 立即购买-生成确认单 */
    ConfirmOrderResult buyNowConfirm(BuyNowParam buyNowParam);

    /** 立即购买-创建订单 */
    Map<String, Object> buyNow(OrderParam orderParam, BuyNowParam buyNowParam);

    /** 支付成功回调 */
    Integer paySuccess(Long orderId, Integer payType);

    /** 批量取消超时订单 */
    Integer cancelTimeOutOrder();

    /** 取消单个订单 */
    void cancelOrder(Long orderId);

    /** 发送延迟取消订单消息 */
    void sendDelayMessageCancelOrder(Long orderId);

    /** 确认收货 */
    void confirmReceiveOrder(Long orderId);

    /** 分页获取订单列表 */
    CommonPage<OmsOrderDetail> list(Integer status, Integer pageNum, Integer pageSize);

    /** 获取订单详情 */
    OmsOrderDetail detail(Long orderId);

    /** 删除订单 */
    void deleteOrder(Long orderId);

    /** 根据订单号支付成功回调 */
    void paySuccessByOrderSn(String orderSn, Integer payType);
}

