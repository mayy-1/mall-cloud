package com.mall.api.dto;

import lombok.Data;

/**
 * 专题 Feign 传输对象
 */
@Data
public class SubjectDTO {
    private Long id;
    private String title;
    private String pic;
    private Integer showStatus;
}
