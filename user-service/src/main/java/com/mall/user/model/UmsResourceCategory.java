package com.mall.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;

/**后台资源分类实体 */
public class UmsResourceCategory implements Serializable {
    private Long id;

    @Schema(title = "鍒涘缓鏃堕棿")
    private Date createTime;

    @Schema(title = "鍒嗙被鍚嶇О")
    private String name;

    @Schema(title = "鎺掑簭")
    private Integer sort;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", createTime=").append(createTime);
        sb.append(", name=").append(name);
        sb.append(", sort=").append(sort);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}