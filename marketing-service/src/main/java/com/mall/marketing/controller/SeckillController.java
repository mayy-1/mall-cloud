package com.mall.marketing.controller;

import com.mall.marketing.domain.dto.SeckillOrderParam;
import com.mall.marketing.domain.dto.SeckillProductDetailDTO;
import com.mall.marketing.service.SeckillService;
import com.mym.mall.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀控制器（会员端）
 * 提供秒杀下单、库存查询接口
 */
@Tag(name = "秒杀管理", description = "秒杀活动下单与库存查询")
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillController.class);
    private final SeckillService seckillService;

    @Operation(summary = "执行秒杀下单")
    @PostMapping("/execute")
    public CommonResult<String> executeSeckill(@Valid @RequestBody SeckillOrderParam param) {
        LOGGER.info("秒杀请求, memberId={}, productId={}, promotionId={}",
                param.getMemberId(), param.getProductId(), param.getPromotionId());

        int result = seckillService.executeSeckill(param);

        if (result == 1) {
            return CommonResult.success("秒杀成功，订单处理中");
        } else if (result == 0) {
            return CommonResult.failed("库存不足，秒杀已结束");
        } else if (result == -1) {
            return CommonResult.failed("您已参与过该商品的秒杀，请勿重复购买");
        } else {
            return CommonResult.failed("秒杀失败");
        }
    }

    @Operation(summary = "查询秒杀商品剩余库存")
    @GetMapping("/stock")
    public CommonResult<Long> getSeckillStock(@RequestParam Long promotionId,
                                               @RequestParam Long productId) {
        Long stock = seckillService.getSeckillStock(promotionId, productId);
        return CommonResult.success(stock);
    }

    @Operation(summary = "获取当前秒杀活动列表")
    @GetMapping("/list")
    public CommonResult<List<SeckillProductDetailDTO>> getCurrentSeckillProducts() {
        List<SeckillProductDetailDTO> list = seckillService.getCurrentSeckillProducts();
        return CommonResult.success(list);
    }

    @Operation(summary = "获取秒杀商品详情")
    @GetMapping("/detail")
    public CommonResult<SeckillProductDetailDTO> getSeckillProductDetail(@RequestParam Long promotionId,
                                                                            @RequestParam Long productId) {
        SeckillProductDetailDTO detail = seckillService.getSeckillProductDetail(promotionId, productId);
        if (detail == null) {
            return CommonResult.failed("秒杀商品不存在");
        }
        return CommonResult.success(detail);
    }
}
