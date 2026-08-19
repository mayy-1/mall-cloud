package com.mall.marketing.service;

import com.mall.marketing.domain.dto.CartPromotionItem;
import com.mall.marketing.domain.dto.SmsCouponDetail;
import com.mall.marketing.domain.dto.SmsCouponHistoryDetail;
import com.mall.marketing.domain.dto.SmsCouponParam;
import com.mall.marketing.model.SmsCoupon;
import com.mall.marketing.model.SmsCouponHistory;

import java.util.List;

/**
 * 优惠券管理服务接口
 */
public interface ICouponService {

    // ==================== 管理端 ====================

    /**
     * 新增优惠券（含关联数据）
     */
    int create(SmsCouponParam couponParam);

    /**
     * 删除优惠券（物理删除，同时清理关联数据）
     *
     * @param id 优惠券 ID
     * @return 受影响行数
     */
    int delete(Long id);

    /**
     * 修改优惠券（先删关联，再插新关联）
     *
     * @param id          优惠券 ID
     * @param couponParam 修改后的参数
     * @return 受影响行数
     */
    int update(Long id, SmsCouponParam couponParam);

    /**
     * 切换优惠券上下架状态
     *
     * @param id     优惠券 ID
     * @param status 0-下架，1-上架
     * @return 受影响行数
     */
    int updateStatus(Long id, Integer status);

    /**
     * 管理端分页查询优惠券
     *
     * @param name     名称（模糊，可选）
     * @param type     类型（0-全场/1-会员/2-购物/3-注册，可选）
     * @param status   上下架（0/1，可选）
     * @param pageSize 每页条数
     * @param pageNum  页码
     * @return 分页列表
     */
    List<SmsCoupon> list(String name, Integer type, Integer status, Integer pageSize, Integer pageNum);

    /**
     * 获取优惠券详情（含关联商品/分类列表）
     *
     * @param id 优惠券 ID
     * @return 优惠券详情，不存在时返回 null
     */
    SmsCouponParam getItem(Long id);

    // ==================== 用户端 ====================

    /**
     * 会员领取优惠券
     * <p>
     * 校验：券是否存在 → 库存是否充足 → 是否在可领取时间 → 是否超过每人限领次数
     *
     * @param couponId 优惠券 ID
     */
    void add(Long couponId);

    /**
     * 查询会员已领取的优惠券列表（含关联商品/分类）
     * <p>
     * 用于"我的优惠券"页面，支持按使用状态筛选。
     *
     * @param useStatus 使用状态：0-未使用，1-已使用，2-已过期，null-全部
     * @return 已领取优惠券详情列表
     */
    List<SmsCouponHistoryDetail> listHistory(Integer useStatus);

    /**
     * 获取当前会员购物车可用的优惠券
     * <p>
     * 根据购物车商品计算三种 useType 的券是否满足使用条件。
     *
     * @param cartItemList 购物车商品列表
     * @param type         返回类型：1-可用券，其他-不可用券
     * @return 优惠券详情列表
     */
    List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type);

    /**
     * 查询指定商品可用的优惠券
     * <p>
     * 用于商品详情页。匹配 useType=0（全场）、useType=1（匹配商品分类）、useType=2（匹配商品ID）。
     *
     * @param productId 商品 ID
     * @return 可用优惠券列表
     */
    List<SmsCoupon> listByProduct(Long productId);

    /**
     * 查询平台所有已上架可领取的优惠券（领券中心）
     * <p>
     * 返回 SmsCouponDetail（仅含优惠券 + 关联数据，无历史记录字段）。
     *
     * @return 可领取优惠券列表
     */
    List<SmsCouponDetail> listAvailableCoupons();

    // ==================== 内部调用 ====================

    /**
     * 更新会员优惠券使用状态
     * <p>
     * 供 order-service 通过 Feign 调用，下单成功后标记"已使用"，订单取消时回滚"未使用"。
     *
     * @param couponId  优惠券 ID
     * @param memberId  会员 ID
     * @param useStatus 使用状态：0-未使用，1-已使用
     */
    void updateCouponStatus(Long couponId, Long memberId, Integer useStatus);

}