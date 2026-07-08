package com.mall.cart.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis配置类
 * 配置Mapper扫描路径，启用事务管理
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.mall.cart.mapper"})
public class MyBatisConfig {
}
