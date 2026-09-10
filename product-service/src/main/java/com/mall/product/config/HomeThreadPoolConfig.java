package com.mall.product.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 首页聚合查询线程池配置
 *
 * <p>首页 content() 聚合了 6 路独立数据源（品牌、新品、热门、专题、秒杀、广告），
 * 其中 4 路是跨服务 Feign 调用。串行执行时首页耗时 = 各调用耗时之和，
 * 用线程池并行后可缩短到最慢的那一路。</p>
 */
@Slf4j
@Configuration
public class HomeThreadPoolConfig {

    @Bean("homeContentExecutor")
    public Executor homeContentExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger idx = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "home-content-" + idx.getAndIncrement());
            }
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                6,                                    // 核心线程数：6 路并行
                12,                                   // 最大线程数
                60L, TimeUnit.SECONDS,                // 空闲线程存活时间
                new ArrayBlockingQueue<>(20),         // 有界队列
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者运行，削峰不丢请求
        );

        log.info("首页聚合查询并行线程池初始化完成");
        return executor;
    }
}
