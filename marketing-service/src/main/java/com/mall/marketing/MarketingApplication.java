package com.mall.marketing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 营销服务启动类
 * 负责营销模块的服务发现、Mapper扫描、Feign客户端注册和定时任务
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.mall.marketing.mapper")
@EnableFeignClients(basePackages = "com.mall.api.client")
@EnableScheduling
public class MarketingApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketingApplication.class, args);
    }
}
