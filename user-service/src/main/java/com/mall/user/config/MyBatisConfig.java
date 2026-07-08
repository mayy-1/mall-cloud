package com.mall.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis配置类
 * 启用事务管理、扫描Mapper接口
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.mall.user.mapper"})
public class MyBatisConfig {
}
