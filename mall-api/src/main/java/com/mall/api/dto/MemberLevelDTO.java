package com.mall.api.dto;

import lombok.Data;

/**
 * 会员等级 Feign 传输对象
 */
@Data
public class MemberLevelDTO {
    private Long id;
    private String name;
}
