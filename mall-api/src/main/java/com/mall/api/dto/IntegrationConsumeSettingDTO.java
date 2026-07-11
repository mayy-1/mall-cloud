package com.mall.api.dto;

import lombok.Data;

/**
 * 积分消费设置 Feign 传输对象
 */
@Data
public class IntegrationConsumeSettingDTO {
    private Long id;
    private Integer deductionPerAmount;
    private Integer maxPercentPerOrder;
    private Integer useUnit;
    private Integer couponStatus;
}
