# mall-cloud 搜索服务学习笔记

---

# 第一部分：search-service 模块完整梳理

## 一、模块定位

`search-service` 是 mall-cloud 微服务电商平台的 **全文搜索服务**，基于 **Elasticsearch** 提供商品的全文检索、多条件筛选（品牌/分类/属性）、多维度排序（综合/销量/价格/新品）以及基于商品名称的相似商品推荐。数据从 MySQL（product-service 数据库）导入 ES，搜索请求不再经过 MySQL，实现读写分离。

---

## 二、技术架构全景

```
              mall-gateway (:/mall-search/** → 白名单放行)
                           │
                           v
                   mall-portal (BFF)
                           │
                           v
    ┌──────────────────────────────────────┐
    │       search-service :8093          │
    │       (Elasticsearch 搜索服务)        │
    │                                      │
    │  ┌────────────────────────────────┐  │
    │  │ EsProductController         ★ │  │
    │  │ POST /esProduct/importAll     │  │  ← 全量导入 ES
    │  │ GET  /esProduct/search/simple │  │  ← 简单搜索
    │  │ GET  /esProduct/search        │  │  ← 综合搜索 ★
    │  │ GET  /esProduct/recommend/{id}│  │  ← 相似推荐 ★
    │  └────────────────────────────────┘  │
    │                                      │
    │  搜索引擎: Elasticsearch (pms 索引)   │
    │  数据源:   MySQL (pms_product)       │
    │  分词器:   ik_max_word               │
    └──────────────────────────────────────┘
```

---

## 三、目录结构

```
search-service/
├── pom.xml
└── src/main/
    ├── java/com/mall/search/
    │   ├── SearchApplication.java        # 启动类
    │   ├── config/
    │   │   ├── MyBatisConfig.java        # MyBatis（只读 MySQL）
    │   │   └── SpringDocConfig.java      # API 文档
    │   ├── controller/
    │   │   └── EsProductController.java  # 搜索控制器 ★
    │   ├── domain/
    │   │   ├── EsProduct.java            # ES 文档实体 ★
    │   │   ├── EsProductAttributeValue.java
    │   │   └── EsProductRelatedInfo.java
    │   ├── mapper/
    │   │   └── EsProductMapperCustom.java # 自定义 Mapper
    │   ├── repository/
    │   │   └── EsProductRepository.java  # ES Repository
    │   └── service/
    │       ├── IEsProductService.java
    │       └── impl/
    │           └── EsProductServiceImpl.java ★
    └── resources/
        ├── application.yml               # ES 连接配置
        ├── application-dev.yml
        ├── application-prod.yml
        └── mapper/
            └── EsProductMapperCustom.xml  # 数据导入 SQL ★
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `spring-boot-starter-data-elasticsearch` | **Elasticsearch 操作核心**（Spring Data ES） |
| `mybatis-spring-boot-starter` | MyBatis（仅用于从 MySQL 读取商品数据导入 ES） |
| `spring-boot-starter-web` | Web 服务 |
| `spring-cloud-starter-alibaba-nacos-discovery/config` | Nacos |
| `knife4j-openapi3-jakarta-spring-boot-starter` | API 文档 |

---

## 五、ES 索引设计详解

### 5.1 索引定义

```java
@Document(indexName = "pms")               // 索引名
@Setting(shards = 1, replicas = 0)         // 1 个分片，0 个副本（开发环境）
public class EsProduct implements Serializable {
    @Id
    private Long id;                        // 文档 ID = 商品 ID

    @Field(type = FieldType.Keyword)
    private String productSn;               // 货号（精确匹配）

    @Field(analyzer = "ik_max_word", type = FieldType.Text)
    private String name;                    // 商品名称（分词搜索）

    @Field(analyzer = "ik_max_word", type = FieldType.Text)
    private String subTitle;                // 副标题（分词搜索）

    @Field(analyzer = "ik_max_word", type = FieldType.Text)
    private String keywords;                // 关键词（分词搜索）

    @Field(type = FieldType.Keyword)
    private String brandName;               // 品牌名（精确匹配）

    @Field(type = FieldType.Keyword)
    private String productCategoryName;     // 分类名（精确匹配）

