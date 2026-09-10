package com.mall.order.service.impl;

import com.mall.api.client.member.MemberAddressClient;
import com.mall.api.client.member.MemberClient;
import com.mall.api.client.product.SkuStockClient;
import com.mall.api.dto.MemberAddressDTO;
import com.mall.api.dto.MemberDTO;
import com.mall.api.dto.SeckillHoldInfo;
import com.mall.api.dto.SkuStockDTO;
import com.mall.order.domain.dto.SeckillConfirmParam;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.model.OmsOrder;
import com.mall.order.model.OmsOrderItem;
import com.mall.order.mq.CancelOrderSender;
import com.mall.order.service.ISeckillOrderService;
import com.mym.mall.common.exception.Asserts;
import com.mym.mall.common.service.RedisService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 秒杀订单服务实现
 * 核心流程：唯一索引幂等（uk_seckill_order）→ 锁定 SKU 库存 → 创建订单 → 延时取消
 */
@Service
@RequiredArgsConstructor
public class SeckillOrderServiceImpl implements ISeckillOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillOrderServiceImpl.class);

    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final SkuStockClient skuStockClient;
    private final MemberClient memberClient;
    private final MemberAddressClient memberAddressClient;
    private final RedisService redisService;
    private final CancelOrderSender cancelOrderSender;

    @Value("${redis.key.orderId}")
    private String REDIS_KEY_ORDER_ID;
    @Value("${redis.database}")
    private String REDIS_DATABASE;

    /**
     * 确认秒杀订单（占坑后选地址提交）
     * 校验占坑标记 → 生成订单（含收货地址）→ 删除占坑标记 → 发延时取消消息
     */
    @Override
    @GlobalTransactional(timeoutMills = 300000, name = "order-seckill-confirm")
    public Long confirmSeckillOrder(SeckillConfirmParam param) {
        Long promotionId = param.getPromotionId();
        Long productId = param.getProductId();

        // 1. 获取当前会员
        MemberDTO currentMember = memberClient.getCurrentMember().getData();
        Long memberId = currentMember.getId();

        // 2. 校验占坑标记
        String holdKey = String.format("seckill:hold:%d:%d:%d", promotionId, productId, memberId);
        Object holdObj = redisService.get(holdKey);
        if (holdObj == null) {
            Asserts.fail("抢购资格已失效或超时，请重新抢购");
        }
        SeckillHoldInfo holdInfo = (SeckillHoldInfo) holdObj;

        // 3. 查 SKU
        SkuStockDTO skuStock = skuStockClient.getSkuStockBySkuId(holdInfo.getSkuId()).getData();
        if (skuStock == null) {
            Asserts.fail("商品SKU不存在");
        }

        // 4. 查收货地址
        MemberAddressDTO address = memberAddressClient.getItem(param.getMemberReceiveAddressId()).getData();
        if (address == null) {
            Asserts.fail("收货地址不存在");
        }
        int quantity = holdInfo.getQuantity() != null ? holdInfo.getQuantity() : 1;

        // 5. 构建订单（含收货地址）
        OmsOrder order = new OmsOrder();
        order.setPromotionId(promotionId);
        order.setSkuId(holdInfo.getSkuId());
        order.setMemberId(memberId);
        order.setMemberUsername(currentMember.getUsername());
        order.setTotalAmount(holdInfo.getSeckillPrice());
        order.setPayAmount(holdInfo.getSeckillPrice());
        order.setFreightAmount(BigDecimal.ZERO);
        order.setPromotionAmount(BigDecimal.ZERO);
        order.setIntegrationAmount(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayType(param.getPayType() != null ? param.getPayType() : 0);
        order.setSourceType(1);
        order.setStatus(0);
        order.setOrderType(1);
        order.setCreateTime(new Date());
        order.setConfirmStatus(0);
        order.setDeleteStatus(0);
        order.setIntegration(0);
        order.setGrowth(0);
        order.setPromotionInfo("秒杀活动ID:" + promotionId);
        // 从收货地址表填充收件人信息
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhoneNumber());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setOrderSn(generateOrderSn(order));

        // 6. 插入订单（唯一索引 uk_seckill_order 幂等守卫）
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            LOGGER.info("重复确认秒杀订单，忽略, promotionId={}, skuId={}, memberId={}",
                    promotionId, holdInfo.getSkuId(), memberId);
            return null;
        }

        // 7. 锁定库存（原子 SQL，可售不足抛异常触发全局回滚）
        skuStockClient.lockStock(skuStock.getId(), quantity);

        // 8. 构建订单商品项
        OmsOrderItem orderItem = new OmsOrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setOrderSn(order.getOrderSn());
        orderItem.setProductId(productId);
        orderItem.setProductName(holdInfo.getProductName());
        orderItem.setProductPrice(holdInfo.getSeckillPrice());
        orderItem.setProductQuantity(quantity);
        orderItem.setProductSkuId(skuStock.getId());
        orderItem.setProductSkuCode(skuStock.getSkuCode());
        orderItem.setPromotionName("秒杀价");
        orderItem.setPromotionAmount(BigDecimal.ZERO);
        orderItem.setCouponAmount(BigDecimal.ZERO);
        orderItem.setIntegrationAmount(BigDecimal.ZERO);
        orderItem.setRealAmount(holdInfo.getSeckillPrice());
        orderItem.setGiftIntegration(0);
        orderItem.setGiftGrowth(0);
        orderItemMapper.insert(orderItem);

        // 9. 删除占坑标记（占坑已转化为订单）
        redisService.del(holdKey);

        // 10. 发送延时取消消息：秒杀订单 5 分钟未支付自动取消
        cancelOrderSender.sendMessage(order.getId(), 5 * 60 * 1000);
        LOGGER.info("秒杀订单确认成功, orderId={}, orderSn={}, memberId={}",
                order.getId(), order.getOrderSn(), memberId);
        return order.getId();
    }

    /**
     * 生成订单编号
     * 格式：日期 + 来源 + 支付方式 + 自增序号
     */
    private String generateOrderSn(OmsOrder order) {
        StringBuilder sb = new StringBuilder();
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ORDER_ID + date;
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

    @Override
    public void rollbackSeckillStock(Long promotionId, Long productId, Long memberId, Integer quantity) {
        String stockKey = String.format("seckill:stock:%d:%d", promotionId, productId);
        String usersKey = String.format("seckill:users:%d:%d", promotionId, productId);

        // 1. 恢复 Redis 库存
        redisService.incr(stockKey, quantity);
        LOGGER.info("秒杀库存已回滚, promotionId={}, productId={}, quantity={}", promotionId, productId, quantity);

        // 2. 从已购买用户集合中移除该用户（允许再次参与秒杀）
        redisService.sRemove(usersKey, memberId);
        LOGGER.info("已移除用户购买记录, memberId={}, 用户可再次参与秒杀", memberId);
    }
}
