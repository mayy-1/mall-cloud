package com.mym.mall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.mall.api.client")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.mym.mall")
public class MallAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallAuthApplication.class, args);
    }

}
