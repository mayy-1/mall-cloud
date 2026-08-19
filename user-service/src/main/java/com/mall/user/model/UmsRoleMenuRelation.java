package com.mall.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**角色菜单关系实体 */
@Data
public class UmsRoleMenuRelation implements Serializable {
    private Long id;

    @Schema(title = "瑙掕壊ID")
    private Long roleId;

    @Schema(title = "鑿滃崟ID")
    private Long menuId;

    private static final long serialVersionUID = 1L;
}