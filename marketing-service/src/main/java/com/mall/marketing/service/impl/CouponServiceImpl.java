package com.mall.marketing.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageHelper;
import com.mym.mall.common.exception.Asserts;
import com.mall.marketing.domain.dto.SmsCouponHistoryDetail;
import com.mall.marketing.domain.dto.SmsCouponParam;
import com.mall.marketing.mapper.*;
import com.mall.marketing.model.*;
import com.mall.marketing.service.ICouponService;
import com.mall.marketing.util.StpMemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 优惠券管理Service实现类
 * Created by macro on 2018/8/28.
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {
    /** 优惠券Mapper */
    private final SmsCouponMapper couponMapper;
    /** 优惠券商品分类关联Mapper */
    private final SmsCouponProductCategoryRelationMapper productCategoryRelationMapper;
    /** 优惠券商品关联Mapper */
    private final SmsCouponProductRelationMapper productRelationMapper;
    /** 优惠券领取历史Mapper */
    private final SmsCouponHistoryMapper couponHistoryMapper;
    /** 商品Mapper（用于查询商品分类ID） */
    private final PmsProductMapper productMapper;

    // ==================== 管理端 ====================

    @Override
    public int create(SmsCouponParam couponParam) {
        couponParam.setCount(couponParam.getPublishCount());
        couponParam.setUseCount(0);
        couponParam.setReceiveCount(0);
        int count = couponMapper.insert(couponParam);
        if(couponParam.getUseType().equals(2)){
            for(SmsCouponProductRelation productRelation:couponParam.getProductRelationList()){
                productRelation.setCouponId(couponParam.getId());
            }
            productRelationMapper.insertList(couponParam.getProductRelationList());
        }
        if(couponParam.getUseType().equals(1)){
            for (SmsCouponProductCategoryRelation couponProductCategoryRelation : couponParam.getProductCategoryRelationList()) {
                couponProductCategoryRelation.setCouponId(couponParam.getId());
            }
            productCategoryRelationMapper.insertList(couponParam.getProductCategoryRelationList());
        }
        return count;
    }

    @Override
    public int delete(Long id) {
        int count = couponMapper.deleteByPrimaryKey(id);
        deleteProductRelation(id);
        deleteProductCategoryRelation(id);
        return count;
    }

    private void deleteProductCategoryRelation(Long id) {
        SmsCouponProductCategoryRelation condition = new SmsCouponProductCategoryRelation();
        condition.setCouponId(id);
        productCategoryRelationMapper.deleteByCondition(condition);
    }

    private void deleteProductRelation(Long id) {
        SmsCouponProductRelation condition = new SmsCouponProductRelation();
        condition.setCouponId(id);
        productRelationMapper.deleteByCondition(condition);
    }

    @Override
    public int update(Long id, SmsCouponParam couponParam) {
        couponParam.setId(id);
        int count =couponMapper.updateByPrimaryKey(couponParam);
        if(couponParam.getUseType().equals(2)){
            for(SmsCouponProductRelation productRelation:couponParam.getProductRelationList()){
                productRelation.setCouponId(couponParam.getId());
            }
            deleteProductRelation(id);
            productRelationMapper.insertList(couponParam.getProductRelationList());
        }
        if(couponParam.getUseType().equals(1)){
            for (SmsCouponProductCategoryRelation couponProductCategoryRelation : couponParam.getProductCategoryRelationList()) {
                couponProductCategoryRelation.setCouponId(couponParam.getId());
            }
            deleteProductCategoryRelation(id);
            productCategoryRelationMapper.insertList(couponParam.getProductCategoryRelationList());
        }
        return count;
    }

    @Override
    public List<SmsCoupon> list(String name, Integer type, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        SmsCoupon condition = new SmsCoupon();
        if(!StringUtils.isEmpty(name)){
            condition.setName("%"+name+"%");
        }
        if(type!=null){
            condition.setType(type);
        }
        return couponMapper.selectByCondition(condition);
    }

    @Override
    public SmsCouponParam getItem(Long id) {
        return couponMapper.getItem(id);
    }

    // ==================== 用户端 ====================

    @Override
    public void add(Long couponId) {
        Long currentMemberId = StpMemberUtil.getLoginIdAsLong();
        SmsCoupon coupon = couponMapper.selectByPrimaryKey(couponId);
        if(coupon==null){
            Asserts.fail("优惠券不存在");
        }
        if(coupon.getCount()<=0){
            Asserts.fail("优惠券已经领完了");
        }
        Date now = new Date();
        if(now.before(coupon.getEnableTime())){
            Asserts.fail("优惠券还没到领取时间");
        }
        SmsCouponHistory condition = new SmsCouponHistory();
        condition.setCouponId(couponId);
        condition.setMemberId(currentMemberId);
        List<SmsCouponHistory> historyList = couponHistoryMapper.selectByCondition(condition);
        if(historyList.size()>=coupon.getPerLimit()){
            Asserts.fail("您已经领取过该优惠券");
        }
        SmsCouponHistory couponHistory = new SmsCouponHistory();
        couponHistory.setCouponId(couponId);
        couponHistory.setCouponCode(generateCouponCode(currentMemberId));
        couponHistory.setCreateTime(now);
        couponHistory.setMemberId(currentMemberId);
        // 查询会员昵称需要 Feign 调用 member-service，此处简化处理
        couponHistory.setMemberNickname("会员" + currentMemberId);
        couponHistory.setGetType(1);
        couponHistory.setUseStatus(0);
        couponHistoryMapper.insert(couponHistory);
        coupon.setCount(coupon.getCount()-1);
        coupon.setReceiveCount(coupon.getReceiveCount()==null?1:coupon.getReceiveCount()+1);
        couponMapper.updateByPrimaryKey(coupon);
    }

    private String generateCouponCode(Long memberId) {
        StringBuilder sb = new StringBuilder();
        Long currentTimeMillis = System.currentTimeMillis();
        String timeMillisStr = currentTimeMillis.toString();
        sb.append(timeMillisStr.substring(timeMillisStr.length() - 8));
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        String memberIdStr = memberId.toString();
        if (memberIdStr.length() <= 4) {
            sb.append(String.format("%04d", memberId));
        } else {
            sb.append(memberIdStr.substring(memberIdStr.length()-4));
        }
        return sb.toString();
    }

    @Override
    public List<SmsCouponHistory> listHistory(Integer useStatus) {
        Long currentMemberId = StpMemberUtil.getLoginIdAsLong();
        SmsCouponHistory condition = new SmsCouponHistory();
        condition.setMemberId(currentMemberId);
        if(useStatus!=null){
            condition.setUseStatus(useStatus);
        }
        return couponHistoryMapper.selectByCondition(condition);
    }

    @Override
    public List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type) {
        Long currentMemberId = StpMemberUtil.getLoginIdAsLong();
        Date now = new Date();
        List<SmsCouponHistoryDetail> allList = couponHistoryMapper.getDetailList(currentMemberId);
        List<SmsCouponHistoryDetail> enableList = new ArrayList<>();
        List<SmsCouponHistoryDetail> disableList = new ArrayList<>();
        for (SmsCouponHistoryDetail couponHistoryDetail : allList) {
            Integer useType = couponHistoryDetail.getCoupon().getUseType();
            BigDecimal minPoint = couponHistoryDetail.getCoupon().getMinPoint();
            Date endTime = couponHistoryDetail.getCoupon().getEndTime();
            if(useType.equals(0)){
                BigDecimal totalAmount = calcTotalAmount(cartItemList);
                if(now.before(endTime)&&totalAmount.subtract(minPoint).intValue()>=0){
                    enableList.add(couponHistoryDetail);
                }else{
                    disableList.add(couponHistoryDetail);
                }
            }else if(useType.equals(1)){
                List<Long> productCategoryIds = new ArrayList<>();
                for (SmsCouponProductCategoryRelation categoryRelation : couponHistoryDetail.getCategoryRelationList()) {
                    productCategoryIds.add(categoryRelation.getProductCategoryId());
                }
                BigDecimal totalAmount = calcTotalAmountByproductCategoryId(cartItemList,productCategoryIds);
                if(now.before(endTime)&&totalAmount.intValue()>0&&totalAmount.subtract(minPoint).intValue()>=0){
                    enableList.add(couponHistoryDetail);
                }else{
                    disableList.add(couponHistoryDetail);
                }
            }else if(useType.equals(2)){
                List<Long> productIds = new ArrayList<>();
                for (SmsCouponProductRelation productRelation : couponHistoryDetail.getProductRelationList()) {
                    productIds.add(productRelation.getProductId());
                }
                BigDecimal totalAmount = calcTotalAmountByProductId(cartItemList,productIds);
                if(now.before(endTime)&&totalAmount.intValue()>0&&totalAmount.subtract(minPoint).intValue()>=0){
                    enableList.add(couponHistoryDetail);
                }else{
                    disableList.add(couponHistoryDetail);
                }
            }
        }
        if(type.equals(1)){
            return enableList;
        }else{
            return disableList;
        }
    }

    @Override
    public List<SmsCoupon> listByProduct(Long productId) {
        List<Long> allCouponIds = new ArrayList<>();
        SmsCouponProductRelation cprCondition = new SmsCouponProductRelation();
        cprCondition.setProductId(productId);
        List<SmsCouponProductRelation> cprList = productRelationMapper.selectByCondition(cprCondition);
        if(CollUtil.isNotEmpty(cprList)){
            List<Long> couponIds = cprList.stream().map(SmsCouponProductRelation::getCouponId).collect(Collectors.toList());
            allCouponIds.addAll(couponIds);
        }
        PmsProduct product = productMapper.selectByPrimaryKey(productId);
        if(product != null) {
            SmsCouponProductCategoryRelation cpcrCondition = new SmsCouponProductCategoryRelation();
            cpcrCondition.setProductCategoryId(product.getProductCategoryId());
            List<SmsCouponProductCategoryRelation> cpcrList = productCategoryRelationMapper.selectByCondition(cpcrCondition);
            if(CollUtil.isNotEmpty(cpcrList)){
                List<Long> couponIds = cpcrList.stream().map(SmsCouponProductCategoryRelation::getCouponId).collect(Collectors.toList());
                allCouponIds.addAll(couponIds);
            }
        }
        if(CollUtil.isEmpty(allCouponIds)){
            return new ArrayList<>();
        }
        Date now = new Date();
        List<SmsCoupon> allCoupons = couponMapper.selectByCondition(null);
        List<SmsCoupon> filteredCoupons = allCoupons.stream()
                .filter(coupon -> {
                    boolean timeValid = coupon.getEndTime() != null && coupon.getStartTime() != null
                            && now.before(coupon.getEndTime()) && now.after(coupon.getStartTime());
                    if (coupon.getUseType() == 0) {
                        return timeValid;
                    } else {
                        return timeValid && allCouponIds.contains(coupon.getId());
                    }
                })
                .collect(Collectors.toList());
        return filteredCoupons;
    }

    @Override
    public List<SmsCoupon> listMemberCoupons(Integer useStatus) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        return couponHistoryMapper.getCouponList(memberId, useStatus);
    }

    @Override
    public void updateCouponStatus(Long couponId, Long memberId, Integer useStatus) {
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

    private BigDecimal calcTotalAmount(List<CartPromotionItem> cartItemList) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
            total=total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }

    private BigDecimal calcTotalAmountByproductCategoryId(List<CartPromotionItem> cartItemList,List<Long> productCategoryIds) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            if(productCategoryIds.contains(item.getProductCategoryId())){
                BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
                total=total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return total;
    }

    private BigDecimal calcTotalAmountByProductId(List<CartPromotionItem> cartItemList,List<Long> productIds) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            if(productIds.contains(item.getProductId())){
                BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
                total=total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return total;
    }
}
