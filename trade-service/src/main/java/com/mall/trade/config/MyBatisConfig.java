package com.mall.trade.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis配置类
 * 开启事务管理，扫描com.mall.trade.mapper包下的Mapper接口
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.mall.trade.mapper"})
public class MyBatisConfig {
}
