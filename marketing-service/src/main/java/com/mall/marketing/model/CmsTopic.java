package com.mall.marketing.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 话题实体
 * 存储话题的基本信息
 */
@Data
public class CmsTopic implements Serializable {
    private Long id;

    private Long categoryId;

    private String name;

    private Date createTime;

    private Date startTime;

    private Date endTime;

    @Schema(title = "参与人数")
    private Integer attendCount;

    @Schema(title = "关注人数")
    private Integer attentionCount;

    private Integer readCount;

    @Schema(title = "奖品名称")
    private String awardName;

    @Schema(title = "参与方式")
    private String attendType;

    @Schema(title = "话题内容")
    private String content;

    private static final long serialVersionUID = 1L;
}