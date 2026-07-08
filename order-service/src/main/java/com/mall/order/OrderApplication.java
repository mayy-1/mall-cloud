package com.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 * 负责订单模块的服务发现、Mapper扫描和Feign客户端注册
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.mall.order.mapper")
@EnableFeignClients(basePackages = "com.mall.api.client")
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
