package com.mall.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageHelper;
import com.mall.api.client.cart.CartClient;
import com.mall.api.client.member.MemberAddressClient;
import com.mall.api.client.member.MemberClient;
import com.mall.api.client.marketing.MarketingCouponClient;
import com.mall.api.client.product.ProductClient;
import com.mall.api.client.product.SkuStockClient;
import com.mall.api.config.FeignAuthContext;
import com.mall.api.dto.SkuStockDTO;
import com.mall.order.mapper.*;
import io.seata.spring.annotation.GlobalTransactional;
import com.mall.api.dto.*;
import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.exception.Asserts;
import com.mym.mall.common.service.RedisService;
import com.mall.order.domain.dto.BuyNowParam;
import com.mall.order.model.OmsOrderSetting;
import com.mall.order.model.OmsOrder;
import com.mall.order.model.OmsOrderItem;
import com.mall.order.domain.dto.ConfirmOrderResult;
import com.mall.order.domain.dto.OmsOrderDetail;
import com.mall.order.domain.dto.OrderParam;
import com.mall.order.service.IPortalOrderService;
import com.mall.order.service.ISeckillOrderService;
import com.mall.order.mq.CancelOrderSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 前台订单服务实现 —— 三种下单模式的核心编排层
 */
@Service
@RequiredArgsConstructor
public class PortalOrderServiceImpl implements IPortalOrderService {

    /** 会员服务Feign客户端 */
    private final MemberClient memberClient;
    /** SKU库存服务Feign客户端 */
    private final SkuStockClient skuStockClient;
    /** 商品服务 Feign */
    private final ProductClient productClient;
    /** 购物车服务Feign客户端 */
    private final CartClient cartClient;
    /** 收货地址服务Feign客户端 */
    private final MemberAddressClient memberAddressClient;
    /** 会员优惠券服务Feign客户端 */
    private final MarketingCouponClient marketingCouponClient;
    /** 订单Mapper */
    private final OmsOrderMapper orderMapper;
    /** 前台订单商品项Mapper */
    private final PortalOrderItemMapper portalOrderItemMapper;
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

    /** 订单确认单并行查询线程池 */
    @Autowired
    @Qualifier("orderConfirmExecutor")
    private Executor orderConfirmExecutor;

    /**
     * 根据勾选的购物车 ID 生成确认单预览
     */
    @Override
    public ConfirmOrderResult generateConfirmOrder(List<Long> cartIds) {
        ConfirmOrderResult result = new ConfirmOrderResult();
        // 获取当前登录会员
        MemberDTO currentMember = memberClient.getCurrentMember().getData();

        // 查询购物车商品、收货地址、积分规则（异步并行，token 需显式传入子线程）
        CompletableFuture<List<CartItemDetailDTO>> cartFuture =
                asyncWithToken(() -> cartClient.listCart(currentMember.getId(), cartIds).getData());
        CompletableFuture<List<MemberAddressDTO>> addressFuture =
                asyncWithToken(() -> memberAddressClient.list().getData());
        CompletableFuture<IntegrationConsumeSettingDTO> integSettingFuture =
                asyncWithToken(() -> memberClient.getIntegrationConsumeSetting().getData());
        CompletableFuture.allOf(cartFuture, addressFuture, integSettingFuture).join();

        List<CartItemDetailDTO> cartList = cartFuture.join();
        result.setCartPromotionItemList(cartList);
        result.setMemberReceiveAddressList(addressFuture.join());
        result.setIntegrationConsumeSetting(integSettingFuture.join());

        // 会员积分
        result.setMemberIntegration(currentMember.getIntegration());
        // 查询可用优惠券
        List<CouponHistoryDetailDTO> coupons = marketingCouponClient.listCart(cartList, 1).getData();
        result.setCouponHistoryDetailList(coupons);

        // 计算商品金额
        result.setCalcAmount(calcCartAmount(cartList));

        return result;
    }

