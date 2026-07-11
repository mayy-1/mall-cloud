package com.mall.api.client.marketing;

import com.mall.api.dto.SubjectDTO;
import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专题服务 Feign 接口
 */
@FeignClient(name = "marketing-service", path = "/subject", contextId = "marketing-subject")
public interface SubjectClient {

    /** 获取全部专题 */
    @GetMapping("/listAll")
    CommonResult<List<SubjectDTO>> listAll();

    /** 分页搜索专题 */
    @GetMapping("/list")
    CommonResult<CommonPage<SubjectDTO>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                              @RequestParam("pageNum") Integer pageNum,
                                              @RequestParam("pageSize") Integer pageSize);
}
