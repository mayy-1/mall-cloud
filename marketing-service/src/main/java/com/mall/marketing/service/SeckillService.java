package com.mall.marketing.service;

import com.mall.marketing.domain.dto.SeckillOrderParam;
import com.mall.marketing.domain.dto.SeckillProductDetailDTO;

import java.util.List;

/**
 * 秒杀服务接口
 * 负责秒杀核心业务：Redis Lua 原子脚本扣减、MQ 异步下单
 */
public interface SeckillService {

    /**
     * 执行秒杀
     * 通过 Lua 脚本原子扣减 Redis 库存，成功后发送 MQ 消息异步创建订单
     *
     * @param param 秒杀下单参数
     * @return 秒杀结果：1=成功，0=库存不足，-1=重复秒杀
     */
    int executeSeckill(SeckillOrderParam param);

    /**
     * 预热秒杀库存到 Redis
     * 在秒杀活动开始前调用，将 DB 中的 flash_promotion_count 加载到 Redis
     *
     * @param promotionId 秒杀活动ID
     */
    void warmUpStock(Long promotionId);

    /**
     * 对账 Redis 与 DB 的秒杀库存
     * 定时任务调用，自动补偿不一致数据
     *
     * @param promotionId 秒杀活动ID
     */
    void reconcileStock(Long promotionId);

    /**
     * 获取秒杀商品当前剩余库存（Redis）
     *
     * @param promotionId 活动ID
     * @param productId   商品ID
     * @return 剩余库存
     */
    Long getSeckillStock(Long promotionId, Long productId);

    /**
     * 获取当前正在进行的秒杀活动列表
     *
     * @return 秒杀活动及商品列表
     */
    List<SeckillProductDetailDTO> getCurrentSeckillProducts();

    /**
     * 获取秒杀商品详情
     *
     * @param promotionId 秒杀活动ID
     * @param productId   商品ID
     * @return 秒杀商品详情
     */
    SeckillProductDetailDTO getSeckillProductDetail(Long promotionId, Long productId);

    /**
     * 自动上下线秒杀活动
     * 根据活动时间自动更新活动状态：到了开始时间上线，到了结束时间下线
     */
    void autoStartStopPromotions();
}
