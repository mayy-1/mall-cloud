package com.mall.api.dto;

import lombok.Data;

/**
 * 专题商品关联 Feign DTO
 */
@Data
public class CmsSubjectProductRelationDTO {
    private Long id;
    private Long subjectId;
    private Long productId;
}
