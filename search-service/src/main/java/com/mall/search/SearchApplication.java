package com.mall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import com.mall.api.config.DefaultFeignConfig;

/**
 * 搜索服务启动类
 * 提供基于Elasticsearch的商品全文检索、筛选、排序、推荐功能
 * 启用Nacos服务发现、Feign远程调用及 RabbitMQ（接收 Canal binlog 变更同步 ES）
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableRabbit
@EnableFeignClients(basePackages = "com.mall.api.client", defaultConfiguration = DefaultFeignConfig.class)
public class SearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}
