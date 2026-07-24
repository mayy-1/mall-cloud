package com.mall.trade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 交易服务启动类
 * 提供服务注册发现、MyBatis映射扫描、Feign客户端调用及定时任务能力
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan({"com.mall.trade.mapper"})
@EnableFeignClients(basePackages = {"com.mall.api.client", "com.mall.trade.feign"})
@EnableScheduling
public class TradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradeApplication.class, args);
    }
}
