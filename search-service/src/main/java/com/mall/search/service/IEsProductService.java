package com.mall.search.service;

import com.mall.search.domain.EsProduct;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 商品搜索管理Service
 * <p>
 * 对外只保留两个核心能力：全量同步（fullSync，手动初始化/兜底）与综合搜索（用户端）；
 * 增量（增/改/删）由 Canal → MQ → {@code CanalProductReceiver} 消费后调用 create/delete，
 * 不经过 Controller。
 */
public interface IEsProductService {

    /**
     * 全量同步：从 MySQL 查询全部上架商品，Bulk 写入 ES（手动初始化 / 兜底）
     *
     * @return 同步的商品条数
     */
    int fullSync();

    /**
     * 根据 id 从 ES 查询单个商品文档（内部调试 / 排查用）
     */
    EsProduct getEsProductById(Long id);

    /**
     * 根据 id 删除 ES 中的商品文档（增量删除走 MQ 消费）
     */
    void delete(Long id);

    /**
     * 根据 id 创建/更新 ES 商品文档（增量增改走 MQ 消费；重新查 MySQL 组装完整文档 upsert）
     */
    EsProduct create(Long id);

    /**
     * 根据 id 列表批量删除 ES 商品文档
     */
    void delete(List<Long> ids);

    /**
     * 综合搜索：关键词(名称/副标题/关键词加权) + 品牌/分类精确筛选 + 排序
     *
     * @param keyword           搜索关键词，可为空（空则查全部）
     * @param brandId           品牌ID，可为空
     * @param productCategoryId 商品分类ID（二级），可为空
     * @param pageNum           页码
     * @param pageSize          每页条数
     * @param sort              排序：0-相关度；1-新品；2-销量；3-价格升；4-价格降
     */
    Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId,
                           Integer pageNum, Integer pageSize, Integer sort);
}
