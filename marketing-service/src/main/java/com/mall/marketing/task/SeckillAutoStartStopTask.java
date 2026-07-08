package com.mall.marketing.task;

import com.mall.marketing.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 秒杀活动自动上下线定时任务
 * 根据活动的开始时间和结束时间，自动启用或禁用秒杀活动
 *
 * 执行策略：
 * - 每分钟检查一次所有秒杀活动
 * - 活动开始时间到达且未启用时，自动将状态设置为启用（1），并预热库存
 * - 活动结束时间到达且已启用时，自动将状态设置为禁用（0）
 */
@Component
@RequiredArgsConstructor
public class SeckillAutoStartStopTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillAutoStartStopTask.class);

    private final SeckillService seckillService;

    /**
     * 每分钟执行一次秒杀活动自动上下线
     */
    @Scheduled(cron = "0 * * * * ?")
    public void autoStartStop() {
        LOGGER.info("开始执行秒杀活动自动上下线任务");
        try {
            seckillService.autoStartStopPromotions();
        } catch (Exception e) {
            LOGGER.error("秒杀活动自动上下线任务异常", e);
        }
        LOGGER.info("秒杀活动自动上下线任务完成");
    }
}
