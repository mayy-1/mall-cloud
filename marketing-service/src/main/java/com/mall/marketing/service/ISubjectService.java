package com.mall.marketing.service;

import com.mall.marketing.model.CmsSubject;

import java.util.List;

/**
 * 商品专题管理Service
 */
public interface ISubjectService {
    List<CmsSubject> listAll();

    List<CmsSubject> list(String keyword, Integer pageNum, Integer pageSize);

    List<CmsSubject> listSome();

    int create(CmsSubject subject);

    int update(Long id, CmsSubject subject);

    int delete(Long id);
}
