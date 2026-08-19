package com.mall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.mall.api.config.DefaultFeignConfig;

/**
 * 会员服务启动类
 * 提供服务注册发现、MyBatis映射扫描及Feign客户端调用能力
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.mall.member.mapper")
@EnableFeignClients(basePackages = "com.mall.api.client", defaultConfiguration = DefaultFeignConfig.class)
public class MemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}
