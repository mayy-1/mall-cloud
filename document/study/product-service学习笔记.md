# mall-cloud 商品服务学习笔记

---

# 第一部分：product-service 模块完整梳理

## 一、模块定位

`product-service` 是 mall-cloud 微服务电商平台的 **核心商品服务**，负责商品全生命周期管理：商品 CRUD（含阶梯价/满减/会员价）、品牌管理、商品分类（树形结构）、商品属性（规格+参数）、SKU 库存管理、审核上架流程、以及 MinIO/OSS 双文件存储方案。是整个电商系统中数据量最大、关联关系最复杂的模块。

---

## 二、技术架构全景

```
                     mall-gateway :8201
                           │
             ┌─────────────┼─────────────┐
             v             v             v
      mall-auth      mall-admin     mall-portal
         │               │              │
         v               v              v
  ┌──────────────────────────────────────────┐
  │         product-service :8082            │
  │         (商品核心服务)                      │
  │                                          │
  │  ┌────────────────────────────────────┐  │
  │  │ ProductController          ★      │  │  ← 商品 CRUD + 审核
  │  │ BrandController                    │  │  ← 品牌管理
  │  │ CategoryController                 │  │  ← 分类管理（树形）
  │  │ AttributeController                │  │  ← 属性管理
  │  │ AttributeCategoryController        │  │  ← 属性分类管理
  │  │ SkuController                      │  │  ← SKU 库存管理
  │  │ MinioController                    │  │  ← MinIO 文件存储
  │  │ OssController                      │  │  ← OSS 文件存储
  │  └────────────────────────────────────┘  │
  │                                          │
  │  数据库: mall_product (MySQL)            │
  │  缓存: Redis                             │
  │  文件: MinIO / Aliyun OSS               │
  │  连接池: Druid                           │
  └──────────────────────────────────────────┘
```

---

## 三、目录结构

```
product-service/
├── pom.xml
└── src/main/
    ├── java/com/mall/product/
    │   ├── ProductApplication.java         # 启动类
    │   ├── config/
    │   │   └── MyBatisConfig.java          # MyBatis + 事务管理
    │   ├── controller/
    │   │   ├── ProductController.java      # 商品管理 ★★
    │   │   ├── BrandController.java        # 品牌管理
    │   │   ├── CategoryController.java     # 分类管理
    │   │   ├── AttributeController.java    # 属性管理
    │   │   ├── AttributeCategoryController.java
    │   │   ├── SkuController.java          # SKU 库存
    │   │   ├── MinioController.java        # MinIO 上传
    │   │   └── OssController.java          # OSS 上传
    │   ├── domain/dto/ (14个 DTO 类)
    │   │   ├── PmsProductParam.java        # ★ 创建/修改商品 DTO
    │   │   ├── PmsProductQueryParam.java   # 商品查询参数
    │   │   ├── PmsProductResult.java       # 商品编辑结果
    │   │   ├── PmsBrandParam.java          # 品牌参数
    │   │   └── ...
    │   ├── mapper/ (29个 Mapper 接口)
    │   │   ├── PmsProductMapperCustom.java # 自定义商品查询
    │   │   ├── PmsSkuStockMapperCustom.java # 批量 SKU 操作
    │   │   └── ...
    │   ├── model/ (24个实体类)
    │   └── service/ (7个 Service 接口 + 实现)
    │       ├── IProductService / impl     # ★ 核心
    │       └── ...
    └── resources/
        ├── application.yml
        ├── application-dev.yml
        └── mapper/ (35个 MyBatis XML)
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `mybatis-spring-boot-starter` | MyBatis ORM |
| `druid-spring-boot-3-starter` | Druid 连接池 |
| `mysql-connector-j` | MySQL 驱动 |
| `pagehelper-spring-boot-starter` | PageHelper 分页 |
| `spring-boot-starter-data-redis` | Redis 缓存 |
| `aliyun-sdk-oss` | **阿里云 OSS SDK** |
| `io.minio:minio` | **MinIO SDK** |
| `knife4j-openapi3-jakarta-spring-boot-starter` | API 文档 |
| `spring-boot-admin-starter-client` | 监控客户端 |
| `spring-cloud-starter-alibaba-nacos-discovery/config` | Nacos |

---

## 五、核心功能详解

### 5.1 ProductController — 商品管理 ★★

**商品 CRUD**：

| API | 方法 | 路径 | 说明 |
|-----|------|------|------|
| create | POST | /product/create | 创建商品（含关联数据） |
| update | POST | /product/update/{id} | 更新商品（先删后增关联） |
| getUpdateInfo | GET | /product/updateInfo/{id} | 获取商品编辑信息 |
| list | GET | /product/list | 分页查询商品列表 |

**商品状态管理**：

| API | 方法 | 路径 | 说明 |
|-----|------|------|------|
| updateVerifyStatus | POST | /product/update/verifyStatus | 批量审核商品 |
| updatePublishStatus | POST | /product/update/publishStatus | 批量上下架 |
| updateRecommendStatus | POST | /product/update/recommendStatus | 批量推荐 |
| updateNewStatus | POST | /product/update/newStatus | 批量设为新品 |
| updateDeleteStatus | POST | /product/update/deleteStatus | 批量删除（软删除） |

**前台接口**（供 portal-service 调用）：

| API | 方法 | 路径 | 说明 |
|-----|------|------|------|
| getById | GET | /product/{id} | 按 ID 查询商品 |
| getDetail | GET | /product/detail/{id} | 商品详情 |
| getByIds | GET | /product/batch | 批量获取 |
| search | GET | /product/search | 综合搜索 |
| getHotProducts | GET | /product/hot | 热门商品 |
| getNewProducts | GET | /product/new | 新品推荐 |
| categoryTreeList | GET | /product/categoryTreeList | 分类树 |

### 5.2 商品创建核心逻辑

```java
// ProductServiceImpl.create()
@Transactional
public int create(PmsProductParam productParam) {
    // 1. 插入商品主记录 → pms_product
    productMapper.insert(productParam);

    // 2. 通过反射调用 relateAndInsertList() 统一处理关联数据
    // 会员价格 → pms_member_price
    // 阶梯价格 → pms_product_ladder
    // 满减价格 → pms_product_full_reduction
    // SKU库存  → pms_sku_stock (含SKU编码自动生成)
    // 商品属性值 → pms_product_attribute_value
    // 专题关联   → cms_subject_product_relation
    // 优选关联   → cms_prefrence_area_product_relation
}
```

**SKU 编码生成规则**：
```
格式: yyyyMMdd + 4位商品ID + 3位序号
示例: 202607080001001
      ↑日期    ↑商品ID  ↑第1个SKU
