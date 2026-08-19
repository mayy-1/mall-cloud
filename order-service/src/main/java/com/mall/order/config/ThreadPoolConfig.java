package com.mall.order.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * 订单服务线程池配置
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolConfig.class);

    @Bean("orderConfirmExecutor")
    public Executor orderConfirmExecutor() {
        // 1. 自定义线程工厂，实现线程名前缀 order-confirm-
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNamePrefix("order-confirm-")
                .build();

        // 2. 有界阻塞队列，容量20
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(20);

        // 3. 拒绝策略：调用者运行策略
        RejectedExecutionHandler rejectedHandler = new ThreadPoolExecutor.CallerRunsPolicy();

        // 4. 使用原生构造函数创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,
                4,
                30L,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                rejectedHandler
        );

        log.info("订单确认单并行线程池初始化完成");
        return executor;
    }
}
