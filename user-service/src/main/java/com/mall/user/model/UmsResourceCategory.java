package com.mall.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**后台资源分类实体 */
@Data
public class UmsResourceCategory implements Serializable {
    private Long id;

    @Schema(title = "鍒涘缓鏃堕棿")
    private Date createTime;

    @Schema(title = "鍒嗙被鍚嶇О")
    private String name;

    @Schema(title = "鎺掑簭")
    private Integer sort;

    private static final long serialVersionUID = 1L;
}