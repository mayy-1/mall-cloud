package com.mall.marketing.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 帮助分类实体
 * 存储帮助文档分类信息
 */
@Data
public class CmsHelpCategory implements Serializable {
    private Long id;

    private String name;

    @Schema(title = "分类图标")
    private String icon;

    @Schema(title = "专题数量")
    private Integer helpCount;

    private Integer showStatus;

    private Integer sort;

    private static final long serialVersionUID = 1L;
}