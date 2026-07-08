package com.mall.user.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员分页查询参数
 */
@Data
public class AdminPageQuery {
    @Schema(description = "页码")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量")
    private Integer pageSize = 10;
    
    @Schema(description = "用户名关键字")
    private String keyword;
}
