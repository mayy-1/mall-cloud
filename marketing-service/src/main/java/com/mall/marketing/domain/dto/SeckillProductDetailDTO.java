package com.mall.marketing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀商品详情DTO
 * 前台展示秒杀商品详细信息
 */
@Data
public class SeckillProductDetailDTO {

    @Schema(title = "秒杀活动ID")
    private Long promotionId;

    @Schema(title = "秒杀活动标题")
    private String promotionTitle;

    @Schema(title = "秒杀场次ID")
    private Long sessionId;

    @Schema(title = "商品ID")
    private Long productId;

    @Schema(title = "商品名称")
    private String productName;

    @Schema(title = "商品图片")
    private String productPic;

    @Schema(title = "商品原价")
    private BigDecimal originalPrice;

    @Schema(title = "秒杀价")
    private BigDecimal seckillPrice;

    @Schema(title = "秒杀库存")
    private Integer seckillStock;

    @Schema(title = "每人限购数量")
    private Integer limitPerUser;

    @Schema(title = "活动开始时间")
    private String startTime;

    @Schema(title = "活动结束时间")
    private String endTime;

    @Schema(title = "秒杀状态：0-未开始，1-进行中，2-已结束")
    private Integer status;
}