    @Field(type = FieldType.Nested)
    private List<EsProductAttributeValue> attrValueList; // 属性值（嵌套类型）
}
```

### 5.2 字段类型策略

| 字段 | ES 类型 | 分词器 | 用途 |
|------|---------|--------|------|
| `name` | Text | ik_max_word | 全文搜索（主要匹配字段） |
| `subTitle` | Text | ik_max_word | 全文搜索（辅助匹配） |
| `keywords` | Text | ik_max_word | 全文搜索（SEO 关键词） |
| `productSn` | Keyword | 无 | 精确匹配（货号查询） |
| `brandName` | Keyword | 无 | 精确筛选（品牌过滤） |
| `productCategoryName` | Keyword | 无 | 精确筛选（分类过滤） |
| `attrValueList` | Nested | - | 属性筛选（颜色/内存等） |

### 5.3 为什么 attrValueList 用 Nested 类型？

```json
// ❌ Object 类型的问题：扁平化后失去关联
{
  "attrValueList": [
    {"name": "颜色", "value": "黑色"},
    {"name": "内存", "value": "128G"}
  ]
}
// 扁平化后查询 "颜色=128G" 可能误匹配

// ✅ Nested 类型：保持数组内对象的独立性
// 查询 "颜色=黑色" 只匹配颜色对象，不会跨对象匹配
```

---

## 六、核心功能详解

### 6.1 全量数据导入

```java
// POST /esProduct/importAll
public CommonResult<Integer> importAllList() {
    // 1. 从 MySQL 查询所有上架商品（含属性值）
    List<EsProduct> esProductList = productMapper.getAllEsProductList(null);
    // 2. 批量保存到 ES
    Iterable<EsProduct> result = productRepository.saveAll(esProductList);
    return CommonResult.success(count);
}
```

**数据导入 SQL**（EsProductMapperCustom.xml）：
```sql
SELECT p.*, pav.id attr_id, pav.value attr_value,
       pa.type attr_type, pa.name attr_name
FROM pms_product p
LEFT JOIN pms_product_attribute_value pav ON p.id = pav.product_id
LEFT JOIN pms_product_attribute pa ON pav.product_attribute_id = pa.id
WHERE delete_status = 0 AND publish_status = 1
```

### 6.2 综合搜索 — Function Score 加权打分

```java
// EsProductServiceImpl.search()
FunctionScoreQuery functionScoreQuery = QueryBuilders.functionScore(
    // 搜索查询：name/subTitle/keywords 分别匹配
    QueryBuilders.bool(b -> b
        .should(QueryBuilders.match(m -> m.field("name").query(keyword)))
        .should(QueryBuilders.match(m -> m.field("subTitle").query(keyword)))
        .should(QueryBuilders.match(m -> m.field("keywords").query(keyword)))
    ),
    // 权重函数
    new FunctionScore[]{
        new FunctionScore(FilterFunction.of(
            f -> f.filter(QueryBuilders.match(m -> m.field("name").query(keyword))),
            WeightScore.of(10.0)        // name 匹配权重 10
        )),
        new FunctionScore(FilterFunction.of(
            f -> f.filter(QueryBuilders.match(m -> m.field("subTitle").query(keyword))),
            WeightScore.of(5.0)         // subTitle 匹配权重 5
        )),
        new FunctionScore(FilterFunction.of(
            f -> f.filter(QueryBuilders.match(m -> m.field("keywords").query(keyword))),
            WeightScore.of(2.0)         // keywords 匹配权重 2
        ))
    }
).scoreMode(FunctionScoreQuery.ScoreMode.Sum)
 .minScore(2.0);  // 最低相关度分数
