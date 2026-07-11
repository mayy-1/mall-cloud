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
import java.util.List;

/**
 * 秒杀库存对账定时任务
 * 定时比对 Redis 与 DB 的秒杀库存数据，自动补偿不一致
 *
 * 执行策略：
 * - 每 5 分钟执行一次
 * - 对比 Redis 中的 seckill:stock 与 DB 中 sms_flash_promotion_product_relation.flash_promotion_count
 * - 不一致时以 DB 为准修正 Redis（DB 已通过 MQ 消费者扣减过）
 */
@Component
@RequiredArgsConstructor
public class SeckillStockCheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillStockCheckTask.class);

    private final SmsFlashPromotionMapper flashPromotionMapper;
    private final SeckillService seckillService;

    /**
     * 每 5 分钟执行一次库存对账
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void reconcileSeckillStock() {
        LOGGER.info("开始执行秒杀库存对账任务");

        try {
            SmsFlashPromotion example = new SmsFlashPromotion();
            example.setStatus(1);
            List<SmsFlashPromotion> promotions = flashPromotionMapper.selectByCondition(example);

            LocalDate today = LocalDate.now();

            for (SmsFlashPromotion promotion : promotions) {
                if (promotion.getStartDate() != null && promotion.getEndDate() != null) {
                    LocalDate startDate = promotion.getStartDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate endDate = promotion.getEndDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();

                    if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
                        seckillService.reconcileStock(promotion.getId());
                    }
                }
            }

            LOGGER.info("秒杀库存对账任务完成");
        } catch (Exception e) {
            LOGGER.error("秒杀库存对账任务异常", e);
        }
    }
}
