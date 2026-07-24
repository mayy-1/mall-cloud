package com.mall.api.dto;

import lombok.Data;

/**
 * 首页轮播广告 Feign 传输对象
 */
@Data
public class HomeAdvertiseDTO {
    private Long id;
    private String name;
    private Integer type;
    private String pic;
    private String startTime;
    private String endTime;
    private Integer status;
    private Integer clickCount;
    private Integer orderCount;
    private String url;
    private String note;
    private Integer sort;
}
