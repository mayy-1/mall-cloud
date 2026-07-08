package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.marketing.mapper.CmsSubjectMapper;
import com.mall.marketing.model.CmsSubject;
import com.mall.marketing.model.CmsSubjectExample;
import com.mall.marketing.service.ISubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品专题管理Service实现类
 * Created by macro on 2018/6/1.
 */
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements ISubjectService {
    /** 商品专题Mapper */
    private final CmsSubjectMapper subjectMapper;

    @Override
    public List<CmsSubject> listAll() {
        return subjectMapper.selectByExample(new CmsSubjectExample());
    }

    @Override
    public List<CmsSubject> list(String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        CmsSubjectExample example = new CmsSubjectExample();
        CmsSubjectExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(keyword)) {
            criteria.andTitleLike("%" + keyword + "%");
        }
        return subjectMapper.selectByExample(example);
    }
}
