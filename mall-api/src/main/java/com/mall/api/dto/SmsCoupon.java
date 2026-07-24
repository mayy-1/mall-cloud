package com.mall.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券 Feign 传输对象
 */
@Data
public class SmsCoupon {
    private Long id;
    private Integer type;
    private String name;
    private String code;
    private Integer count;
    private Integer amount;
    private Integer perLimit;
    private BigDecimal minPoint;
    private Date startTime;
    private Date endTime;
    private Integer useType;
    private String note;
    private Integer publishCount;
    private Integer useCount;
    private Integer receiveCount;
    private Date enableTime;
    private String codeInfo;
    private Integer memberLevel;
    private Integer status;
}
