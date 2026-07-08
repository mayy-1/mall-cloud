package com.mall.product.service;

import com.mall.product.domain.dto.OssCallbackResult;
import com.mall.product.domain.dto.OssPolicyResult;
import jakarta.servlet.http.HttpServletRequest;

/**
 * OSS对象存储服务接口
 * 提供文件上传签名生成和回调处理
 */
public interface IOssService {
    /** 生成OSS上传签名策略 */
    OssPolicyResult policy();
    /** 处理OSS上传成功回调 */
    OssCallbackResult callback(HttpServletRequest request);
}
