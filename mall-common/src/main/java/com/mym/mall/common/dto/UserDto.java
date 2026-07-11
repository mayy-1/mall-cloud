package com.mym.mall.common.dto;

import lombok.*;

import java.util.List;

/**
 * @description 权限框架中使用的用户信息封装类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String clientId;
    private List<String> permissionList;
}
