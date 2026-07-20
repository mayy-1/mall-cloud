package com.mall.member.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.member.domain.MemberReadHistory;
import com.mall.member.service.IReadHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员商品浏览记录管理Controller
 */
@RestController
@Tag(name = "ReadHistoryController", description = "会员商品浏览记录管理")
@RequestMapping("/member/readHistory")
@RequiredArgsConstructor
public class ReadHistoryController {
    /** 浏览记录业务服务 */
    private final IReadHistoryService readHistoryService;

    @Operation(summary = "创建浏览记录")
    @PostMapping("/create")
    public CommonResult create(@RequestBody MemberReadHistory memberReadHistory) {
        int count = readHistoryService.create(memberReadHistory);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary = "删除浏览记录")
    @PostMapping("/delete")
    public CommonResult delete(@RequestParam("ids") List<String> ids) {
        int count = readHistoryService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary = "清空除浏览记录")
    @PostMapping("/clear")
    public CommonResult clear() {
        readHistoryService.clear();
        return CommonResult.success(null);
    }

    @Operation(summary = "分页获取用户浏览记录")
    @GetMapping("/list")
    public CommonResult<CommonPage<MemberReadHistory>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<MemberReadHistory> page = readHistoryService.list(pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(page));
    }
}
