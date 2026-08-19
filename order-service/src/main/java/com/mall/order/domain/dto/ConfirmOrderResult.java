package com.mall.order.domain.dto;

import com.mall.api.dto.CartItemDetailDTO;
import com.mall.api.dto.CouponHistoryDetailDTO;
import com.mall.api.dto.MemberAddressDTO;
import com.mall.api.dto.IntegrationConsumeSettingDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 确认单信息封装
 */
@Data
public class ConfirmOrderResult {
    //包含优惠信息的购物车信息
    private List<CartItemDetailDTO> cartPromotionItemList;
    //用户收货地址列表
    private List<MemberAddressDTO> memberReceiveAddressList;
    //用户可用优惠券列表
    private List<CouponHistoryDetailDTO> couponHistoryDetailList;
    //积分使用规则
    private IntegrationConsumeSettingDTO integrationConsumeSetting;
    //会员持有的积分
    private Integer memberIntegration;
    //计算的金额
    private CalcAmount calcAmount;

    public static class CalcAmount{
        //订单商品总金额
        private BigDecimal totalAmount;
        //运费
        private BigDecimal freightAmount;
        //活动优惠
        private BigDecimal promotionAmount;
        //应付金额
        private BigDecimal payAmount;

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public BigDecimal getFreightAmount() {
            return freightAmount;
        }

        public void setFreightAmount(BigDecimal freightAmount) {
            this.freightAmount = freightAmount;
        }

        public BigDecimal getPromotionAmount() {
            return promotionAmount;
        }

        public void setPromotionAmount(BigDecimal promotionAmount) {
            this.promotionAmount = promotionAmount;
        }

        public BigDecimal getPayAmount() {
            return payAmount;
        }

        public void setPayAmount(BigDecimal payAmount) {
            this.payAmount = payAmount;
        }
    }
}

