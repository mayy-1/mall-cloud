package com.mall.product.service.impl;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.mall.product.domain.dto.OssCallbackResult;
import com.mall.product.domain.dto.OssPolicyResult;
import com.mall.product.service.IOssService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * OSS对象存储服务实现类
 * 【管理端专用】提供阿里云OSS文件上传签名策略生成和上传成功回调处理。
 * 前端直传OSS模式：前端获取签名后直接上传文件到OSS，上传成功后OSS回调本服务。
 */
@Service
public class OssServiceImpl implements IOssService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OssServiceImpl.class);

    @Value("${aliyun.oss.endpoint}")
    private String ALIYUN_OSS_ENDPOINT;

    @Value("${aliyun.oss.accessKeyId}")
    private String ALIYUN_OSS_ACCESSKEYID;

    @Value("${aliyun.oss.accessKeySecret}")
    private String ALIYUN_OSS_ACCESSKEYSECRET;

    @Value("${aliyun.oss.bucketName}")
    private String ALIYUN_OSS_BUCKETNAME;

    @Value("${aliyun.oss.dir.prefix:mall}")
    private String ALIYUN_OSS_DIR_PREFIX;

    @Value("${aliyun.oss.callback:}")
    private String ALIYUN_OSS_CALLBACK;

    @Override
    public OssPolicyResult policy() {
        OssPolicyResult result = new OssPolicyResult();
        // OSS服务端地址（对外访问域名）
        String host = "https://" + ALIYUN_OSS_BUCKETNAME + "." + ALIYUN_OSS_ENDPOINT;
        // 上传目录：前缀 + 日期分目录
        String format = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String dir = ALIYUN_OSS_DIR_PREFIX + "/" + format + "/";

        OSS ossClient = new OSSClientBuilder().build(ALIYUN_OSS_ENDPOINT, ALIYUN_OSS_ACCESSKEYID, ALIYUN_OSS_ACCESSKEYSECRET);
        try {
            // 生成签名策略，有效期1小时
            long expireEndTime = System.currentTimeMillis() + 3600 * 1000L;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConditions = new PolicyConditions();
            // 限制上传文件大小：最大10MB
            policyConditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 10485760);
            // 限制上传文件前缀
            policyConditions.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConditions);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            // 回调配置（可选）
            String callback = null;
            if (ALIYUN_OSS_CALLBACK != null && !ALIYUN_OSS_CALLBACK.isEmpty()) {
                callback = BinaryUtil.toBase64String(
                        ("{\"callbackUrl\":\"" + ALIYUN_OSS_CALLBACK + "\","
                                + "\"callbackBody\":\"filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}\","
                                + "\"callbackBodyType\":\"application/x-www-form-urlencoded\"}")
                                .getBytes(StandardCharsets.UTF_8));
            }

            result.setAccessKeyId(ALIYUN_OSS_ACCESSKEYID);
            result.setPolicy(encodedPolicy);
            result.setSignature(postSignature);
            result.setDir(dir);
            result.setHost(host);
            result.setCallback(callback);
        } finally {
            ossClient.shutdown();
        }
        return result;
    }

    @Override
    public OssCallbackResult callback(HttpServletRequest request) {
        OssCallbackResult result = new OssCallbackResult();
        // OSS回调通过 form 表单方式传递参数
        String filename = request.getParameter("filename");
        String size = request.getParameter("size");
        String mimeType = request.getParameter("mimeType");
        String width = request.getParameter("width");
        String height = request.getParameter("height");

        result.setFilename(filename);
        result.setSize(size);
        result.setMimeType(mimeType);
        result.setWidth(width);
        result.setHeight(height);

        LOGGER.info("OSS callback received - filename: {}, size: {}, mimeType: {}", filename, size, mimeType);
        return result;
    }
}
