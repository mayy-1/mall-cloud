package com.mall.order.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.order.domain.dto.OmsOrderReturnApplyParam;
import com.mall.order.service.IPortalReturnApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 申请退货管理Controller
 * Created by macro on 2018/10/17.
 */
@RestController
@Tag(name = "PortalReturnApplyController", description = "前台申请退货管理")
@RequestMapping("/returnApply")
@RequiredArgsConstructor
public class PortalReturnApplyController {

    /** 退货申请业务服务 */
    private final IPortalReturnApplyService returnApplyService;

    @Operation(summary = "申请退货")
    @PostMapping("/create")
    public CommonResult create(@RequestBody OmsOrderReturnApplyParam returnApply) {
        int count = returnApplyService.create(returnApply);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}

