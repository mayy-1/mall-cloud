package com.mall.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 管理员信息视图对象
 */
@Data
public class AdminInfoVO {
    private Long id;
    private String username;
    private String icon;
    private String email;
    private String nickName;
    private String note;
    private Date loginTime;
    private List<String> roles;
    private List<String> menus;
}
