package com.mall.api.dto;

import lombok.Data;

import java.util.Date;

/**
 * 会员 Feign 传输对象（跨服务最小信息）
 */
@Data
public class MemberDTO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String icon;
    private Integer gender;
    private Date birthday;
    private String city;
    private String job;
    private Integer integration;
    private Integer growth;
}
