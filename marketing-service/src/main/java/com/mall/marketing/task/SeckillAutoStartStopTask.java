package com.mall.marketing.task;

import com.mall.marketing.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 秒杀活动自动上下线 + 库存预热定时任务
 * <p>
 * 执行策略：
 * <ul>
 *   <li>每分钟检查一次所有秒杀活动</li>
 *   <li>活动开始前 5 分钟：自动预热库存（MySQL → Redis），但不上线</li>
 *   <li>活动开始时间到达且未启用时：自动将状态设置为启用（1）</li>
 *   <li>活动结束时间到达且已启用时：自动将状态设置为禁用（0）</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class SeckillAutoStartStopTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillAutoStartStopTask.class);

    /** 活动开始前多少毫秒预热库存（5 分钟） */
    private static final long PRE_WARM_LEAD_MS = 5 * 60 * 1000;

    private final SeckillService seckillService;

    /**
     * 每分钟执行：检查是否需要预热库存、上下线活动
     */
    @Scheduled(cron = "0 * * * * ?")
    public void autoStartStop() {
        LOGGER.info("开始执行秒杀活动自动上下线及预热任务");
        try {
            // 先预热（活动开始前 5 分钟），再上下线
            seckillService.preWarmNearbyPromotions(PRE_WARM_LEAD_MS);
            seckillService.autoStartStopPromotions();
        } catch (Exception e) {
            LOGGER.error("秒杀活动自动上下线任务异常", e);
        }
        LOGGER.info("秒杀活动自动上下线及预热任务完成");
    }
}
