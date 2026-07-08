package com.mall.marketing.service;

import com.mall.marketing.model.CmsSubject;

import java.util.List;

/**
 * 商品专题管理Service
 * Created by macro on 2018/6/1.
 */
public interface ISubjectService {
    /**
     * 查询所有专题
     */
    List<CmsSubject> listAll();

    /**
     * 分页查询专题
     */
    List<CmsSubject> list(String keyword, Integer pageNum, Integer pageSize);
}
