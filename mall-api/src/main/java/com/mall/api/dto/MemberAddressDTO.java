package com.mall.api.dto;

import lombok.Data;

/**
 * 会员收货地址 Feign 传输对象
 */
@Data
public class MemberAddressDTO {
    private Long id;
    private Long memberId;
    private String name;
    private String phoneNumber;
    private Integer defaultStatus;
    private String postCode;
    private String province;
    private String city;
    private String region;
    private String detailAddress;
}
