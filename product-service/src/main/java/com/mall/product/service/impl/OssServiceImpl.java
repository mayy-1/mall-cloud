package com.mall.product.service.impl;

import com.mall.product.domain.dto.OssCallbackResult;
import com.mall.product.domain.dto.OssPolicyResult;
import com.mall.product.service.IOssService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * OSS对象存储服务实现类
 * 提供文件上传签名策略生成和回调处理（待实现）
 */
@Service
public class OssServiceImpl implements IOssService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OssServiceImpl.class);

    @Override
    public OssPolicyResult policy() {
        // TODO: 实现OSS上传策略生成
        LOGGER.info("OSS policy generation - TODO implement");
        return null;
    }

    @Override
    public OssCallbackResult callback(HttpServletRequest request) {
        // TODO: 实现OSS回调处理
        LOGGER.info("OSS callback - TODO implement");
        return null;
    }
}
