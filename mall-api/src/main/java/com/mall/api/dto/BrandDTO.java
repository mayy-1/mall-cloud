package com.mall.api.dto;

import lombok.Data;

/**
 * 品牌 Feign 传输对象
 */
@Data
public class BrandDTO {
    private Long id;
    private String name;
    private String logo;
    private String description;
    private Integer showStatus;
}
