package com.mall.marketing.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.mym.mall.common.api.CommonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 营销服务异常处理。
 */
@RestControllerAdvice
public class MarketingExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = NotLoginException.class)
    public CommonResult handleNotLoginException(NotLoginException e) {
        return CommonResult.unauthorized(null);
    }
}
