package com.mall.product.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.product.domain.dto.OssCallbackResult;
import com.mall.product.domain.dto.OssPolicyResult;
import com.mall.product.service.IOssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 阿里云 OSS 对象存储 Controller
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "OssController", description = "Oss管理")
@RequestMapping("/aliyun/oss")
public class OssController {
    private final IOssService ossService;

    /** 【管理端】OSS 上传签名生成（前端直传 OSS 时获取临时授权） */
    @Operation(summary = "oss上传签名生成")
    @GetMapping("/policy")
    public CommonResult<OssPolicyResult> policy() {
        OssPolicyResult result = ossService.policy();
        return CommonResult.success(result);
    }

    /** 【管理端】OSS 上传成功回调（OSS 通知服务端上传完成） */
    @Operation(summary = "oss上传成功回调")
    @PostMapping("callback")
    public CommonResult<OssCallbackResult> callback(HttpServletRequest request) {
        OssCallbackResult ossCallbackResult = ossService.callback(request);
        return CommonResult.success(ossCallbackResult);
    }

}
