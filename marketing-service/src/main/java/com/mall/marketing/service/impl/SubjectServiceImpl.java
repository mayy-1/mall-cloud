package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.marketing.mapper.CmsSubjectMapper;
import com.mall.marketing.model.CmsSubject;import com.mall.marketing.service.ISubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品专题管理Service实现类
 */
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements ISubjectService {
    /** 商品专题Mapper */
    private final CmsSubjectMapper subjectMapper;

    @Override
    public List<CmsSubject> listAll() {
        return subjectMapper.selectByCondition(new CmsSubject());
    }

    @Override
    public List<CmsSubject> list(String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        CmsSubject condition = new CmsSubject();
        if (!StringUtils.isEmpty(keyword)) {
            condition.setTitle("%" + keyword + "%");
        }
        return subjectMapper.selectByCondition(condition);
    }

    @Override
    public List<CmsSubject> listSome() {
        CmsSubject condition = new CmsSubject();
        condition.setRecommendStatus(1);
        condition.setShowStatus(1);
        return subjectMapper.selectByCondition(condition);
    }

    @Override
    public int create(CmsSubject subject) {
        subject.setCreateTime(new java.util.Date());
        return subjectMapper.insertSelective(subject);
    }

    @Override
    public int update(Long id, CmsSubject subject) {
        subject.setId(id);
        return subjectMapper.updateByPrimaryKeySelective(subject);
    }

    @Override
    public int delete(Long id) {
        return subjectMapper.deleteByPrimaryKey(id);
    }
}