    // 生成 buyNow 确认单
    @Override
    public ConfirmOrderResult buyNowConfirm(BuyNowParam buyNowParam) {
        ConfirmOrderResult result = new ConfirmOrderResult();
        // 1. 获取当前登录会员
        MemberDTO currentMember = memberClient.getCurrentMember().getData();
        // 2. 将前端单品参数构建为一条虚拟购物车记录
        CartItemDetailDTO virtualItem = buildBuyNowItem(buyNowParam);
        // 查商品真实价格（价格防篡改 + 上下架校验）
        ProductDTO product = validateProduct(buyNowParam.getProductId());
        virtualItem.setPrice(product.getPrice());
        List<CartItemDetailDTO> cartList = List.of(virtualItem);
        result.setCartPromotionItemList(cartList);

        // 3. 查询收货地址、可用优惠券、积分规则（异步并行，token 需显式传入子线程）
        CompletableFuture<List<MemberAddressDTO>> addressFuture =
                asyncWithToken(() -> memberAddressClient.list().getData());
        CompletableFuture<List<CouponHistoryDetailDTO>> couponFuture =
                asyncWithToken(() -> marketingCouponClient.listCart(cartList, 1).getData());
        CompletableFuture<IntegrationConsumeSettingDTO> integSettingFuture =
                asyncWithToken(() -> memberClient.getIntegrationConsumeSetting().getData());

        // 4. 会员积分
        result.setMemberIntegration(currentMember.getIntegration());

        // 5. 等待并行任务完成并汇总
        CompletableFuture.allOf(addressFuture, couponFuture, integSettingFuture).join();
        result.setMemberReceiveAddressList(addressFuture.join());
        result.setCouponHistoryDetailList(couponFuture.join());
        result.setIntegrationConsumeSetting(integSettingFuture.join());

        // 6. 计算商品金额合计
        result.setCalcAmount(calcCartAmount(cartList));
        return result;
    }

