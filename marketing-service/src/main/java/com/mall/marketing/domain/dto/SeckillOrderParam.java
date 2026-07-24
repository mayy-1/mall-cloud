package com.mall.marketing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 秒杀下单请求参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SeckillOrderParam {

    @Schema(title = "秒杀活动ID")
    @NotNull(message = "秒杀活动ID不能为空")
    private Long promotionId;

    @Schema(title = "秒杀场次ID")
    @NotNull(message = "场次ID不能为空")
    private Long sessionId;

    @Schema(title = "商品ID")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(title = "会员ID（从Token解析）")
    private Long memberId;
}
