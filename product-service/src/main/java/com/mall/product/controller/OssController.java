package com.mall.product.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.product.domain.dto.OssCallbackResult;
import com.mall.product.domain.dto.OssPolicyResult;
import com.mall.product.service.IOssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Oss相关操作接口
 * Created by macro on 2018/4/26.
 */
@Controller
@RequiredArgsConstructor
@Tag(name = "OssController", description = "Oss管理")
@RequestMapping("/aliyun/oss")
public class OssController {
    private final IOssService ossService;

    @Operation(summary = "oss上传签名生成")
    @RequestMapping(value = "/policy", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<OssPolicyResult> policy() {
        OssPolicyResult result = ossService.policy();
        return CommonResult.success(result);
    }

    @Operation(summary = "oss上传成功回调")
    @RequestMapping(value = "callback", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<OssCallbackResult> callback(HttpServletRequest request) {
        OssCallbackResult ossCallbackResult = ossService.callback(request);
        return CommonResult.success(ossCallbackResult);
    }

}