    /**
     * 读取当前请求的登录 token（主线程内可用）
     * <p>order-service 未引入 Sa-Token，登录态由 member-service 统一校验，
     * 因此这里直接从 Servlet 请求上下文取 Authorization 头，透传给下游。</p>
     */
    private String getCurrentToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest().getHeader("Authorization");
    }

    /**
     * 在确认单线程池中异步执行任务，并把当前登录 token 显式传给子线程
     */
    private <T> CompletableFuture<T> asyncWithToken(Supplier<T> supplier) {
        String token = getCurrentToken();
        return CompletableFuture.supplyAsync(() -> {
            FeignAuthContext.setToken(token);
            try {
                return supplier.get();
            } finally {
                FeignAuthContext.clear();
            }
        }, orderConfirmExecutor);
    }

    //  提交购物车订单
    @Override
    @GlobalTransactional(timeoutMills = 300000, name = "order-generate-order")
    public Map<String, Object> generateOrder(OrderParam orderParam) {
        List<OmsOrderItem> orderItemList = new ArrayList<>();
        // 校验是否选择收货地址
        if(orderParam.getMemberReceiveAddressId()==null){
            Asserts.fail("请选择收货地址！");
        }
        // 获取会员信息 + 购物车商品
        MemberDTO currentMember = memberClient.getCurrentMember().getData();
        List<CartItemDetailDTO> cartPromotionItemList = cartClient.listCart(currentMember.getId(), orderParam.getCartIds()).getData();
        // 批量查询商品库存
        fetchStocksForCart(cartPromotionItemList);
        for (CartItemDetailDTO item : cartPromotionItemList) {
            OmsOrderItem orderItem = new OmsOrderItem();
            BeanUtils.copyProperties(item, orderItem);
            orderItem.setProductPrice(item.getPrice());
            orderItem.setPromotionAmount(BigDecimal.ZERO);
            orderItem.setPromotionName("");
            orderItemList.add(orderItem);
        }
        // 库存校验
        if (!hasStock(cartPromotionItemList)) {
            Asserts.fail("库存不足，无法下单");
        }
        // 优惠券处理：校验优惠券是否可用 → 按商品金额分摊
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
        // 积分抵扣：校验积分可用性 → 按商品金额分摊
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
        // 计算实付金额（商品价格 - 优惠券 - 积分）
        handleRealAmount(orderItemList);
        // 锁定 SKU 库存
        lockStock(cartPromotionItemList);
        // 构建订单主表数据
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
        // 从收货地址表填充收件人信息
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
        // 生成订单号 + 计算自动确认收货天数
        order.setOrderSn(generateOrderSn(order));
        List<OmsOrderSetting> orderSettings = orderSettingMapper.selectByCondition(new OmsOrderSetting());
        if(CollUtil.isNotEmpty(orderSettings)){
            order.setAutoConfirmDay(orderSettings.get(0).getConfirmOvertime());
        }
        // 插入订单主表 + 批量插入订单明细
        orderMapper.insert(order);
        for (OmsOrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(order.getOrderSn());
        }
        portalOrderItemMapper.insertList(orderItemList);
        // 标记优惠券已使用 / 扣减会员积分 / 清理已下单的购物车记录
        if (orderParam.getCouponId() != null) {
            updateCouponStatus(orderParam.getCouponId(), currentMember.getId(), 1);
        }
        if (orderParam.getUseIntegration() != null) {
            order.setUseIntegration(orderParam.getUseIntegration());
            if(currentMember.getIntegration()==null){
                currentMember.setIntegration(0);
            }
            memberClient.updateIntegration(currentMember.getId(), currentMember.getIntegration() - orderParam.getUseIntegration(), 0);
        }
        deleteCartItemList(cartPromotionItemList, currentMember);
        // 发送延时消息：超时未支付自动取消订单
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
        for (OmsOrderItem orderItem : orderDetail.getOrderItemList()) {
            skuStockClient.paySuccessDeductStock(orderItem.getProductSkuId(), orderItem.getProductQuantity());
        }
        return orderDetail.getOrderItemList().size();
    }

    @Override
    @GlobalTransactional(timeoutMills = 300000, name = "order-cancel-timeout")
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
            for (OmsOrderItem orderItem : timeOutOrder.getOrderItemList()) {
                skuStockClient.releaseStock(orderItem.getProductSkuId(), orderItem.getProductQuantity());
            }
            updateCouponStatus(timeOutOrder.getCouponId(), timeOutOrder.getMemberId(), 0);
            if (timeOutOrder.getUseIntegration() != null) {
                MemberDTO member = memberClient.getById(timeOutOrder.getMemberId()).getData();
                memberClient.updateIntegration(timeOutOrder.getMemberId(), member.getIntegration() + timeOutOrder.getUseIntegration(), 0);
            }
        }
        return timeOutOrders.size();
    }

    @Override
    public void cancelOrder(Long orderId) {
        OmsOrder condition = new OmsOrder();
        condition.setId(orderId);
        condition.setStatus(0);
        condition.setDeleteStatus(0);
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
                for (OmsOrderItem orderItem : orderItemList) {
                    skuStockClient.releaseStock(orderItem.getProductSkuId(), orderItem.getProductQuantity());
                }
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
                memberClient.updateIntegration(cancelOrder.getMemberId(), member.getIntegration() + cancelOrder.getUseIntegration(), 0);
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

    private void deleteCartItemList(List<CartItemDetailDTO> cartPromotionItemList, MemberDTO currentMember) {
        List<Long> ids = new ArrayList<>();
        for (CartItemDetailDTO cartPromotionItem : cartPromotionItemList) {
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
        marketingCouponClient.updateCouponStatus(couponId, memberId, useStatus);
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
        IntegrationConsumeSettingDTO integrationConsumeSetting = memberClient.getIntegrationConsumeSetting().getData();
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

    private CouponHistoryDetailDTO getUseCoupon(List<CartItemDetailDTO> cartPromotionItemList, Long couponId) {
        List<CouponHistoryDetailDTO> couponHistoryDetailList = marketingCouponClient.listCart(cartPromotionItemList, 1).getData();
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

    // ════════════════════════════════════════════════════
    //  模式1+2 共用：库存 / 金额 / 优惠券 / 积分 / 订单号
    // ════════════════════════════════════════════════════

    /** 批量锁定 SKU 库存 — 订单创建成功后调用，任意失败 Seata 回滚 */
    private void lockStock(List<CartItemDetailDTO> cartPromotionItemList) {
        for (CartItemDetailDTO cartPromotionItem : cartPromotionItemList) {
            skuStockClient.lockStock(cartPromotionItem.getProductSkuId(), cartPromotionItem.getQuantity());
        }
    }

    private boolean hasStock(List<CartItemDetailDTO> cartPromotionItemList) {
        for (CartItemDetailDTO cartPromotionItem : cartPromotionItemList) {
            if (cartPromotionItem.getRealStock()==null
                    ||cartPromotionItem.getRealStock() <= 0
                    || cartPromotionItem.getRealStock() < cartPromotionItem.getQuantity())
            {
                return false;
            }
        }
        return true;
    }

    private ConfirmOrderResult.CalcAmount calcCartAmount(List<CartItemDetailDTO> cartPromotionItemList) {
        ConfirmOrderResult.CalcAmount calcAmount = new ConfirmOrderResult.CalcAmount();
        calcAmount.setFreightAmount(BigDecimal.ZERO);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemDetailDTO item : cartPromotionItemList) {
            totalAmount = totalAmount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        calcAmount.setTotalAmount(totalAmount);
        // 普通商品无促销减免
        calcAmount.setPromotionAmount(BigDecimal.ZERO);
        calcAmount.setPayAmount(totalAmount);
        return calcAmount;
    }

    // 提交立即购买订单
    @Override
    @GlobalTransactional(timeoutMills = 300000, name = "order-buy-now")
    public Map<String, Object> buyNow(OrderParam orderParam, BuyNowParam buyNowParam) {
        // 价格防篡改 + 上下架校验
        MemberDTO currentMember = memberClient.getCurrentMember().getData();
        ProductDTO product = validateProduct(buyNowParam.getProductId());
        CartItemDetailDTO virtualItem = buildBuyNowItem(buyNowParam);
        virtualItem.setPrice(product.getPrice());
        // 查 SKU 真实库存
        fetchRealStock(virtualItem);
        List<CartItemDetailDTO> cartList = List.of(virtualItem);
        if (orderParam.getMemberReceiveAddressId() == null) Asserts.fail("请选择收货地址！");
        List<OmsOrderItem> orderItemList = new ArrayList<>();
        OmsOrderItem orderItem = new OmsOrderItem();
        BeanUtils.copyProperties(virtualItem, orderItem);
        orderItem.setProductPrice(virtualItem.getPrice());
        orderItem.setPromotionAmount(BigDecimal.ZERO);
        orderItem.setPromotionName("");
        orderItemList.add(orderItem);
        // 库存校验
        if (!hasStock(cartList)) Asserts.fail("库存不足");
        // 优惠券处理
        if (orderParam.getCouponId() == null) {
            orderItem.setCouponAmount(BigDecimal.ZERO);
        } else {
            CouponHistoryDetailDTO coupon = getUseCoupon(cartList, orderParam.getCouponId());
            if (coupon == null) Asserts.fail("该优惠券不可用");
            handleCouponAmount(orderItemList, coupon);
        }
        // 积分抵扣
        if (orderParam.getUseIntegration() == null || orderParam.getUseIntegration() == 0) {
            orderItem.setIntegrationAmount(BigDecimal.ZERO);
        } else {
            BigDecimal totalAmount = calcTotalAmount(orderItemList);
            BigDecimal integAmt = getUseIntegrationAmount(orderParam.getUseIntegration(), totalAmount, currentMember, orderParam.getCouponId() != null);
            if (integAmt.compareTo(BigDecimal.ZERO) == 0) Asserts.fail("积分不可用");
            orderItem.setIntegrationAmount(integAmt);
        }
        // 计算实付金额 + 锁定库存
        handleRealAmount(orderItemList);
        lockStock(cartList);
        // 构建订单 + 生成订单号
        OmsOrder order = buildOrderForBuyNow(orderParam, orderItemList, currentMember);
        orderMapper.insert(order);
        orderItem.setOrderId(order.getId());
        orderItem.setOrderSn(order.getOrderSn());
        portalOrderItemMapper.insertList(orderItemList);
        // 标记优惠券已使用 / 扣减积分
        if (orderParam.getCouponId() != null) {
            marketingCouponClient.updateCouponStatus(orderParam.getCouponId(), currentMember.getId(), 1);
        }
        if (orderParam.getUseIntegration() != null && orderParam.getUseIntegration() > 0) {
            memberClient.updateIntegration(currentMember.getId(), currentMember.getIntegration() - orderParam.getUseIntegration(),0);
        }
        // 发送延时消息：超时未支付自动取消订单
        cancelOrderSender.sendMessage(order.getId(), 60 * 1000);
        Map<String, Object> res = new HashMap<>();
        res.put("order", orderMapper.getDetail(order.getId()));
        res.put("orderItemList", orderItemList);
        return res;
    }


    // 将 buyNow 映射为一条虚拟购物车记录（复用购物车下单的金额计算逻辑）
    private CartItemDetailDTO buildBuyNowItem(BuyNowParam p) {
        CartItemDetailDTO item = new CartItemDetailDTO();
        BeanUtils.copyProperties(p, item);
        // 价格由后端 validateProduct 查询后覆盖，不使用前端传值
        item.setPrice(BigDecimal.ZERO);
        item.setQuantity(p.getQuantity() != null ? p.getQuantity() : 1);
        return item;
    }

    //价格防篡改 + 上下架校验
    private ProductDTO validateProduct(Long productId) {
        ProductDTO product = productClient.getById(productId).getData();
        if (product == null) Asserts.fail("商品不存在");
        if (product.getPublishStatus() == null || product.getPublishStatus() != 1)
            Asserts.fail("商品已下架");
        if (product.getDeleteStatus() != null && product.getDeleteStatus() == 1)
            Asserts.fail("商品已删除");
        return product;
    }

    // 购物车下单·批量查库存
    private void fetchStocksForCart(List<CartItemDetailDTO> items) {
        for (CartItemDetailDTO item : items) {
            fetchRealStock(item);
        }
    }

    // 立即购买查可用库存
    private void fetchRealStock(CartItemDetailDTO item) {
        if (item.getProductId() == null) return;
        try {
            List<SkuStockDTO> skus = skuStockClient.getSkuStockByProductId(item.getProductId()).getData();
            if (skus == null || skus.isEmpty()) {
                item.setRealStock(0);
                return;
            }
            SkuStockDTO target = skus.stream()
                    .filter(s -> item.getProductSkuId() == null || item.getProductSkuId().equals(s.getId()))
                    .findFirst().orElseThrow(() -> new Exception("购物车商品规格不存在，请重新选择"));
            int total = target.getStock() != null ? target.getStock() : 0;
            int locked = target.getLockStock() != null ? target.getLockStock() : 0;
            item.setRealStock(Math.max(total - locked, 0));
        } catch (Exception e) {
            item.setRealStock(0);
        }
    }

    private OmsOrder buildOrderForBuyNow(OrderParam orderParam, List<OmsOrderItem> items, MemberDTO member) {
        OmsOrder o = new OmsOrder();
        o.setDiscountAmount(BigDecimal.ZERO);
        o.setTotalAmount(calcTotalAmount(items));
        o.setFreightAmount(BigDecimal.ZERO);
        o.setPromotionAmount(calcPromotionAmount(items));
        o.setPromotionInfo(getOrderPromotionInfo(items));
        o.setCouponId(orderParam.getCouponId());
        o.setCouponAmount(orderParam.getCouponId() == null ? BigDecimal.ZERO : calcCouponAmount(items));
        o.setIntegration(orderParam.getUseIntegration() == null ? 0 : orderParam.getUseIntegration());
        o.setIntegrationAmount(orderParam.getUseIntegration() == null ? BigDecimal.ZERO : calcIntegrationAmount(items));
        o.setPayAmount(calcPayAmount(o));
        o.setMemberId(member.getId());
        o.setMemberUsername(member.getUsername());
        o.setCreateTime(new Date());
        o.setOrderSn(generateOrderSn(o));
        o.setStatus(0);
        o.setDeleteStatus(0);
        o.setPayType(orderParam.getPayType());
        o.setSourceType(1);
        o.setOrderType(0);
        o.setAutoConfirmDay(15);
        MemberAddressDTO addr = memberAddressClient.getItem(orderParam.getMemberReceiveAddressId()).getData();
        if (addr != null) {
            o.setReceiverName(addr.getName());
            o.setReceiverPhone(addr.getPhoneNumber());
            o.setReceiverPostCode(addr.getPostCode());
            o.setReceiverProvince(addr.getProvince());
            o.setReceiverCity(addr.getCity());
            o.setReceiverRegion(addr.getRegion());
            o.setReceiverDetailAddress(addr.getDetailAddress());
        }
        return o;
    }

}

