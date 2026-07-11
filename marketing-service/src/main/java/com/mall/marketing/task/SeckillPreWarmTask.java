package com.mall.marketing.task;

import com.mall.marketing.mapper.SmsFlashPromotionMapper;
import com.mall.marketing.model.SmsFlashPromotion;
import com.mall.marketing.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 秒杀预热定时任务
 * 在秒杀活动开始前，将库存和用户领取记录预加载到 Redis
 *
 * 执行策略：
 * - 每分钟扫描一次当前正在进行或即将开始的秒杀活动
 * - 对于状态为启用的活动，预热其下所有秒杀商品的库存到 Redis
 * - 使用 SETNX 语义，仅初始化不存在的 key，避免覆盖运行中的数据
 */
@Component
@RequiredArgsConstructor
public class SeckillPreWarmTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillPreWarmTask.class);

    private final SmsFlashPromotionMapper flashPromotionMapper;
    private final SeckillService seckillService;

    /**
     * 每分钟执行一次秒杀库存预热
     * 扫描当前日期在活动周期内且状态为启用的秒杀活动
     */
    @Scheduled(cron = "0 * * * * ?")
    public void preWarmSeckillStock() {
        LOGGER.info("开始执行秒杀库存预热任务");

        try {
            // 查询所有启用状态的秒杀活动
            SmsFlashPromotion example = new SmsFlashPromotion();
            example.setStatus(1); // 1 = 启用
            List<SmsFlashPromotion> promotions = flashPromotionMapper.selectByCondition(example);

            LocalDate today = LocalDate.now();

            for (SmsFlashPromotion promotion : promotions) {
                // 检查活动是否在有效期内
                if (promotion.getStartDate() != null && promotion.getEndDate() != null) {
                    LocalDate startDate = promotion.getStartDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate endDate = promotion.getEndDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();

                    if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
                        LOGGER.info("预热秒杀活动: id={}, title={}", promotion.getId(), promotion.getTitle());
                        seckillService.warmUpStock(promotion.getId());
                    }
                }
            }

            LOGGER.info("秒杀库存预热任务完成");
        } catch (Exception e) {
            LOGGER.error("秒杀库存预热任务异常", e);
        }
    }
}
