package com.mall.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**角色资源关系实体 */
@Data
public class UmsRoleResourceRelation implements Serializable {
    private Long id;

    @Schema(title = "瑙掕壊ID")
    private Long roleId;

    @Schema(title = "璧勬簮ID")
    private Long resourceId;

    private static final long serialVersionUID = 1L;
}