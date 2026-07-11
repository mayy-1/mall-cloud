package com.mym.mall.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * mall-common 模块自动装配入口
 * 通过 spring.factories / AutoConfiguration.imports 自动激活，
 * 将所有 @Component / @Configuration / @ControllerAdvice 纳入 Spring 管理
 */
@Configuration
@ComponentScan(basePackages = "com.mym.mall.common")
public class MallCommonAutoConfiguration {
}
