package com.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品服务启动类
 * 注册到Nacos、扫描MyBatis Mapper、启用Feign远程调用
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.mall.product.mapper")
@EnableFeignClients(basePackages = "com.mall.api.client")
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
