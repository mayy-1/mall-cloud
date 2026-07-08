package com.mall.search.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis相关配置
 */
@Configuration
@MapperScan({"com.mall.search.mapper"})
public class MyBatisConfig {
}