```

### 5.3 商品更新策略 — 先删后增

```java
// 更新关联数据时采用"先删后增"策略
deleteMemberPrice(productId);     // 删除旧的会员价格
insertMemberPrice(newPrices);     // 插入新的会员价格

deleteLadder(productId);          // 删除旧的阶梯价
insertLadder(newLadders);         // 插入新的阶梯价

// SKU 更新特殊处理：比对区分新增/修改/删除
for (SkuStock sku : newSkuList) {
    if (sku.getId() == null) → insert (新增)
    else → update (修改)
}
// 删除不在新列表中的旧 SKU
deleteRemovedSkus(oldIds, newIds);
```

### 5.4 品牌管理 — 级联更新

```java
// 更新品牌名称时，同步更新关联商品中的品牌名
PmsProduct product = new PmsProduct();
product.setBrandName(pmsBrand.getName());
PmsProductExample example = new PmsProductExample();
example.createCriteria().andBrandIdEqualTo(id);
productMapper.updateByExampleSelective(product, example);
```

### 5.5 分类管理 — 自动计算层级

```java
// 创建分类时自动计算 level
if (parentId == 0) {
    category.setLevel(0);  // 一级分类
} else {
    PmsProductCategory parent = getById(parentId);
    category.setLevel(parent.getLevel() + 1);
}
```

分类树查询（自关联 LEFT JOIN）：
```sql
SELECT c1.id, c1.name, c2.id child_id, c2.name child_name
FROM pms_product_category c1
LEFT JOIN pms_product_category c2 ON c1.id = c2.parent_id
WHERE c1.parent_id = 0
```

### 5.6 商品编辑信息 — 多表联查

```sql
-- PmsProductMapperCustom.xml
SELECT *,
    pc.parent_id cateParentId,
    l.id ladder_id, l.discount ladder_discount, l.count ladder_count,
    pf.id full_id, pf.full_price full_full_price,
    m.id member_id, m.member_price member_member_price,
    s.id sku_id, s.price sku_price, s.sku_code sku_sku_code, s.stock sku_stock,
    a.id attribute_id, a.value attribute_value
