package com.mall.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 优惠券 Feign 传输对象
 */
@Data
public class CouponDTO {
    private Long id;
    private Integer useType;
    private BigDecimal amount;
}
