package com.mall.user.service;

import com.mall.user.model.UmsResource;

import java.util.List;
import java.util.Map;

/**
 * 后台资源管理Service
 * Created by macro on 2020/2/2.
 */
public interface IResourceService {
    /**
     * 获取资源详情
     */
    UmsResource getItem(Long id);

    /**
     * 分页查询资源
     */
    List<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword, Integer pageSize, Integer pageNum);

    /**
     * 查询全部资源
     */
    List<UmsResource> listAll();

    /**
     * 初始化路径与资源访问规则
     */
    Map<String, String> initPathResourceMap();
}
