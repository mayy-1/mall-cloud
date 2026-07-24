package com.mall.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀商品详情 Feign 传输对象
 */
@Data
public class SeckillProductDetailDTO {
    private Long promotionId;
    private String promotionTitle;
    private Long sessionId;
    private Long productId;
    private String productName;
    private String productPic;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer limitPerUser;
    private String startTime;
    private String endTime;
    private Integer status;
}
