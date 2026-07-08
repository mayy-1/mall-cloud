package com.mall.cart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 购物车服务启动类
 * 提供购物车增删改查、促销计算、规格变更等功能
 * 启用MyBatis Mapper扫描和Feign远程调用
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.mall.cart.mapper")
@EnableFeignClients(basePackages = {"com.mall.api.client", "com.mall.cart.feign"})
public class CartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }
}
