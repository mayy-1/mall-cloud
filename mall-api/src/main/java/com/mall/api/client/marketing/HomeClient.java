package com.mall.api.client.marketing;

import com.mall.api.dto.HomeAdvertiseDTO;
import com.mall.api.dto.SubjectDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页内容 Feign 接口
 */
@FeignClient(name = "marketing-service", path = "/home", contextId = "marketing-home")
public interface HomeClient {

    /** 获取首页已上线广告列表 */
    @GetMapping("/advertise/search")
    CommonResult<List<HomeAdvertiseDTO>> getHomeAdvertises();

    /** 分页获取推荐专题 */
    @GetMapping("/recommendSubject/list")
    CommonResult<List<SubjectDTO>> getRecommendSubjects(@RequestParam("pageNum") Integer pageNum,
                                                        @RequestParam("pageSize") Integer pageSize);

    /** 获取所有启用的首页新品商品ID */
    @GetMapping("/newProduct/activeIds")
    CommonResult<List<Long>> getActiveNewProductIds();

    /** 获取所有启用的人气推荐商品ID */
    @GetMapping("/recommendProduct/activeIds")
    CommonResult<List<Long>> getActiveRecommendProductIds();
}
