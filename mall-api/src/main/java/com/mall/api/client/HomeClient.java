package com.mall.api.client;

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

    /** 获取首页广告列表 */
    @GetMapping("/advertise/list")
    CommonResult<List<Object>> getHomeAdvertises();

    /** 分页获取推荐专题 */
    @GetMapping("/recommendSubject/list")
    CommonResult<List<SubjectDTO>> getRecommendSubjects(@RequestParam("pageNum") Integer pageNum,
                                                        @RequestParam("pageSize") Integer pageSize);
}
