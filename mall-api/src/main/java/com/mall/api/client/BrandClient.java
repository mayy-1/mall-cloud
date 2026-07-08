package com.mall.api.client;

import com.mall.api.dto.BrandDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌服务 Feign 接口
 */
@FeignClient(name = "product-service", path = "/brand")
public interface BrandClient {

    /** 根据ID查询品牌详情 */
    @GetMapping("/{id}")
    CommonResult<BrandDTO> getDetail(@PathVariable Long id);

    /** 查询全部品牌列表 */
    @GetMapping("/list")
    CommonResult<List<BrandDTO>> listAll();
}
