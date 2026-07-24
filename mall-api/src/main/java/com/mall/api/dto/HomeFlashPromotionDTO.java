package com.mall.api.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 首页限时购活动聚合 DTO.
 */
@Data
public class HomeFlashPromotionDTO {
    private Long id;
    private String title;
    private Date startDate;
    private Date endDate;
    private String startTime;
    private String endTime;
    private String nextStartTime;
    private String nextEndTime;
    private Integer status;
    private Date createTime;
    private List<SeckillProductDetailDTO> productList;
}
