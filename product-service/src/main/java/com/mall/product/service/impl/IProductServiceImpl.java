package com.mall.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageHelper;
import com.mall.product.mapper.*;
import com.mall.product.domain.dto.PmsProductParam;
import com.mall.product.domain.dto.PmsProductQueryParam;
import com.mall.product.domain.dto.PmsProductResult;
import com.mall.product.model.*;
import com.mall.product.service.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品管理Service实现类
 * Created by macro on 2018/4/26.
 */
@Service
@RequiredArgsConstructor
public class IProductServiceImpl implements IProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IProductServiceImpl.class);

    /** 商品Mapper */
    private final PmsProductMapper productMapper;

    /** 会员价格Mapper */
    private final PmsMemberPriceMapper memberPriceMapper;

    /** 商品阶梯价格Mapper */
    private final PmsProductLadderMapper productLadderMapper;

    /** 满减Mapper */
    private final PmsProductFullReductionMapper productFullReductionMapper;

    /** SKU库存Mapper */
    private final PmsSkuStockMapper skuStockMapper;

    /** 商品属性值Mapper */
    private final PmsProductAttributeValueMapper productAttributeValueMapper;

    /** 专题商品关系Mapper */
    private final CmsSubjectProductRelationMapper subjectProductRelationMapper;

    /** 优选商品关系Mapper */
    private final CmsPrefrenceAreaProductRelationMapper prefrenceAreaProductRelationMapper;

    /** 商品审核记录Mapper */
    private final PmsProductVertifyRecordMapper productVertifyRecordMapper;

    @Override
    public int create(PmsProductParam productParam) {
        int count;
        //创建商品
        PmsProduct product = productParam;
        product.setId(null);
        productMapper.insertSelective(product);
        //根据促销类型设置价格：会员价格、阶梯价格、满减价格
        Long productId = product.getId();
        //会员价格
        relateAndInsertList(memberPriceMapper, productParam.getMemberPriceList(), productId);
        //阶梯价格
        relateAndInsertList(productLadderMapper, productParam.getProductLadderList(), productId);
        //满减价格
        relateAndInsertList(productFullReductionMapper, productParam.getProductFullReductionList(), productId);
        //处理sku的编码
        handleSkuStockCode(productParam.getSkuStockList(), productId);
        //添加sku库存信息
        relateAndInsertList(skuStockMapper, productParam.getSkuStockList(), productId);
        //添加商品参数,添加自定义商品规格
        relateAndInsertList(productAttributeValueMapper, productParam.getProductAttributeValueList(), productId);
        //关联专题
        relateAndInsertList(subjectProductRelationMapper, productParam.getSubjectProductRelationList(), productId);
        //关联优选
        relateAndInsertList(prefrenceAreaProductRelationMapper, productParam.getPrefrenceAreaProductRelationList(), productId);
        count = 1;
        return count;
    }

    private void handleSkuStockCode(List<PmsSkuStock> skuStockList, Long productId) {
        if (CollectionUtils.isEmpty(skuStockList)) return;
        for (int i = 0; i < skuStockList.size(); i++) {
            PmsSkuStock skuStock = skuStockList.get(i);
            if (StringUtils.isEmpty(skuStock.getSkuCode())) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                StringBuilder sb = new StringBuilder();
                //日期
                sb.append(sdf.format(new Date()));
                //四位商品id
                sb.append(String.format("%04d", productId));
                //三位索引id
                sb.append(String.format("%03d", i + 1));
                skuStock.setSkuCode(sb.toString());
            }
        }
    }

    @Override
    public PmsProductResult getUpdateInfo(Long id) {
        return productMapper.getUpdateInfo(id);
    }

    @Override
    public int update(Long id, PmsProductParam productParam) {
        int count;
        //更新商品信息
        PmsProduct product = productParam;
        product.setId(id);
        productMapper.updateByPrimaryKeySelective(product);
        //会员价格
        PmsMemberPrice memberPriceCondition = new PmsMemberPrice();
        memberPriceCondition.setProductId(id);
        memberPriceMapper.deleteByCondition(memberPriceCondition);
        relateAndInsertList(memberPriceMapper, productParam.getMemberPriceList(), id);
        //阶梯价格
        PmsProductLadder ladderCondition = new PmsProductLadder();
        ladderCondition.setProductId(id);
        productLadderMapper.deleteByCondition(ladderCondition);
        relateAndInsertList(productLadderMapper, productParam.getProductLadderList(), id);
        //满减价格
        PmsProductFullReduction fullReductionCondition = new PmsProductFullReduction();
        fullReductionCondition.setProductId(id);
        productFullReductionMapper.deleteByCondition(fullReductionCondition);
        relateAndInsertList(productFullReductionMapper, productParam.getProductFullReductionList(), id);
        //修改sku库存信息
        handleUpdateSkuStockList(id, productParam);
        //修改商品参数,添加自定义商品规格
        PmsProductAttributeValue productAttributeValueCondition = new PmsProductAttributeValue();
        productAttributeValueCondition.setProductId(id);
        productAttributeValueMapper.deleteByCondition(productAttributeValueCondition);
        relateAndInsertList(productAttributeValueMapper, productParam.getProductAttributeValueList(), id);
        //关联专题
        CmsSubjectProductRelation subjectProductRelationCondition = new CmsSubjectProductRelation();
        subjectProductRelationCondition.setProductId(id);
        subjectProductRelationMapper.deleteByCondition(subjectProductRelationCondition);
        relateAndInsertList(subjectProductRelationMapper, productParam.getSubjectProductRelationList(), id);
        //关联优选
        CmsPrefrenceAreaProductRelation prefrenceAreaCondition = new CmsPrefrenceAreaProductRelation();
        prefrenceAreaCondition.setProductId(id);
        prefrenceAreaProductRelationMapper.deleteByCondition(prefrenceAreaCondition);
        relateAndInsertList(prefrenceAreaProductRelationMapper, productParam.getPrefrenceAreaProductRelationList(), id);
        count = 1;
        return count;
    }

    private void handleUpdateSkuStockList(Long id, PmsProductParam productParam) {
        //当前的sku信息
        List<PmsSkuStock> currSkuList = productParam.getSkuStockList();
        //当前没有sku直接删除
        if (CollUtil.isEmpty(currSkuList)) {
            PmsSkuStock skuStockCondition = new PmsSkuStock();
            skuStockCondition.setProductId(id);
            skuStockMapper.deleteByCondition(skuStockCondition);
            return;
        }
        //获取初始sku信息
        PmsSkuStock skuStockCondition = new PmsSkuStock();
        skuStockCondition.setProductId(id);
        List<PmsSkuStock> oriStuList = skuStockMapper.selectByCondition(skuStockCondition);
        //获取新增sku信息
        List<PmsSkuStock> insertSkuList = currSkuList.stream().filter(item -> item.getId() == null).collect(Collectors.toList());
        //获取需要更新的sku信息
        List<PmsSkuStock> updateSkuList = currSkuList.stream().filter(item -> item.getId() != null).collect(Collectors.toList());
        List<Long> updateSkuIds = updateSkuList.stream().map(PmsSkuStock::getId).collect(Collectors.toList());
        //获取需要删除的sku信息
        List<PmsSkuStock> removeSkuList = oriStuList.stream().filter(item -> !updateSkuIds.contains(item.getId())).collect(Collectors.toList());
        handleSkuStockCode(insertSkuList, id);
        handleSkuStockCode(updateSkuList, id);
        //新增sku
        if (CollUtil.isNotEmpty(insertSkuList)) {
            relateAndInsertList(skuStockMapper, insertSkuList, id);
        }
        //删除sku
        if (CollUtil.isNotEmpty(removeSkuList)) {
            List<Long> removeSkuIds = removeSkuList.stream().map(PmsSkuStock::getId).collect(Collectors.toList());
            for (Long skuId : removeSkuIds) {
                skuStockMapper.deleteByPrimaryKey(skuId);
            }
        }
        //修改sku
        if (CollUtil.isNotEmpty(updateSkuList)) {
            for (PmsSkuStock pmsSkuStock : updateSkuList) {
                skuStockMapper.updateByPrimaryKeySelective(pmsSkuStock);
            }
        }

    }

    @Override
    public List<PmsProduct> list(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        PmsProduct condition = new PmsProduct();
        condition.setDeleteStatus(0);
        if (productQueryParam.getPublishStatus() != null) {
            condition.setPublishStatus(productQueryParam.getPublishStatus());
        }
        if (productQueryParam.getVerifyStatus() != null) {
            condition.setVerifyStatus(productQueryParam.getVerifyStatus());
        }
        if (!StringUtils.isEmpty(productQueryParam.getKeyword())) {
            condition.setName("%" + productQueryParam.getKeyword() + "%");
        }
        if (!StringUtils.isEmpty(productQueryParam.getProductSn())) {
            condition.setProductSn(productQueryParam.getProductSn());
        }
        if (productQueryParam.getBrandId() != null) {
            condition.setBrandId(productQueryParam.getBrandId());
        }
        if (productQueryParam.getProductCategoryId() != null) {
            condition.setProductCategoryId(productQueryParam.getProductCategoryId());
        }
        if (productQueryParam.getNewStatus() != null) {
            condition.setNewStatus(productQueryParam.getNewStatus());
        }
        if (productQueryParam.getRecommandStatus() != null) {
            condition.setRecommandStatus(productQueryParam.getRecommandStatus());
        }
        return productMapper.selectByCondition(condition);
    }

    @Override
    public int updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail) {
        List<PmsProductVertifyRecord> list = new ArrayList<>();
        int count = 0;
        for (Long id : ids) {
            PmsProduct product = new PmsProduct();
            product.setId(id);
            product.setVerifyStatus(verifyStatus);
            count += productMapper.updateByPrimaryKeySelective(product);
        }
        //修改完审核状态后插入审核记录
        for (Long id : ids) {
            PmsProductVertifyRecord record = new PmsProductVertifyRecord();
            record.setProductId(id);
            record.setCreateTime(new Date());
            record.setDetail(detail);
            record.setStatus(verifyStatus);
            record.setVertifyMan("test");
            list.add(record);
        }
        productVertifyRecordMapper.insertList(list);
        return count;
    }

    @Override
    public int updatePublishStatus(List<Long> ids, Integer publishStatus) {
        int count = 0;
        for (Long id : ids) {
            PmsProduct product = new PmsProduct();
            product.setId(id);
            product.setPublishStatus(publishStatus);
            count += productMapper.updateByPrimaryKeySelective(product);
        }
        return count;
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        int count = 0;
        for (Long id : ids) {
            PmsProduct product = new PmsProduct();
            product.setId(id);
            product.setRecommandStatus(recommendStatus);
            count += productMapper.updateByPrimaryKeySelective(product);
        }
        return count;
    }

    @Override
    public int updateNewStatus(List<Long> ids, Integer newStatus) {
        int count = 0;
        for (Long id : ids) {
            PmsProduct product = new PmsProduct();
            product.setId(id);
            product.setNewStatus(newStatus);
            count += productMapper.updateByPrimaryKeySelective(product);
        }
        return count;
    }

    @Override
    public int updateDeleteStatus(List<Long> ids, Integer deleteStatus) {
        int count = 0;
        for (Long id : ids) {
            PmsProduct product = new PmsProduct();
            product.setId(id);
            product.setDeleteStatus(deleteStatus);
            count += productMapper.updateByPrimaryKeySelective(product);
        }
        return count;
    }

    @Override
    public List<PmsProduct> list(String keyword) {
        PmsProduct condition = new PmsProduct();
        condition.setDeleteStatus(0);
        if (!StringUtils.isEmpty(keyword)) {
            condition.setName("%" + keyword + "%");
        }
        return productMapper.selectByCondition(condition);
    }

    @Override
    public PmsProduct getItem(Long id) {
        return productMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<PmsProduct> listByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<PmsProduct> result = new ArrayList<>();
        for (Long id : ids) {
            PmsProduct product = productMapper.selectByPrimaryKey(id);
            if (product != null && product.getDeleteStatus() != null && product.getDeleteStatus() == 0) {
                result.add(product);
            }
        }
        return result;
    }

    /**
     * 建立和插入关系表操作
     *
     * @param dao       可以操作的dao
     * @param dataList  要插入的数据
     * @param productId 建立关系的id
     */
    private void relateAndInsertList(Object dao, List dataList, Long productId) {
        try {
            if (CollectionUtils.isEmpty(dataList)) return;
            for (Object item : dataList) {
                Method setId = item.getClass().getMethod("setId", Long.class);
                setId.invoke(item, (Long) null);
                Method setProductId = item.getClass().getMethod("setProductId", Long.class);
                setProductId.invoke(item, productId);
            }
            Method insertList = dao.getClass().getMethod("insertList", List.class);
            insertList.invoke(dao, dataList);
        } catch (Exception e) {
            LOGGER.warn("创建产品出错:{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}