FROM pms_product p
LEFT JOIN pms_product_category pc ON pc.id = p.product_category_id
LEFT JOIN pms_product_ladder l ON p.id = l.product_id
LEFT JOIN pms_product_full_reduction pf ON pf.product_id = p.id
LEFT JOIN pms_member_price m ON m.product_id = p.id
LEFT JOIN pms_sku_stock s ON s.product_id = p.id
LEFT JOIN pms_product_attribute_value a ON a.product_id = p.id
WHERE p.id = #{id}
```

**涉及 7 张表**：`pms_product` + `pms_product_category` + `pms_product_ladder` + `pms_product_full_reduction` + `pms_member_price` + `pms_sku_stock` + `pms_product_attribute_value`

### 5.7 MinIO 文件上传

```java
@PostMapping("/upload")
public CommonResult<MinioUploadDto> upload(@RequestParam("file") MultipartFile file) {
    // 1. 检查 bucket 是否存在，不存在则创建
    boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    if (!found) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        // 设置公开读策略
        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
    }
    // 2. 按日期+UUID 生成文件名，上传文件
    String filename = dateDir + "/" + UUID.randomUUID() + "_" + originalFilename;
    minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(filename).stream(...).build());
    // 3. 返回访问 URL
    return CommonResult.success(new MinioUploadDto(filename, url));
}
```

---

## 六、数据库核心表

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `pms_product` | 商品主表 | name, price, stock, sale, publishStatus, verifyStatus, brandName, detailHtml |
| `pms_brand` | 品牌表 | name, logo, showStatus, factoryStatus |
| `pms_product_category` | 商品分类 | parentId, level, name, navStatus |
| `pms_product_attribute` | 商品属性 | type(0规格/1参数), selectType, inputType |
| `pms_product_attribute_category` | 属性分类 | name, attributeCount, paramCount |
| `pms_product_attribute_value` | 商品属性值 | productId, productAttributeId, value |
| `pms_sku_stock` | SKU 库存 | skuCode, price, stock, lockStock, spData(JSON) |
| `pms_product_ladder` | 阶梯价格 | count, discount, price |
| `pms_product_full_reduction` | 满减价格 | fullPrice, reducePrice |
| `pms_member_price` | 会员价格 | memberLevelId, memberPrice |
| `pms_product_vertify_record` | 审核记录 | vertifyMan, status, detail |

---

## 七、核心设计亮点总结

1. **多维度定价体系**：商品支持原价、促销价、会员价、阶梯价、满减价五种定价方式
2. **反射关联插入**：`relateAndInsertList` 通过反射统一处理关联数据，避免重复代码
3. **先删后增策略**：更新关联数据时先删后增，简化了差异比对逻辑
4. **SKU 智能编码**：日期+商品ID+序号的编码格式，可读性好且保证唯一
5. **级联更新**：品牌/分类名称变更时自动同步关联商品
6. **双文件存储**：MinIO（自建）+ OSS（阿里云），灵活切换
7. **审核流程**：`verifyStatus` 字段控制商品审核状态，审核后记录到 `pms_product_vertify_record`

---

---

# 第二部分：Java 面试商品服务高频问题

## 面试题 1：商品的多维度价格体系是如何设计的？

```
商品价格优先级（从高到低）：
1. 会员价（memberPrice）      → 特定会员等级专属价
2. 促销价（promotionPrice）    → 限时活动价
3. 阶梯价（productLadder）     → 买 N 件打 X 折
4. 满减价（fullReduction）     → 满 X 元减 Y 元
5. 原价（price）              → 基础价格
```

**面试回答建议**：
> 商品价格体系设计了五层：原价是基础，促销价是限时低价，会员价面向特定等级，阶梯价根据购买数量打折，满减则是达到金额条件后减免。下单时，系统按优先级逐层计算最终价格：先判断是否为会员，再检查是否在促销期，然后匹配阶梯价和满减规则。这个设计参考了主流电商的价格策略。

---

## 面试题 2：SKU 编码的生成规则是什么？为什么这么设计？

```
编码格式: yyyyMMdd + productId(4位) + index(3位)
示例:      20260708   0001            001
           ↑日期      ↑商品ID          ↑该商品第1个SKU

特点：
- 日期前缀：直观看出创建时间
- 商品ID固定宽度：天然按商品聚合
- 序号自增：同一商品不同规格自然区分
- 总长固定：便于数据库索引和前端展示
```

---

## 面试题 3：商品更新为什么用"先删后增"策略？

**问题**：一次更新可能同时修改了会员价、阶梯价、满减、SKU 等多个关联表。

**先删后增 vs 差异比对**：

| 维度 | 先删后增 | 差异比对 |
|------|---------|---------|
| 实现难度 | 简单 | 复杂 |
| SQL 次数 | 固定 | 不确定 |
| 数据一致性 | 事务保证 | 事务保证 |
| ID 变化 | SKU 的 ID 可能变化 | ID 保持不变 |

选择"先删后增"是因为：代码简洁、逻辑清晰、事务保证一致性。对于 SKU，额外做了 ID 比对来区分新增和修改。

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| 五层定价 | 会员价 > 促销价 > 阶梯价 > 满减 > 原价 |
| SKU 编码 | 日期+商品ID+序号，可读且唯一 |
| 先删后增 | 简化更新逻辑，事务保证一致性 |
| 级联更新 | 品牌/分类改名时同步更新商品冗余字段 |
| MinIO | S3 兼容对象存储，自动建 bucket + 公开读策略 |
| 审核流程 | verifyStatus 控制，审核记录独立存储 |

---

*学习日期：2026-07-08*
