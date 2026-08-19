package com.mall.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 后台资源实体类
 * 用于管理系统后端接口/菜单资源信息
 */
@Data
public class UmsResource implements Serializable {
    /** 主键ID */
    private Long id;

    @Schema(title = "创建时间")
    private Date createTime;

    @Schema(title = "资源名称")
    private String name;

    @Schema(title = "资源请求地址URL")
    private String url;

    @Schema(title = "资源描述说明")
    private String description;

    @Schema(title = "资源分类ID，关联资源分类表")
    private Long categoryId;

    private static final long serialVersionUID = 1L;
}