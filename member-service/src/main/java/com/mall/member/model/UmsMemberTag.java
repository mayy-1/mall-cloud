package com.mall.member.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员标签实体类
 */
@Data
public class UmsMemberTag implements Serializable {
    private Long id;

    private String name;

    @Schema(title = "自动打标签完成订单数量")
    private Integer finishOrderCount;

    @Schema(title = "自动打标签完成订单金额")
    private BigDecimal finishOrderAmount;

    private static final long serialVersionUID = 1L;
}