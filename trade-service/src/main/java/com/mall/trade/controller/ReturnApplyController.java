package com.mall.trade.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.trade.domain.dto.OmsOrderReturnApplyParam;
import com.mall.trade.service.IReturnApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 申请退货管理Controller
 * Created by macro on 2018/10/17.
 */
@Controller
@Tag(name = "ReturnApplyController", description = "申请退货管理")
@RequestMapping("/returnApply")
@RequiredArgsConstructor
public class ReturnApplyController {

    /** 退货申请业务服务 */
    private final IReturnApplyService returnApplyService;

    @Operation(summary = "申请退货")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult create(@RequestBody OmsOrderReturnApplyParam returnApply) {
        int count = returnApplyService.create(returnApply);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
