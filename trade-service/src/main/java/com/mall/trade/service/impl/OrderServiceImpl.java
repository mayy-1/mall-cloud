package com.mall.trade.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageHelper;
import com.mall.api.client.CartClient;
import com.mall.api.client.MemberAddressClient;
import com.mall.api.client.MemberClient;
import com.mall.api.client.MemberCouponClient;
import com.mall.api.dto.*;
import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.exception.Asserts;
import com.mym.mall.common.service.RedisService;
import com.mall.trade.mapper.UmsIntegrationConsumeSettingMapper;
import com.mall.trade.mapper.PmsSkuStockMapper;
import com.mall.trade.mapper.SmsCouponHistoryMapper;
import com.mall.trade.mapper.OmsOrderSettingMapper;
import com.mall.trade.mapper.PortalOrderItemMapper;
import com.mall.trade.model.UmsIntegrationConsumeSetting;
import com.mall.trade.model.PmsSkuStock;
import com.mall.trade.model.SmsCouponHistory;
import com.mall.trade.model.OmsOrderSetting;
import com.mall.trade.model.OmsOrder;
import com.mall.trade.model.OmsOrderItem;
import com.mall.trade.domain.dto.ConfirmOrderResult;
import com.mall.trade.domain.dto.OmsOrderDetail;
import com.mall.trade.domain.dto.OrderParam;
import com.mall.trade.mapper.OmsOrderMapper;
import com.mall.trade.mapper.OmsOrderItemMapper;
import com.mall.trade.service.IOrderService;
import com.mall.trade.service.ISeckillOrderService;
import com.mall.trade.mq.CancelOrderSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 前台订单管理Service
 * Created by macro on 2018/8/30.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    /** 会员服务Feign客户端 */
    private final MemberClient memberClient;
    /** 购物车服务Feign客户端 */
    private final CartClient cartClient;
    /** 收货地址服务Feign客户端 */
    private final MemberAddressClient memberAddressClient;
    /** 会员优惠券服务Feign客户端 */
    private final MemberCouponClient memberCouponClient;
    /** 积分消费设置Mapper */
    private final UmsIntegrationConsumeSettingMapper integrationConsumeSettingMapper;
    /** SKU库存Mapper */
    private final PmsSkuStockMapper skuStockMapper;
    /** 订单Mapper */
    private final OmsOrderMapper orderMapper;
    /** 前台订单商品项Mapper */
    private final PortalOrderItemMapper portalOrderItemMapper;
    /** 优惠券历史Mapper */
    private final SmsCouponHistoryMapper couponHistoryMapper;
    /** Redis缓存服务 */
    private final RedisService redisService;
    /** Redis订单ID前缀 */
    @Value("${redis.key.orderId}")
    private String REDIS_KEY_ORDER_ID;
    /** Redis数据库 */
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    /** 前台订单Mapper */
    private final PortalOrderMapper portalOrderMapper;
    /** 订单设置Mapper */
    private final OmsOrderSettingMapper orderSettingMapper;
    /** 订单商品项Mapper */
    private final OmsOrderItemMapper orderItemMapper;
    /** 取消订单消息发送器 */
    private final CancelOrderSender cancelOrderSender;
    /** 秒杀订单服务 */
    private final ISeckillOrderService seckillOrderService;

    @Override
    public ConfirmOrderResult generateConfirmOrder(List<Long> cartIds) {
        ConfirmOrderResult result = new ConfirmOrderResult();
        MemberDTO currentMember = memberClient.getCurrentMember().getData();
        List<CartPromotionItemDTO> cartPromotionItemList = cartClient.listPromotion(currentMember.getId(), cartIds).getData();
        result.setCartPromotionItemList(cartPromotionItemList);
        List<MemberAddressDTO> memberReceiveAddressList = memberAddressClient.list().getData();
        result.setMemberReceiveAddressList(memberReceiveAddressList);
        List<CouponHistoryDetailDTO> couponHistoryDetailList = memberCouponClient.listCart(cartPromotionItemList, 1).getData();
        result.setCouponHistoryDetailList(couponHistoryDetailList);
        result.setMemberIntegration(currentMember.getIntegration());
        UmsIntegrationConsumeSetting integrationConsumeSetting = integrationConsumeSettingMapper.selectByPrimaryKey(1L);
        result.setIntegrationConsumeSetting(integrationConsumeSetting);
        ConfirmOrderResult.CalcAmount calcAmount = calcCartAmount(cartPromotionItemList);
        result.setCalcAmount(calcAmount);
        return result;
    }

    @Override
    public Map<String, Object> generateOrder(OrderParam orderParam) {
        List<OmsOrderItem> orderItemList = new ArrayList<>();
        if(orderParam.getMemberReceiveAddressId()==null){
            Asserts.fail("请选择收货地址！");
        }
        MemberDTO currentMember = memberClient.getCurrentMember().getData();
        List<CartPromotionItemDTO> cartPromotionItemList = cartClient.listPromotion(currentMember.getId(), orderParam.getCartIds()).getData();
        for (CartPromotionItemDTO cartPromotionItem : cartPromotionItemList) {
            OmsOrderItem orderItem = new OmsOrderItem();
            orderItem.setProductId(cartPromotionItem.getProductId());
            orderItem.setProductName(cartPromotionItem.getProductName());
            orderItem.setProductPic(cartPromotionItem.getProductPic());
            orderItem.setProductAttr(cartPromotionItem.getProductAttr());
            orderItem.setProductBrand(cartPromotionItem.getProductBrand());
            orderItem.setProductSn(cartPromotionItem.getProductSn());
            orderItem.setProductPrice(cartPromotionItem.getPrice());
            orderItem.setProductQuantity(cartPromotionItem.getQuantity());
            orderItem.setProductSkuId(cartPromotionItem.getProductSkuId());
            orderItem.setProductSkuCode(cartPromotionItem.getProductSkuCode());
            orderItem.setProductCategoryId(cartPromotionItem.getProductCategoryId());
            orderItem.setPromotionAmount(cartPromotionItem.getReduceAmount());
            orderItem.setPromotionName(cartPromotionItem.getPromotionMessage());
            orderItem.setGiftIntegration(cartPromotionItem.getIntegration());
            orderItem.setGiftGrowth(cartPromotionItem.getGrowth());
            orderItemList.add(orderItem);
        }
        if (!hasStock(cartPromotionItemList)) {
            Asserts.fail("库存不足，无法下单");
        }
        if (orderParam.getCouponId() == null) {
            for (OmsOrderItem orderItem : orderItemList) {
                orderItem.setCouponAmount(new BigDecimal(0));
            }
        } else {
            CouponHistoryDetailDTO couponHistoryDetail = getUseCoupon(cartPromotionItemList, orderParam.getCouponId());
            if (couponHistoryDetail == null) {
                Asserts.fail("该优惠券不可用");
            }
            handleCouponAmount(orderItemList, couponHistoryDetail);
        }
        if (orderParam.getUseIntegration() == null || orderParam.getUseIntegration().equals(0)) {
            for (OmsOrderItem orderItem : orderItemList) {
                orderItem.setIntegrationAmount(new BigDecimal(0));
            }
        } else {
            BigDecimal totalAmount = calcTotalAmount(orderItemList);
            BigDecimal integrationAmount = getUseIntegrationAmount(orderParam.getUseIntegration(), totalAmount, currentMember, orderParam.getCouponId() != null);
            if (integrationAmount.compareTo(new BigDecimal(0)) == 0) {
                Asserts.fail("积分不可用");
            } else {
                for (OmsOrderItem orderItem : orderItemList) {
                    BigDecimal perAmount = orderItem.getProductPrice().divide(totalAmount, 3, RoundingMode.HALF_EVEN).multiply(integrationAmount);
                    orderItem.setIntegrationAmount(perAmount);
                }
            }
        }
        handleRealAmount(orderItemList);
        lockStock(cartPromotionItemList);
        OmsOrder order = new OmsOrder();
        order.setDiscountAmount(new BigDecimal(0));
        order.setTotalAmount(calcTotalAmount(orderItemList));
        order.setFreightAmount(new BigDecimal(0));
        order.setPromotionAmount(calcPromotionAmount(orderItemList));
        order.setPromotionInfo(getOrderPromotionInfo(orderItemList));
        if (orderParam.getCouponId() == null) {
            order.setCouponAmount(new BigDecimal(0));
        } else {
            order.setCouponId(orderParam.getCouponId());
            order.setCouponAmount(calcCouponAmount(orderItemList));
        }
        if (orderParam.getUseIntegration() == null) {
            order.setIntegration(0);
            order.setIntegrationAmount(new BigDecimal(0));
        } else {
            order.setIntegration(orderParam.getUseIntegration());
            order.setIntegrationAmount(calcIntegrationAmount(orderItemList));
        }
        order.setPayAmount(calcPayAmount(order));
        order.setMemberId(currentMember.getId());
        order.setCreateTime(new Date());
        order.setMemberUsername(currentMember.getUsername());
        order.setPayType(orderParam.getPayType());
        order.setSourceType(1);
        order.setStatus(0);
        order.setOrderType(0);
        MemberAddressDTO address = memberAddressClient.getItem(orderParam.getMemberReceiveAddressId()).getData();
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhoneNumber());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setConfirmStatus(0);
        order.setDeleteStatus(0);
        order.setIntegration(calcGifIntegration(orderItemList));
        order.setGrowth(calcGiftGrowth(orderItemList));
        order.setOrderSn(generateOrderSn(order));
        List<OmsOrderSetting> orderSettings = orderSettingMapper.selectByCondition(new OmsOrderSetting());
        if(CollUtil.isNotEmpty(orderSettings)){
            order.setAutoConfirmDay(orderSettings.get(0).getConfirmOvertime());
        }
        orderMapper.insert(order);
        for (OmsOrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(order.getOrderSn());
        }
        portalOrderItemMapper.insertList(orderItemList);
        if (orderParam.getCouponId() != null) {
            updateCouponStatus(orderParam.getCouponId(), currentMember.getId(), 1);
        }
        if (orderParam.getUseIntegration() != null) {
            order.setUseIntegration(orderParam.getUseIntegration());
            if(currentMember.getIntegration()==null){
                currentMember.setIntegration(0);
            }
            memberClient.updateIntegration(currentMember.getId(), currentMember.getIntegration() - orderParam.getUseIntegration());
        }
        deleteCartItemList(cartPromotionItemList, currentMember);
        sendDelayMessageCancelOrder(order.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("orderItemList", orderItemList);
        return result;
    }

    @Override
    public Integer paySuccess(Long orderId, Integer payType) {
        OmsOrder order = new OmsOrder();
        order.setId(orderId);
        order.setStatus(1);
        order.setPaymentTime(new Date());
        order.setPayType(payType);
        orderMapper.updateByPrimaryKeySelective(order);
        OmsOrderDetail orderDetail = portalOrderMapper.getDetail(orderId);
        int count = portalOrderMapper.updateSkuStock(orderDetail.getOrderItemList());
        return count;
    }

    @Override
    public Integer cancelTimeOutOrder() {
        Integer count = 0;
        OmsOrderSetting orderSetting = orderSettingMapper.selectByPrimaryKey(1L);
        List<OmsOrderDetail> timeOutOrders = portalOrderMapper.getTimeOutOrders(orderSetting.getNormalOrderOvertime());
        if (CollectionUtils.isEmpty(timeOutOrders)) {
            return count;
        }
        List<Long> ids = new ArrayList<>();
        for (OmsOrderDetail timeOutOrder : timeOutOrders) {
            ids.add(timeOutOrder.getId());
        }
        portalOrderMapper.updateOrderStatus(ids, 4);
        for (OmsOrderDetail timeOutOrder : timeOutOrders) {
            portalOrderMapper.releaseSkuStockLock(timeOutOrder.getOrderItemList());
            updateCouponStatus(timeOutOrder.getCouponId(), timeOutOrder.getMemberId(), 0);
            if (timeOutOrder.getUseIntegration() != null) {
                MemberDTO member = memberClient.getById(timeOutOrder.getMemberId()).getData();
                memberClient.updateIntegration(timeOutOrder.getMemberId(), member.getIntegration() + timeOutOrder.getUseIntegration());
            }
        }
        return timeOutOrders.size();
    }

    @Override
    public void cancelOrder(Long orderId) {
        OmsOrder condition = new OmsOrder();
        condition.setId(orderId).setStatus(0).setDeleteStatus(0);
        List<OmsOrder> cancelOrderList = orderMapper.selectByCondition(condition);
        if (CollectionUtils.isEmpty(cancelOrderList)) {
            return;
        }
        OmsOrder cancelOrder = cancelOrderList.get(0);
        if (cancelOrder != null) {
            cancelOrder.setStatus(4);
            orderMapper.updateByPrimaryKeySelective(cancelOrder);
            OmsOrderItem itemCondition = new OmsOrderItem();
            itemCondition.setOrderId(orderId);
            List<OmsOrderItem> orderItemList = orderItemMapper.selectByCondition(itemCondition);
            if (!CollectionUtils.isEmpty(orderItemList)) {
                portalOrderMapper.releaseSkuStockLock(orderItemList);
            }
            // 秒杀订单回滚 Redis 库存
            if (cancelOrder.getOrderType() != null && cancelOrder.getOrderType() == 1) {
                String promotionInfo = cancelOrder.getPromotionInfo();
                Long promotionId = null;
                if (promotionInfo != null && promotionInfo.startsWith("秒杀活动ID:")) {
                    promotionId = Long.parseLong(promotionInfo.replace("秒杀活动ID:", ""));
                }
                if (promotionId != null && !CollectionUtils.isEmpty(orderItemList)) {
                    OmsOrderItem orderItem = orderItemList.get(0);
                    seckillOrderService.rollbackSeckillStock(
                            promotionId,
                            orderItem.getProductId(),
                            cancelOrder.getMemberId(),
                            orderItem.getProductQuantity()
                    );
                }
            }
            updateCouponStatus(cancelOrder.getCouponId(), cancelOrder.getMemberId(), 0);
            if (cancelOrder.getUseIntegration() != null) {
                MemberDTO member = memberClient.getById(cancelOrder.getMemberId()).getData();
                memberClient.updateIntegration(cancelOrder.getMemberId(), member.getIntegration() + cancelOrder.getUseIntegration());
            }
        }
    }

    @Override
    public void sendDelayMessageCancelOrder(Long orderId) {
        OmsOrderSetting orderSetting = orderSettingMapper.selectByPrimaryKey(1L);
        long delayTimes = orderSetting.getNormalOrderOvertime() * 60 * 1000;
        cancelOrderSender.sendMessage(orderId, delayTimes);
    }

    @Override
    public void confirmReceiveOrder(Long orderId) {
        MemberDTO member = memberClient.getCurrentMember().getData();
        OmsOrder order = orderMapper.selectByPrimaryKey(orderId);
        if(!member.getId().equals(order.getMemberId())){
            Asserts.fail("不能确认他人订单！");
        }
        if(order.getStatus()!=2){
            Asserts.fail("该订单还未发货！");
        }
        order.setStatus(3);
        order.setConfirmStatus(1);
        order.setReceiveTime(new Date());
        orderMapper.updateByPrimaryKey(order);
    }

    @Override
    public CommonPage<OmsOrderDetail> list(Integer status, Integer pageNum, Integer pageSize) {
        if(status==-1){
            status = null;
        }
        MemberDTO member = memberClient.getCurrentMember().getData();
        PageHelper.startPage(pageNum, pageSize);
        OmsOrder condition = new OmsOrder();
        condition.setDeleteStatus(0);
        condition.setMemberId(member.getId());
        if(status!=null){
            condition.setStatus(status);
        }
        List<OmsOrder> orderList = orderMapper.selectByCondition(condition);
        CommonPage<OmsOrder> orderPage = CommonPage.restPage(orderList);
        CommonPage<OmsOrderDetail> resultPage = new CommonPage<>();
        resultPage.setPageNum(orderPage.getPageNum());
        resultPage.setPageSize(orderPage.getPageSize());
        resultPage.setTotal(orderPage.getTotal());
        resultPage.setTotalPage(orderPage.getTotalPage());
        if(CollUtil.isEmpty(orderList)){
            return resultPage;
        }
        List<Long> orderIds = orderList.stream().map(OmsOrder::getId).collect(Collectors.toList());
        List<OmsOrderItem> orderItemList = new ArrayList<>();
        for (Long orderId : orderIds) {
            OmsOrderItem itemCondition = new OmsOrderItem();
            itemCondition.setOrderId(orderId);
            orderItemList.addAll(orderItemMapper.selectByCondition(itemCondition));
        }
        List<OmsOrderDetail> orderDetailList = new ArrayList<>();
        for (OmsOrder omsOrder : orderList) {
            OmsOrderDetail orderDetail = new OmsOrderDetail();
            BeanUtil.copyProperties(omsOrder, orderDetail);
            List<OmsOrderItem> relatedItemList = orderItemList.stream().filter(item -> item.getOrderId().equals(orderDetail.getId())).collect(Collectors.toList());
            orderDetail.setOrderItemList(relatedItemList);
            orderDetailList.add(orderDetail);
        }
        resultPage.setList(orderDetailList);
        return resultPage;
    }

    @Override
    public OmsOrderDetail detail(Long orderId) {
        OmsOrder omsOrder = orderMapper.selectByPrimaryKey(orderId);
        OmsOrderItem condition = new OmsOrderItem();
        condition.setOrderId(orderId);
        List<OmsOrderItem> orderItemList = orderItemMapper.selectByCondition(condition);
        OmsOrderDetail orderDetail = new OmsOrderDetail();
        BeanUtil.copyProperties(omsOrder, orderDetail);
        orderDetail.setOrderItemList(orderItemList);
        return orderDetail;
    }

    @Override
    public void deleteOrder(Long orderId) {
        MemberDTO member = memberClient.getCurrentMember().getData();
        OmsOrder order = orderMapper.selectByPrimaryKey(orderId);
        if(!member.getId().equals(order.getMemberId())){
            Asserts.fail("不能删除他人订单！");
        }
        if(order.getStatus()==3||order.getStatus()==4){
            order.setDeleteStatus(1);
            orderMapper.updateByPrimaryKey(order);
        }else{
            Asserts.fail("只能删除已完成或已关闭的订单！");
        }
    }

    @Override
    public void paySuccessByOrderSn(String orderSn, Integer payType) {
        OmsOrder condition = new OmsOrder();
        condition.setOrderSn(orderSn);
        condition.setStatus(0);
        condition.setDeleteStatus(0);
        List<OmsOrder> orderList = orderMapper.selectByCondition(condition);
        if(CollUtil.isNotEmpty(orderList)){
            OmsOrder order = orderList.get(0);
            paySuccess(order.getId(), payType);
        }
    }

    private String generateOrderSn(OmsOrder order) {
        StringBuilder sb = new StringBuilder();
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String key = REDIS_DATABASE+":"+ REDIS_KEY_ORDER_ID + date;
        Long increment = redisService.incr(key, 1);
        sb.append(date);
        sb.append(String.format("%02d", order.getSourceType()));
        sb.append(String.format("%02d", order.getPayType()));
        String incrementStr = increment.toString();
        if (incrementStr.length() <= 6) {
            sb.append(String.format("%06d", increment));
        } else {
            sb.append(incrementStr);
        }
        return sb.toString();
    }

    private void deleteCartItemList(List<CartPromotionItemDTO> cartPromotionItemList, MemberDTO currentMember) {
        List<Long> ids = new ArrayList<>();
        for (CartPromotionItemDTO cartPromotionItem : cartPromotionItemList) {
            ids.add(cartPromotionItem.getId());
        }
        cartClient.delete(currentMember.getId(), ids);
    }

    private Integer calcGiftGrowth(List<OmsOrderItem> orderItemList) {
        Integer sum = 0;
        for (OmsOrderItem orderItem : orderItemList) {
            sum = sum + orderItem.getGiftGrowth() * orderItem.getProductQuantity();
        }
        return sum;
    }

    private Integer calcGifIntegration(List<OmsOrderItem> orderItemList) {
        int sum = 0;
        for (OmsOrderItem orderItem : orderItemList) {
            sum += orderItem.getGiftIntegration() * orderItem.getProductQuantity();
        }
        return sum;
    }

    private void updateCouponStatus(Long couponId, Long memberId, Integer useStatus) {
        if (couponId == null) return;
        SmsCouponHistory condition = new SmsCouponHistory();
        condition.setMemberId(memberId);
        condition.setCouponId(couponId);
        condition.setUseStatus(useStatus == 0 ? 1 : 0);
        List<SmsCouponHistory> couponHistoryList = couponHistoryMapper.selectByCondition(condition);
        if (!CollectionUtils.isEmpty(couponHistoryList)) {
            SmsCouponHistory couponHistory = couponHistoryList.get(0);
            couponHistory.setUseTime(new Date());
            couponHistory.setUseStatus(useStatus);
            couponHistoryMapper.updateByPrimaryKeySelective(couponHistory);
        }
    }

    private void handleRealAmount(List<OmsOrderItem> orderItemList) {
        for (OmsOrderItem orderItem : orderItemList) {
            BigDecimal realAmount = orderItem.getProductPrice()
                    .subtract(orderItem.getPromotionAmount())
                    .subtract(orderItem.getCouponAmount())
                    .subtract(orderItem.getIntegrationAmount());
            orderItem.setRealAmount(realAmount);
        }
    }

    private String getOrderPromotionInfo(List<OmsOrderItem> orderItemList) {
        StringBuilder sb = new StringBuilder();
        for (OmsOrderItem orderItem : orderItemList) {
            sb.append(orderItem.getPromotionName());
            sb.append(";");
        }
        String result = sb.toString();
        if (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private BigDecimal calcPayAmount(OmsOrder order) {
        BigDecimal payAmount = order.getTotalAmount()
                .add(order.getFreightAmount())
                .subtract(order.getPromotionAmount())
                .subtract(order.getCouponAmount())
                .subtract(order.getIntegrationAmount());
        return payAmount;
    }

    private BigDecimal calcIntegrationAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal integrationAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getIntegrationAmount() != null) {
                integrationAmount = integrationAmount.add(orderItem.getIntegrationAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return integrationAmount;
    }

    private BigDecimal calcCouponAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal couponAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getCouponAmount() != null) {
                couponAmount = couponAmount.add(orderItem.getCouponAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return couponAmount;
    }

    private BigDecimal calcPromotionAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal promotionAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getPromotionAmount() != null) {
                promotionAmount = promotionAmount.add(orderItem.getPromotionAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return promotionAmount;
    }

    private BigDecimal getUseIntegrationAmount(Integer useIntegration, BigDecimal totalAmount, MemberDTO currentMember, boolean hasCoupon) {
        BigDecimal zeroAmount = new BigDecimal(0);
        if (useIntegration.compareTo(currentMember.getIntegration()) > 0) {
            return zeroAmount;
        }
        UmsIntegrationConsumeSetting integrationConsumeSetting = integrationConsumeSettingMapper.selectByPrimaryKey(1L);
        if (hasCoupon && integrationConsumeSetting.getCouponStatus().equals(0)) {
            return zeroAmount;
        }
        if (useIntegration.compareTo(integrationConsumeSetting.getUseUnit()) < 0) {
            return zeroAmount;
        }
        BigDecimal integrationAmount = new BigDecimal(useIntegration).divide(new BigDecimal(integrationConsumeSetting.getUseUnit()), 2, RoundingMode.HALF_EVEN);
        BigDecimal maxPercent = new BigDecimal(integrationConsumeSetting.getMaxPercentPerOrder()).divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN);
        if (integrationAmount.compareTo(totalAmount.multiply(maxPercent)) > 0) {
            return zeroAmount;
        }
        return integrationAmount;
    }

    private void handleCouponAmount(List<OmsOrderItem> orderItemList, CouponHistoryDetailDTO couponHistoryDetail) {
        CouponDTO coupon = couponHistoryDetail.getCoupon();
        if (coupon.getUseType().equals(0)) {
            calcPerCouponAmount(orderItemList, coupon);
        } else if (coupon.getUseType().equals(1)) {
            List<OmsOrderItem> couponOrderItemList = getCouponOrderItemByRelation(couponHistoryDetail, orderItemList, 0);
            calcPerCouponAmount(couponOrderItemList, coupon);
        } else if (coupon.getUseType().equals(2)) {
            List<OmsOrderItem> couponOrderItemList = getCouponOrderItemByRelation(couponHistoryDetail, orderItemList, 1);
            calcPerCouponAmount(couponOrderItemList, coupon);
        }
    }

    private void calcPerCouponAmount(List<OmsOrderItem> orderItemList, CouponDTO coupon) {
        BigDecimal totalAmount = calcTotalAmount(orderItemList);
        for (OmsOrderItem orderItem : orderItemList) {
            BigDecimal couponAmount = orderItem.getProductPrice().divide(totalAmount, 3, RoundingMode.HALF_EVEN).multiply(coupon.getAmount());
            orderItem.setCouponAmount(couponAmount);
        }
    }

    private List<OmsOrderItem> getCouponOrderItemByRelation(CouponHistoryDetailDTO couponHistoryDetail, List<OmsOrderItem> orderItemList, int type) {
        List<OmsOrderItem> result = new ArrayList<>();
        if (type == 0) {
            List<Long> categoryIdList = new ArrayList<>();
            for (CouponProductCategoryRelationDTO productCategoryRelation : couponHistoryDetail.getCategoryRelationList()) {
                categoryIdList.add(productCategoryRelation.getProductCategoryId());
            }
            for (OmsOrderItem orderItem : orderItemList) {
                if (categoryIdList.contains(orderItem.getProductCategoryId())) {
                    result.add(orderItem);
                } else {
                    orderItem.setCouponAmount(new BigDecimal(0));
                }
            }
        } else if (type == 1) {
            List<Long> productIdList = new ArrayList<>();
            for (CouponProductRelationDTO productRelation : couponHistoryDetail.getProductRelationList()) {
                productIdList.add(productRelation.getProductId());
            }
            for (OmsOrderItem orderItem : orderItemList) {
                if (productIdList.contains(orderItem.getProductId())) {
                    result.add(orderItem);
                } else {
                    orderItem.setCouponAmount(new BigDecimal(0));
                }
            }
        }
        return result;
    }

    private CouponHistoryDetailDTO getUseCoupon(List<CartPromotionItemDTO> cartPromotionItemList, Long couponId) {
        List<CouponHistoryDetailDTO> couponHistoryDetailList = memberCouponClient.listCart(cartPromotionItemList, 1).getData();
        for (CouponHistoryDetailDTO couponHistoryDetail : couponHistoryDetailList) {
            if (couponHistoryDetail.getCoupon().getId().equals(couponId)) {
                return couponHistoryDetail;
            }
        }
        return null;
    }

    private BigDecimal calcTotalAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsOrderItem item : orderItemList) {
            totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getProductQuantity())));
        }
        return totalAmount;
    }

    private void lockStock(List<CartPromotionItemDTO> cartPromotionItemList) {
        for (CartPromotionItemDTO cartPromotionItem : cartPromotionItemList) {
            PmsSkuStock skuStock = skuStockMapper.selectByPrimaryKey(cartPromotionItem.getProductSkuId());
            skuStock.setLockStock(skuStock.getLockStock() + cartPromotionItem.getQuantity());
            skuStockMapper.updateByPrimaryKeySelective(skuStock);
        }
    }

    private boolean hasStock(List<CartPromotionItemDTO> cartPromotionItemList) {
        for (CartPromotionItemDTO cartPromotionItem : cartPromotionItemList) {
            if (cartPromotionItem.getRealStock()==null
                    ||cartPromotionItem.getRealStock() <= 0
                    || cartPromotionItem.getRealStock() < cartPromotionItem.getQuantity())
            {
                return false;
            }
        }
        return true;
    }

    private ConfirmOrderResult.CalcAmount calcCartAmount(List<CartPromotionItemDTO> cartPromotionItemList) {
        ConfirmOrderResult.CalcAmount calcAmount = new ConfirmOrderResult.CalcAmount();
        calcAmount.setFreightAmount(new BigDecimal(0));
        BigDecimal totalAmount = new BigDecimal("0");
        BigDecimal promotionAmount = new BigDecimal("0");
        for (CartPromotionItemDTO cartPromotionItem : cartPromotionItemList) {
            totalAmount = totalAmount.add(cartPromotionItem.getPrice().multiply(new BigDecimal(cartPromotionItem.getQuantity())));
            promotionAmount = promotionAmount.add(cartPromotionItem.getReduceAmount().multiply(new BigDecimal(cartPromotionItem.getQuantity())));
        }
        calcAmount.setTotalAmount(totalAmount);
        calcAmount.setPromotionAmount(promotionAmount);
        calcAmount.setPayAmount(totalAmount.subtract(promotionAmount));
        return calcAmount;
    }

}
