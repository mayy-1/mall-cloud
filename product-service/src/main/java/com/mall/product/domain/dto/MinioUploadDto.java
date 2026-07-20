package com.mall.product.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MinIO文件上传返回结果
 * Created by macro on 2023/3/26.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MinioUploadDto {
    private String url;
    private String name;
}