```

**权重设计逻辑**：
- `name` 匹配权重最高（10）：商品名是最核心的搜索字段
- `subTitle` 中等权重（5）：副标题提供补充匹配
- `keywords` 较低权重（2）：SEO 关键词匹配只作为辅助

### 6.3 品牌/分类筛选

```java
// 品牌和分类使用 term 查询（精确匹配）
BoolQuery boolQuery = QueryBuilders.bool();
if (brandId != null) {
    boolQuery.must(QueryBuilders.term(t -> t.field("brandId").value(brandId)));
}
if (productCategoryId != null) {
    boolQuery.must(QueryBuilders.term(t -> t.field("productCategoryId").value(productCategoryId)));
}
```

### 6.4 排序策略

```java
switch (sort) {
    case 1: → id desc           // 新品优先
    case 2: → sale desc         // 销量优先
    case 3: → price asc         // 价格从低到高
    case 4: → price desc        // 价格从高到低
    default: → _score desc      // 综合排序（相关度）
}
```

### 6.5 相似商品推荐

```java
// GET /esProduct/recommend/{id}
public CommonPage<EsProduct> recommend(@PathVariable Long id, ...) {
    // 1. 获取当前商品信息
    EsProduct product = productRepository.findById(id).get();
    // 2. 用商品名和品牌构造查询
    FunctionScoreQuery query = QueryBuilders.functionScore(
        QueryBuilders.bool(b -> b
            .mustNot(QueryBuilders.term(t -> t.field("id").value(id)))  // 排除自身
        ),
        new FunctionScore[]{
            // name 匹配 权重 8
            filter(QueryBuilders.match(m -> m.field("name").query(product.getName())), weight(8.0)),
            // brandId 匹配 权重 5
            filter(QueryBuilders.term(t -> t.field("brandId").value(product.getBrandId())), weight(5.0)),
            // productCategoryId 匹配 权重 3
            filter(QueryBuilders.term(t -> t.field("productCategoryId").value(product.getProductCategoryId())), weight(3.0))
        }
    ).scoreMode(Sum).minScore(2.0);
}
```

**推荐逻辑**：同品牌（权重5）+ 同分类（权重3）+ 名称相似（权重8），排除自身。

---

## 七、核心设计亮点总结

1. **读写分离**：搜索读 ES、管理读写 MySQL，互不干扰
2. **ik_max_word 分词**：中文商品名搜索的核心，细粒度分词提高召回率
3. **Function Score 加权**：不同字段匹配度赋予不同权重，搜索结果更符合预期
4. **Nested 属性存储**：保证多属性筛选的正确性
5. **多维度排序**：综合/销量/价格/新品四种排序满足不同场景
6. **相似推荐**：利用商品名+品牌+分类的加权匹配推荐相似商品
7. **手动触发导入**：管理员在后台操作商品后手动触发 ES 索引重建

---

---

# 第二部分：Java 面试 Elasticsearch 高频问题

## 面试题 1：为什么商品搜索用 ES 而不是 MySQL LIKE？

| 维度 | MySQL LIKE | Elasticsearch |
|------|-----------|---------------|
| 全文搜索 | 不支持分词，只能模糊匹配 | 支持 IK 分词器，语义搜索 |
| 性能 | 大表全表扫描 O(n) | 倒排索引 O(1) 查找 |
| 排序 | 简单排序 | 相关度打分 + 多维度排序 |
| 高亮 | 不支持 | 原生支持 |
| 聚合 | 不支持 | 原生支持（品牌/分类/属性聚合） |
| 扩展性 | 垂直扩展 | 水平扩展（分片） |

**面试回答建议**：
> MySQL 的 LIKE 查询是全表扫描，百万级商品数据的搜索性能无法接受。ES 使用倒排索引，搜索时间复杂度是 O(1)。它支持 IK 中文分词器，能把"苹果手机"拆成"苹果"+"手机"，实现语义级别的搜索。还支持相关度打分、多维度排序、聚合筛选等电商搜索的刚需功能。

---

## 面试题 2：ik_max_word 和 ik_smart 有什么区别？

| 维度 | ik_max_word | ik_smart |
|------|-------------|----------|
| 分词粒度 | 最细粒度 | 粗粒度 |
| 示例（"中华人民共和国国歌"） | 中华人民共和国/中华人民/中华/华人/人民共和国/人民/共和国/共和/国歌 | 中华人民共和国/国歌 |
| 召回率 | 高 | 低 |
| 精确度 | 低 | 高 |

电商搜索场景选择 `ik_max_word`：用户搜"苹果手机"，能匹配到标题中含"苹果"或"手机"的商品，召回率更高。

---

## 面试题 3：ES 的倒排索引原理是什么？

```
正向索引（MySQL）：文档ID → 包含哪些词
倒排索引（ES）：    词 → 出现在哪些文档中

示例：
文档1: "苹果手机"
文档2: "华为手机"
文档3: "苹果电脑"

倒排索引:
"苹果" → [文档1, 文档3]
"手机" → [文档1, 文档2]
"华为" → [文档2]
"电脑" → [文档3]

搜索"手机" → 直接查倒排索引 → 返回文档1和文档2 → O(1)
```

---

## 面试题 4：MySQL 数据如何同步到 ES？

**本项目的方案**：手动触发全量导入
```java
POST /esProduct/importAll → 从 MySQL 查询 → saveAll 到 ES
```

**其他方案对比**：

| 方案 | 实时性 | 复杂度 | 适用场景 |
|------|--------|--------|---------|
| 手动导入（本项目） | 低 | 低 | 数据变更不频繁 |
| Canal（监听 binlog） | 高（秒级） | 高 | 实时性要求高 |
| MQ 异步同步 | 中 | 中 | 数据量大有削峰需求 |
| Logstash 定时同步 | 中 | 低 | 定时全量更新 |

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| ES vs MySQL | 倒排索引 vs 全表扫描，分词搜索 vs 模糊匹配 |
| ik_max_word | 最细粒度分词，召回率高，适合电商搜索 |
| 倒排索引 | 词→文档映射，O(1) 查找 |
| Nested 类型 | 保持数组内对象独立性，避免跨对象匹配 |
| Function Score | 不同匹配字段赋予不同权重，优化排序效果 |
| 数据同步 | 手动全量导入（本项目），Canal/binlog（生产） |

---

*学习日期：2026-07-08*
