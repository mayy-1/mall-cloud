package com.mall.marketing.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageHelper;
import com.mall.api.client.member.MemberClient;
import com.mall.marketing.domain.dto.CartPromotionItem;
import com.mym.mall.common.exception.Asserts;
import com.mall.marketing.domain.dto.SmsCouponDetail;
import com.mall.marketing.domain.dto.SmsCouponHistoryDetail;
import com.mall.marketing.domain.dto.SmsCouponParam;
import com.mall.marketing.mapper.*;
import com.mall.marketing.model.*;
import com.mall.marketing.service.ICouponService;
import com.mall.api.client.product.ProductClient;
import com.mall.api.dto.MemberDTO;
import com.mall.api.dto.ProductDTO;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 优惠券管理 Service 实现
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {

    // ==================== 数据访问层依赖 ====================

    /** 优惠券主表 Mapper */
    private final SmsCouponMapper couponMapper;
    /** 优惠券-商品分类关联表 Mapper */
    private final SmsCouponProductCategoryRelationMapper productCategoryRelationMapper;
    /** 优惠券-商品关联表 Mapper */
    private final SmsCouponProductRelationMapper productRelationMapper;
    /** 优惠券领取历史表 Mapper */
    private final SmsCouponHistoryMapper couponHistoryMapper;

    // ==================== 远程服务依赖 ====================

    /** 商品服务 Feign 客户端（查询商品信息） */
    private final ProductClient productClient;
    /** 会员服务 Feign 客户端（获取当前登录用户） */
    private final MemberClient memberClient;

    // ==================== Redis ====================

    /** Redis 缓存 */
    private final RedisTemplate<String, Object> redisTemplate;
    /** Redisson 客户端（Lua 脚本执行） */
    private final RedissonClient redissonClient;
    /** JSON 序列化（缓存读写） */
    private final ObjectMapper objectMapper;

    /** 优惠券库存键前缀 */
    private static final String COUPON_STOCK_KEY = "coupon:stock:%d";
    /** 优惠券已领用户键前缀 */
    private static final String COUPON_USERS_KEY = "coupon:users:%d";

    /** 查询缓存：可领取优惠券列表 */
    private static final String CACHE_AVAILABLE_KEY = "coupon:list:available";
    /** 查询缓存：会员已领取优惠券 */
    private static final String CACHE_HISTORY_KEY = "coupon:history:%d:%d";
    /** 查询缓存：商品可用优惠券 */
    private static final String CACHE_PRODUCT_KEY = "coupon:product:%d";

    /** 缓存过期时间：可领列表（秒） */
    private static final long TTL_AVAILABLE = 1800;
    /** 缓存过期时间：我的优惠券（秒） */
    private static final long TTL_HISTORY = 600;
    /** 缓存过期时间：商品券（秒） */
    private static final long TTL_PRODUCT = 1800;

    /** Redisson Lua 脚本：领取优惠券 */
    private RScript claimCouponScript;

    /** Lua 返回值：领取成功 */
    private static final int CLAIM_SUCCESS = 1;
    /** Lua 返回值：库存不足 */
    private static final int CLAIM_STOCK_EMPTY = 0;
    /** Lua 返回值：已领取过 */
    private static final int CLAIM_REPEAT = -1;

    @PostConstruct
    public void init() {
        claimCouponScript = redissonClient.getScript(StringCodec.INSTANCE);
    }

    // ==================== 管理端 ====================

    @Override
    @Transactional
    public int create(SmsCouponParam couponParam) {
        // 初始化统计字段
        couponParam.setCount(couponParam.getPublishCount());
        couponParam.setUseCount(0);
        couponParam.setReceiveCount(0);
        int count = couponMapper.insert(couponParam);
        // 写入关联数据
        if (couponParam.getUseType().equals(2)) {
            for (SmsCouponProductRelation productRelation : couponParam.getProductRelationList()) {
                productRelation.setCouponId(couponParam.getId());
            }
            productRelationMapper.insertList(couponParam.getProductRelationList());
        }
        if (couponParam.getUseType().equals(1)) {
            for (SmsCouponProductCategoryRelation couponProductCategoryRelation : couponParam.getProductCategoryRelationList()) {
                couponProductCategoryRelation.setCouponId(couponParam.getId());
            }
            productCategoryRelationMapper.insertList(couponParam.getProductCategoryRelationList());
        }
        return count;
    }

    /**
     * 删除优惠券（物理删除）
     * 同时清理关联表数据，保证数据一致性。
     */
    @Override
    @Transactional
    public int delete(Long id) {
        int count = couponMapper.deleteByPrimaryKey(id);
        deleteProductRelation(id);
        deleteProductCategoryRelation(id);
        // 删除后清除可领列表缓存
        evictAvailableCache();
        return count;
    }

    /** 删除优惠券的商品分类关联 */
    private void deleteProductCategoryRelation(Long id) {
        SmsCouponProductCategoryRelation condition = new SmsCouponProductCategoryRelation();
        condition.setCouponId(id);
        productCategoryRelationMapper.deleteByCondition(condition);
    }

    /** 删除优惠券的商品关联 */
    private void deleteProductRelation(Long id) {
        SmsCouponProductRelation condition = new SmsCouponProductRelation();
        condition.setCouponId(id);
        productRelationMapper.deleteByCondition(condition);
    }

    /**
     * 修改优惠券
     * 先更新主表，再"先删后插"关联表，确保关联数据与当前 useType 匹配。
     */
    @Override
    @Transactional
    public int update(Long id, SmsCouponParam couponParam) {
        couponParam.setId(id);
        int count = couponMapper.updateByPrimaryKey(couponParam);
        // 先删后插，刷新关联数据
        if (couponParam.getUseType().equals(2)) {
            for (SmsCouponProductRelation productRelation : couponParam.getProductRelationList()) {
                productRelation.setCouponId(couponParam.getId());
            }
            deleteProductRelation(id);
            productRelationMapper.insertList(couponParam.getProductRelationList());
        }
        if (couponParam.getUseType().equals(1)) {
            for (SmsCouponProductCategoryRelation couponProductCategoryRelation : couponParam.getProductCategoryRelationList()) {
                couponProductCategoryRelation.setCouponId(couponParam.getId());
            }
            deleteProductCategoryRelation(id);
            productCategoryRelationMapper.insertList(couponParam.getProductCategoryRelationList());
        }
        // 修改后清除可领列表缓存
        evictAvailableCache();
        return count;
    }

    /**
     * 管理端分页查询优惠券
     */
    @Override
    public List<SmsCoupon> list(String name, Integer type, Integer status, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        SmsCoupon condition = new SmsCoupon();
        if (!StringUtils.isEmpty(name)) {
            condition.setName("%" + name + "%");
        }
        if (type != null) {
            condition.setType(type);
        }
        if (status != null) {
            condition.setStatus(status);
        }
        return couponMapper.selectByCondition(condition);
    }

    /**
     * 切换优惠券上下架状态
     */
    @Override
    public int updateStatus(Long id, Integer status) {
        SmsCoupon record = new SmsCoupon();
        record.setId(id);
        record.setStatus(status);
        int count = couponMapper.updateByPrimaryKeySelective(record);
        if (count > 0 && status != null && status == 1) {
            // 上架时同步库存到 Redis
            SmsCoupon coupon = couponMapper.selectByPrimaryKey(id);
            if (coupon == null) return count;
            String stockKey = String.format(COUPON_STOCK_KEY, id);
            redisTemplate.opsForValue().set(stockKey, coupon.getCount() != null ? coupon.getCount() : 0);
        }
        // 状态变更后清除可领列表缓存
        evictAvailableCache();
        return count;
    }

    /**
     * 获取优惠券详情（含关联商品/分类）
     *
     * @param id 优惠券 ID
     * @return 优惠券详情或 null（不存在时）
     */
    @Override
    public SmsCouponParam getItem(Long id) {
        SmsCoupon coupon = couponMapper.selectByPrimaryKey(id);
        if (coupon == null) return null;
        SmsCouponParam result = new SmsCouponParam();
        BeanUtils.copyProperties(coupon, result);
        // 加载关联商品列表（useType=2）
        SmsCouponProductRelation cprCondition = new SmsCouponProductRelation();
        cprCondition.setCouponId(id);
        result.setProductRelationList(productRelationMapper.selectByCondition(cprCondition));
        // 加载关联分类列表（useType=1）
        SmsCouponProductCategoryRelation cpcrCondition = new SmsCouponProductCategoryRelation();
        cpcrCondition.setCouponId(id);
        result.setProductCategoryRelationList(productCategoryRelationMapper.selectByCondition(cpcrCondition));
        return result;
    }

    // ==================== 用户端 ====================

    /**
     * 会员领取优惠券（Redisson + Lua 原子扣库存）
     * @param couponId 优惠券 ID
     */
    @Override
    public void add(Long couponId) {
        MemberDTO currentMember = getCurrentMember();
        Long currentMemberId = currentMember.getId();

        // 1. DB 快照校验：券是否存在、能否领取
        SmsCoupon coupon = couponMapper.selectByPrimaryKey(couponId);
        if (coupon == null) {
            Asserts.fail("优惠券不存在");
        }
        Date now = new Date();
        if (now.before(coupon.getEnableTime())) {
            Asserts.fail("优惠券还没到领取时间");
        }

        // 2. Redisson 执行 Lua 原子脚本：验库存 + 防重复 + 扣库存
        String stockKey = String.format(COUPON_STOCK_KEY, couponId);
        String usersKey = String.format(COUPON_USERS_KEY, couponId);
        String luaScript = readLuaScript("lua/claim_coupon.lua");
        Long result = claimCouponScript.eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER,
                List.of(stockKey, usersKey),
                currentMemberId, coupon.getPerLimit()
        );
        int claimResult = result != null ? result.intValue() : CLAIM_STOCK_EMPTY;

        if (claimResult == CLAIM_STOCK_EMPTY) {
            Asserts.fail("优惠券已经领完了");
        }
        if (claimResult == CLAIM_REPEAT) {
            Asserts.fail("您已经领取过该优惠券");
        }

        // 3. 创建领取记录
        SmsCouponHistory couponHistory = new SmsCouponHistory();
        couponHistory.setCouponId(couponId);
        couponHistory.setCouponCode(UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        couponHistory.setCreateTime(now);
        couponHistory.setMemberId(currentMemberId);
        couponHistory.setMemberNickname(currentMember.getNickname() == null
                ? currentMember.getUsername() : currentMember.getNickname());
        couponHistory.setGetType(1);
        couponHistory.setUseStatus(0);
        couponHistoryMapper.insert(couponHistory);

        // 4. 同步更新 DB 统计
        coupon.setCount(coupon.getCount() - 1);
        coupon.setReceiveCount(coupon.getReceiveCount() == null ? 1 : coupon.getReceiveCount() + 1);
        couponMapper.updateByPrimaryKey(coupon);
        // 清除该会员的历史缓存（下次查询重新加载）
        evictMemberCache(currentMemberId);
    }


    /**
     * 查询当前会员已领取的优惠券历史（Redis 缓存）
     */
    @Override
    public List<SmsCouponHistoryDetail> listHistory(Integer useStatus) {
        Long memberId = getCurrentMember().getId();
        int statusKey = (useStatus != null) ? useStatus : -1;
        String cacheKey = String.format(CACHE_HISTORY_KEY, memberId, statusKey);
        // 1. 查 Redis 缓存
        String cached = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return readCache(cached, new TypeReference<List<SmsCouponHistoryDetail>>() {});
        }
        // 2. 缓存未命中 → 查 DB 并同步缓存
        List<SmsCouponHistoryDetail> list = couponHistoryMapper.getCouponList(memberId, useStatus);
        for (SmsCouponHistoryDetail detail : list) {
            if (detail.getCoupon() == null) continue;
            Long couponId = detail.getCoupon().getId();
            if (couponId == null) continue;
            SmsCouponProductRelation cprCondition = new SmsCouponProductRelation();
            cprCondition.setCouponId(couponId);
            detail.setProductRelationList(productRelationMapper.selectByCondition(cprCondition));
            SmsCouponProductCategoryRelation cpcrCondition = new SmsCouponProductCategoryRelation();
            cpcrCondition.setCouponId(couponId);
            detail.setCategoryRelationList(productCategoryRelationMapper.selectByCondition(cpcrCondition));
        }
        redisTemplate.opsForValue().set(cacheKey, writeCache(list), TTL_HISTORY, java.util.concurrent.TimeUnit.SECONDS);
        return list;
    }

    /**
     * 获取会员购物车可用的优惠券
     * @param cartItemList 购物车商品列表（含价格、数量、分类、商品ID）
     * @param type         返回类型：1-可用列表，其他-不可用列表
     * @return 优惠券详情列表
     */
    @Override
    public List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type) {
        Long currentMemberId = getCurrentMember().getId();
        Date now = new Date();
        List<SmsCouponHistoryDetail> allList = couponHistoryMapper.getDetailList(currentMemberId);
        List<SmsCouponHistoryDetail> enableList = new ArrayList<>();
        List<SmsCouponHistoryDetail> disableList = new ArrayList<>();
        for (SmsCouponHistoryDetail couponHistoryDetail : allList) {
            Integer useType = couponHistoryDetail.getCoupon().getUseType();
            BigDecimal minPoint = couponHistoryDetail.getCoupon().getMinPoint();
            Date endTime = couponHistoryDetail.getCoupon().getEndTime();
            if (useType.equals(0)) {
                // 全场通用：购物车总金额达到门槛即可
                BigDecimal totalAmount = calcTotalAmount(cartItemList);
                if (now.before(endTime) && totalAmount.subtract(minPoint).intValue() >= 0) {
                    enableList.add(couponHistoryDetail);
                } else {
                    disableList.add(couponHistoryDetail);
                }
            } else if (useType.equals(1)) {
                // 指定分类：匹配分类的金额达到门槛
                List<Long> productCategoryIds = new ArrayList<>();
                for (SmsCouponProductCategoryRelation categoryRelation : couponHistoryDetail.getCategoryRelationList()) {
                    productCategoryIds.add(categoryRelation.getProductCategoryId());
                }
                BigDecimal totalAmount = calcTotalAmountByproductCategoryId(cartItemList, productCategoryIds);
                if (now.before(endTime) && totalAmount.intValue() > 0 && totalAmount.subtract(minPoint).intValue() >= 0) {
                    enableList.add(couponHistoryDetail);
                } else {
                    disableList.add(couponHistoryDetail);
                }
            } else if (useType.equals(2)) {
                // 指定商品：匹配商品的金额达到门槛
                List<Long> productIds = new ArrayList<>();
                for (SmsCouponProductRelation productRelation : couponHistoryDetail.getProductRelationList()) {
                    productIds.add(productRelation.getProductId());
                }
                BigDecimal totalAmount = calcTotalAmountByProductId(cartItemList, productIds);
                if (now.before(endTime) && totalAmount.intValue() > 0 && totalAmount.subtract(minPoint).intValue() >= 0) {
                    enableList.add(couponHistoryDetail);
                } else {
                    disableList.add(couponHistoryDetail);
                }
            }
        }
        if (type.equals(1)) {
            return enableList;
        } else {
            return disableList;
        }
    }

    /**
     * 查询指定商品可用的优惠券（Redis 缓存）
     */
    @Override
    public List<SmsCoupon> listByProduct(Long productId) {
        String cacheKey = String.format(CACHE_PRODUCT_KEY, productId);
        String cached = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return readCache(cached, new TypeReference<List<SmsCoupon>>() {});
        }
        List<SmsCoupon> result = buildProductCoupons(productId);
        redisTemplate.opsForValue().set(cacheKey, writeCache(result), TTL_PRODUCT, java.util.concurrent.TimeUnit.SECONDS);
        return result;
    }

    private List<SmsCoupon> buildProductCoupons(Long productId) {
        List<Long> allCouponIds = new ArrayList<>();
        // 查 useType=2 的关联（指定商品）
        SmsCouponProductRelation cprCondition = new SmsCouponProductRelation();
        cprCondition.setProductId(productId);
        List<SmsCouponProductRelation> cprList = productRelationMapper.selectByCondition(cprCondition);
        if (CollUtil.isNotEmpty(cprList)) {
            List<Long> couponIds = cprList.stream().map(SmsCouponProductRelation::getCouponId).collect(Collectors.toList());
            allCouponIds.addAll(couponIds);
        }
        // 查 useType=1 的关联（指定分类）
        ProductDTO product = productClient.getById(productId).getData();
        if (product != null) {
            SmsCouponProductCategoryRelation cpcrCondition = new SmsCouponProductCategoryRelation();
            cpcrCondition.setProductCategoryId(product.getProductCategoryId());
            List<SmsCouponProductCategoryRelation> cpcrList = productCategoryRelationMapper.selectByCondition(cpcrCondition);
            if (CollUtil.isNotEmpty(cpcrList)) {
                List<Long> couponIds = cpcrList.stream().map(SmsCouponProductCategoryRelation::getCouponId).collect(Collectors.toList());
                allCouponIds.addAll(couponIds);
            }
        }
        if (CollUtil.isEmpty(allCouponIds)) {
            return new ArrayList<>();
        }
        // 从全量券中过滤：匹配 useType=0 或 useType=1/2 且在有效期内
        Date now = new Date();
        List<SmsCoupon> allCoupons = couponMapper.selectByCondition(null);
        List<SmsCoupon> filteredCoupons = allCoupons.stream()
                .filter(coupon -> {
                    boolean timeValid = coupon.getEndTime() != null && coupon.getStartTime() != null
                            && now.before(coupon.getEndTime()) && now.after(coupon.getStartTime());
                    if (coupon.getUseType() == 0) {
                        return timeValid; // 全场通用券
                    } else {
                        return timeValid && allCouponIds.contains(coupon.getId());
                    }
                })
                .collect(Collectors.toList());
        return filteredCoupons;
    }



    /**
     * 查询平台所有可领取的优惠券（领券中心，Redis 缓存基础数据 + 每用户标记已领/已抢光）
     */
    @Override
    public List<SmsCouponDetail> listAvailableCoupons() {
        // 1. 查 Redis 缓存（基础数据不含 claimed）
        String cached = (String) redisTemplate.opsForValue().get(CACHE_AVAILABLE_KEY);
        List<SmsCouponDetail> result;
        if (cached != null) {
            result = readCache(cached, new TypeReference<List<SmsCouponDetail>>() {});
        } else {
            result = buildAvailableCoupons();
            redisTemplate.opsForValue().set(CACHE_AVAILABLE_KEY, writeCache(result), TTL_AVAILABLE, java.util.concurrent.TimeUnit.SECONDS);
        }
        // 2. 按当前会员标记已领取 + 库存为0
        markClaimedStatus(result);
        return result;
    }

    /** 标记每张券当前会员是否已领取 + 库存是否为0 */
    private void markClaimedStatus(List<SmsCouponDetail> list) {
        Long memberId = getCurrentMember().getId();
        for (SmsCouponDetail detail : list) {
            if (detail.getCoupon() == null) continue;
            Long couponId = detail.getCoupon().getId();
            String usersKey = String.format(COUPON_USERS_KEY, couponId);
            String stockKey = String.format(COUPON_STOCK_KEY, couponId);
            // O(1) 检查：已领取 或 库存为0
            boolean claimed = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(usersKey, memberId.toString()));
            Object stockObj = redisTemplate.opsForValue().get(stockKey);
            boolean stockEmpty = stockObj == null || "0".equals(String.valueOf(stockObj));
            detail.setClaimed(claimed || stockEmpty);
        }
    }

    /** 从 DB 构建可领优惠券列表并加载关联数据 */
    private List<SmsCouponDetail> buildAvailableCoupons() {
        List<SmsCoupon> coupons = couponMapper.getCouponList();
        List<SmsCouponDetail> result = new ArrayList<>();
        for (SmsCoupon coupon : coupons) {
            SmsCouponDetail detail = new SmsCouponDetail();
            detail.setCoupon(coupon);
            SmsCouponProductRelation cprCondition = new SmsCouponProductRelation();
            cprCondition.setCouponId(coupon.getId());
            detail.setProductRelationList(productRelationMapper.selectByCondition(cprCondition));
            SmsCouponProductCategoryRelation cpcrCondition = new SmsCouponProductCategoryRelation();
            cpcrCondition.setCouponId(coupon.getId());
            detail.setCategoryRelationList(productCategoryRelationMapper.selectByCondition(cpcrCondition));
            result.add(detail);
        }
        return result;
    }

    // ==================== 内部方法 ====================

    /**
     * 获取当前登录会员信息（从 Token 解析）
     */
    private MemberDTO getCurrentMember() {
        MemberDTO member = memberClient.getCurrentMember().getData();
        if (member == null || member.getId() == null) {
            Asserts.fail("暂未登录或token已经过期");
        }
        return member;
    }

    /**
     * 更新优惠券使用状态（Feign 内部调用）
     * <p>
     * 调用方：order-service（下单/取消订单时）。
     *
     * @param couponId  优惠券 ID
     * @param memberId  会员 ID
     * @param useStatus 使用状态：0-未使用，1-已使用
     */
    @Override
    public void updateCouponStatus(Long couponId, Long memberId, Integer useStatus) {
        SmsCouponHistory condition = new SmsCouponHistory();
        condition.setMemberId(memberId);
        condition.setCouponId(couponId);
        // 找当前相反状态的记录（0→1 或 1→0）
        condition.setUseStatus(useStatus == 0 ? 1 : 0);
        List<SmsCouponHistory> couponHistoryList = couponHistoryMapper.selectByCondition(condition);
        if (!CollectionUtils.isEmpty(couponHistoryList)) {
            SmsCouponHistory couponHistory = couponHistoryList.get(0);
            couponHistory.setUseTime(new Date());
            couponHistory.setUseStatus(useStatus);
            couponHistoryMapper.updateByPrimaryKeySelective(couponHistory);
        }
    }

    // ==================== 金额计算工具方法 ====================

    /**
     * 计算购物车商品总金额
     *
     * @param cartItemList 购物车商品列表
     * @return 总金额（扣除单品促销后的实付金额 × 数量）
     */
    private BigDecimal calcTotalAmount(List<CartPromotionItem> cartItemList) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            BigDecimal realPrice = item.getPrice();
            total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }

    /**
     * 计算指定分类下的商品总金额
     *
     * @param cartItemList        购物车商品列表
     * @param productCategoryIds 目标分类 ID 列表
     * @return 匹配分类的金额总和
     */
    private BigDecimal calcTotalAmountByproductCategoryId(List<CartPromotionItem> cartItemList, List<Long> productCategoryIds) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            if (productCategoryIds.contains(item.getProductCategoryId())) {
                BigDecimal realPrice = item.getPrice();
                total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return total;
    }

    /**
     * 计算指定商品的总金额
     *
     * @param cartItemList 购物车商品列表
     * @param productIds   目标商品 ID 列表
     * @return 匹配商品的金额总和
     */
    private BigDecimal calcTotalAmountByProductId(List<CartPromotionItem> cartItemList, List<Long> productIds) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            if (productIds.contains(item.getProductId())) {
                BigDecimal realPrice = item.getPrice();
                total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return total;
    }

    /**
     * 从 classpath 读取 Lua 脚本文件内容
     */
    private String readLuaScript(String path) {
        try {
            return new String(new ClassPathResource(path).getInputStream().readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("读取 Lua 脚本失败: " + path, e);
        }
    }

    // ==================== Redis 缓存工具 ====================

    /** 序列化对象 → JSON 字符串 */
    private String writeCache(Object obj) {
        try { return objectMapper.writeValueAsString(obj); } catch (Exception e) { return null; }
    }

    /** 反序列化 JSON → 对象 */
    private <T> T readCache(String json, TypeReference<T> typeRef) {
        try { return objectMapper.readValue(json, typeRef); } catch (Exception e) { return null; }
    }

    /** 清除可领券列表缓存（券创建/修改/删除/上下架时触发） */
    private void evictAvailableCache() {
        redisTemplate.delete(CACHE_AVAILABLE_KEY);
    }

    /** 清除指定会员的历史缓存（领取/使用后触发） */
    private void evictMemberCache(Long memberId) {
        Set<String> keys = redisTemplate.keys("coupon:history:" + memberId + ":*");
        if (keys != null) keys.forEach(redisTemplate::delete);
    }
}