package com.mall.marketing.controller;

import com.mall.api.dto.HomeFlashPromotionDTO;
import com.mall.api.dto.SmsFlashPromotion;
import com.mall.marketing.service.IFlashPromotionService;
import com.mall.marketing.service.SeckillService;
import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 限时购活动管理Controller
 */
@RestController
@Tag(name = "FlashPromotionController", description = "限时购活动管理")
@RequestMapping("/flash")
@RequiredArgsConstructor
public class FlashPromotionController {
    /** 限时购活动服务 */
    private final IFlashPromotionService flashPromotionService;
    private final SeckillService seckillService;

    @Operation(summary = "添加活动")
    @PostMapping("/create")
    public CommonResult create(@RequestBody SmsFlashPromotion flashPromotion) {
        int count = flashPromotionService.create(flashPromotion);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "编辑活动信息")
    @PostMapping("/update/{id}")
    public Object update(@PathVariable Long id, @RequestBody SmsFlashPromotion flashPromotion) {
        int count = flashPromotionService.update(id, flashPromotion);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "删除活动信息")
    @PostMapping("/delete/{id}")
    public Object delete(@PathVariable Long id) {
        int count = flashPromotionService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改上下线状态")
    @PostMapping("/update/status/{id}")
    public Object update(@PathVariable Long id, @RequestParam Integer status) {
        int count = flashPromotionService.updateStatus(id, status);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public Object getItem(@PathVariable Long id) {
        SmsFlashPromotion flashPromotion = flashPromotionService.getItem(id);
        return CommonResult.success(flashPromotion);
    }

    @Operation(summary = "根据活动名称分页查询")
    @GetMapping("/list")
    public Object getItem(@RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                          @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<SmsFlashPromotion> flashPromotionList = flashPromotionService.list(keyword, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(flashPromotionList));
    }

    @Operation(summary = "获取当前秒杀活动")
    @GetMapping("/current")
    public CommonResult<Object> getCurrentFlashPromotion() {
        SmsFlashPromotion flashPromotion = flashPromotionService.getCurrentFlashPromotion();
        return CommonResult.success(flashPromotion);
    }

    @Operation(summary = "获取首页秒杀活动")
    @GetMapping("/home")
    public CommonResult<HomeFlashPromotionDTO> getHomeFlashPromotion() {
        SmsFlashPromotion flashPromotion = flashPromotionService.getCurrentFlashPromotion();
        if (flashPromotion == null) {
            return CommonResult.success(null);
        }
        return CommonResult.success(toHomeFlashPromotionDTO(flashPromotion));
    }

    private HomeFlashPromotionDTO toHomeFlashPromotionDTO(SmsFlashPromotion flashPromotion) {
        HomeFlashPromotionDTO dto = new HomeFlashPromotionDTO();
        dto.setId(flashPromotion.getId());
        dto.setTitle(flashPromotion.getTitle());
        dto.setStartDate(flashPromotion.getStartDate());
        dto.setEndDate(flashPromotion.getEndDate());
        dto.setStartTime(formatDate(flashPromotion.getStartDate()));
        dto.setEndTime(formatDate(flashPromotion.getEndDate()));
        dto.setStatus(flashPromotion.getStatus());
        dto.setCreateTime(flashPromotion.getCreateTime());
        dto.setProductList(seckillService.getCurrentSeckillProducts().stream()
                .filter(item -> flashPromotion.getId().equals(item.getPromotionId()))
                .collect(Collectors.toList()));
        return dto;
    }


    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
