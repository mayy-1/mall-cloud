package com.mall.trade.service;

import com.mym.mall.common.api.CommonPage;
import com.mall.trade.domain.dto.ConfirmOrderResult;
import com.mall.trade.domain.dto.OmsOrderDetail;
import com.mall.trade.domain.dto.OrderParam;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 前台订单管理Service
 * Created by macro on 2018/8/30.
 */
public interface IOrderService {
    /** 根据购物车生成确认单 */
    ConfirmOrderResult generateConfirmOrder(List<Long> cartIds);

    /** 生成订单 */
    @Transactional
    Map<String, Object> generateOrder(OrderParam orderParam);

    /** 支付成功回调 */
    @Transactional
    Integer paySuccess(Long orderId, Integer payType);

    /** 批量取消超时订单 */
    @Transactional
    Integer cancelTimeOutOrder();

    /** 取消单个订单 */
    @Transactional
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
    @Transactional
    void paySuccessByOrderSn(String orderSn, Integer payType);
}
