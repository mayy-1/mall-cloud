# mall-cloud 用户管理服务学习笔记

---

# 第一部分：user-service 模块完整梳理

## 一、模块定位

`user-service` 是 mall-cloud 微服务电商平台的 **后台管理员用户服务**，负责管理员账户的 CRUD、RBAC（角色-权限）管理、资源权限控制和后台登录认证。它实现了完整的 RBAC 权限模型：**管理员 → 角色 → 菜单/资源** 的三层权限体系。

---

## 二、技术架构全景

```
           mall-gateway (:/mall-admin/**)
                    │
                    v
            mall-auth (认证路由)
                    │
                    ▼
    ┌─────────────────────────────────────┐
    │         user-service :8081          │
    │         (后台管理员服务)              │
    │                                     │
    │  ┌─────────────────────────────┐    │
    │  │  AdminController            │    │  ← 管理员 CRUD + 登录
    │  │  POST /admin/login          │    │
    │  │  GET  /admin/list           │    │
    │  └─────────────────────────────┘    │
    │  ┌─────────────────────────────┐    │
    │  │  RoleController             │    │  ← 角色管理
    │  │  MenuController             │    │  ← 菜单管理（树形结构）
    │  │  ResourceController         │    │  ← 资源权限管理 ★
    │  │  ResourceCategoryController │    │  ← 资源分类管理
    │  └─────────────────────────────┘    │
    │                                     │
    │  数据库: mall_user (MySQL)          │
    │  缓存: Redis (Session/Token)        │
    │  认证: Sa-Token (默认模式)           │
    └─────────────────────────────────────┘
```

---

## 三、目录结构

```
user-service/
├── pom.xml
└── src/main/
    ├── java/com/mym/mall/admin/       (注意：包名是 admin)
    │   ├── MallAdminApplication.java  # 启动类
    │   ├── config/
    │   │   ├── MyBatisConfig.java     # MyBatis 配置
    │   │   └── SaTokenConfig.java     # Sa-Token 认证配置 ★
    │   ├── controller/
    │   │   ├── AdminController.java          # 管理员管理
    │   │   ├── RoleController.java           # 角色管理
    │   │   ├── MenuController.java           # 菜单管理
    │   │   ├── ResourceController.java       # 资源管理 ★
    │   │   ├── ResourceCategoryController.java # 资源分类管理
    │   │   └── MemberLevelController.java    # 会员等级管理
    │   ├── domain/dto/
    │   │   ├── AdminUserDetails.java         # Spring Security 用户详情
    │   │   ├── AdminLoginParam.java          # 登录参数
    │   │   └── UmsAdminQueryParam.java       # 管理员查询参数
    │   ├── mapper/ (14个 Mapper 接口)
    │   ├── model/ (11个实体类)
    │   └── service/
    │       ├── AdminService / impl
    │       ├── RoleService / impl
    │       ├── MenuService / impl
    │       ├── ResourceService / impl
    │       └── ...
    └── resources/
        ├── application.yml
        ├── application-dev.yml
        ├── application-prod.yml
        └── mapper/ (11个 MyBatis XML 文件)
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `mall-common` | 公共模块（显式排除 `spring-boot-starter-web`） |
| `mall-api` | Feign 接口模块 |
| `spring-boot-starter-web` | Web 服务 |
| `mybatis-spring-boot-starter` | MyBatis ORM 框架 |
| `druid-spring-boot-3-starter` | Druid 连接池 |
| `pagehelper-spring-boot-starter` | PageHelper 分页插件 |
| `spring-boot-starter-data-redis` | Redis（Sa-Token Session 存储） |
| `sa-token-spring-boot3-starter` | Sa-Token 认证框架 |
| `sa-token-redis-jackson` | Sa-Token Redis 存储集成 |
| `spring-cloud-starter-alibaba-nacos-discovery/config` | Nacos 服务发现/配置中心 |
| `knife4j-openapi3-jakarta-spring-boot-starter` | API 文档 |
| `spring-boot-admin-starter-client` | 监控客户端 |

---

## 五、核心功能详解

### 5.1 RBAC 权限模型

```
UmsAdmin (管理员)
    │
    ├── UmsAdminRoleRelation (管理员-角色关联)
    │       │
    │       ▼
    │   UmsRole (角色)
    │       │
    │       ├── UmsRoleMenuRelation (角色-菜单关联)
    │       │       │
    │       │       ▼
    │       │   UmsMenu (菜单)
    │       │
    │       └── UmsRoleResourceRelation (角色-资源关联)
    │               │
    │               ▼
    │           UmsResource (资源 = 后端接口权限)
    │               │
    │               ▼
    │           UmsResourceCategory (资源分类)
```

### 5.2 AdminController — 管理员管理

| API | 方法 | 路径 | 说明 |
|-----|------|------|------|
| login | POST | /admin/login | 管理员登录，返回 Token |
| register | POST | /admin/register | 管理员注册 |
| getItem | GET | /admin/{id} | 获取管理员详情 |
| list | GET | /admin/list | 分页查询管理员列表 |
| update | POST | /admin/update/{id} | 修改管理员信息 |
| delete | POST | /admin/delete/{id} | 删除管理员 |
| updateStatus | POST | /admin/updateStatus/{id} | 修改账号启用状态 |
| updateRole | POST | /admin/role/update | 给管理员分配角色 |
| getRoleList | GET | /admin/role/{adminId} | 获取管理员的角色列表 |

**登录流程**：
```java
// AdminServiceImpl.login()
String token = StpUtil.login(admin.getId());  // Sa-Token 生成 Token
// 加载用户权限列表
List<String> permissionList = resourceService.getResourceList(admin.getId());
// 存储到 Sa-Token Session
StpUtil.getSession().set(AuthConstant.STP_ADMIN_INFO, userDto);
```

### 5.3 ResourceController — 资源权限管理 ★

资源（Resource）代表后端接口的权限点，是整个 RBAC 体系中最细粒度的控制单元：

| API | 方法 | 路径 | 说明 |
|-----|------|------|------|
| create | POST | /resource/create | 创建资源 |
| update | POST | /resource/update/{id} | 修改资源 |
| delete | POST | /resource/delete/{id} | 删除资源 |
| getItem | GET | /resource/{id} | 获取资源详情 |
| listAll | GET | /resource/listAll | 查询所有资源 |
| list | GET | /resource/list | 分页模糊查询 |

**动态权限刷新**：当管理员新增/删除/修改资源时，需要将最新的"路径→权限"映射同步到 Redis：
```java
// 写入 Redis Hash
redisService.hSet(AuthConstant.PATH_RESOURCE_MAP, resource.getUrl(), resource.getId().toString());
```

这样网关层面的 `SaReactorFilter` 就能从 Redis 读取最新的权限映射进行动态鉴权。

### 5.4 MenuController — 菜单管理

菜单是树形结构（`parentId` 表示父子关系），支持无限层级：

```
菜单结构
├── 权限管理 (一级菜单)
│   ├── 用户列表 (二级菜单)
│   ├── 角色列表
│   └── 菜单列表
├── 商品管理
│   ├── 商品列表
│   ├── 商品分类
│   └── 商品品牌
└── ...
```

**核心方法**：`treeList()` — 查询所有菜单并构建树形结构返回前端。

### 5.5 SaTokenConfig — Sa-Token 认证配置

```java
@Configuration
public class SaTokenConfig {
    @Bean
    public StpInterface stpInterface() {
        return new StpInterface() {
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                // 从数据库查询该管理员的权限列表
                return resourceService.getResourceList(Long.valueOf(loginId.toString()));
            }

            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                // 从数据库查询该管理员的角色列表
                return roleService.getRoleList(Long.valueOf(loginId.toString()));
            }
        };
    }
}
```

**关键设计**：`StpInterface` 是 Sa-Token 的权限加载接口。当网关调用 `StpUtil.checkPermissionOr()` 时，Sa-Token 会调用此接口获取当前用户的权限列表，然后与所需权限做比对。**权限数据来自数据库，不是硬编码在代码里的。**

---

## 六、数据库表结构

**核心表** (数据库 `mall_user`)：
- `ums_admin` — 管理员表（用户名、密码、头像、状态等）
- `ums_admin_role_relation` — 管理员-角色关联表
- `ums_role` — 角色表
- `ums_role_menu_relation` — 角色-菜单关联表
- `ums_role_resource_relation` — 角色-资源关联表
- `ums_menu` — 菜单表（含 parentId 形成树形结构）
- `ums_resource` — 资源表（url/描述/分类ID）
- `ums_resource_category` — 资源分类表
- `ums_admin_login_log` — 管理员登录日志
- `ums_member_level` — 会员等级表

---

## 七、权限校验全流程

```
1. 管理员登录
   POST /mall-admin/admin/login
   → user-service AdminController.login()
   → Sa-Token 生成 Token (存入 Redis)
   → 返回 Token 给客户端

2. 客户端携带 Token 访问业务接口
   GET /mall-admin/product/list
   Authorization: Bearer xxxxx

3. 网关 SaReactorFilter 鉴权
   ├─ 匹配 /mall-admin/**
   ├─ StpUtil.checkLogin() → 从 Redis 验证 Token
   ├─ 从 Redis 读取 PATH_RESOURCE_MAP (路径→权限映射)
   ├─ 匹配当前路径需要的权限
   └─ StpUtil.checkPermissionOr() → 从 StpInterface 加载用户权限列表做比对

4. 放行 → 转发到 product-service
```

---

## 八、核心设计亮点总结

1. **完整 RBAC 模型**：管理员 → 角色 → 菜单/资源，三级权限控制
2. **动态权限**：权限与接口的映射存在 Redis，网关动态读取，接口变更无需重启
3. **Sa-Token 集成**：使用 `StpInterface` 从数据库加载权限，与 Spring Security 解耦
4. **树形菜单**：`parentId` 自关联实现无限层级菜单结构
5. **登录日志**：`ums_admin_login_log` 记录每次登录的 IP、时间、结果
6. **Druid 连接池**：生产级数据库连接池，支持 SQL 监控

---

---

# 第二部分：Java 面试用户服务高频问题

## 面试题 1：RBAC 权限模型是什么？本项目如何实现的？

**RBAC = Role-Based Access Control（基于角色的访问控制）**

**三层结构**：
```
User(管理员) ──N:N── Role(角色) ──N:N── Permission(权限点)
```

本项目实现：
- `ums_admin` ↔ `ums_admin_role_relation` ↔ `ums_role`
- `ums_role` ↔ `ums_role_menu_relation` ↔ `ums_menu`（前端菜单可见性）
- `ums_role` ↔ `ums_role_resource_relation` ↔ `ums_resource`（后端接口鉴权）

**面试回答建议**：
> RBAC 的核心思想是"用户不直接绑定权限，而是通过角色间接获得权限"。本项目实现了三层控制：管理员绑定角色，角色绑定菜单（控制前端可见菜单）和资源（控制后端接口权限）。给管理员分配角色时，自动获得该角色下的所有菜单和接口权限。修改角色的权限点时，该角色下所有管理员同步生效。

---

## 面试题 2：Sa-Token 的 StpInterface 和 Spring Security 的 UserDetailsService 有什么异同？

| 维度 | StpInterface | UserDetailsService |
|------|-------------|-------------------|
| 框架 | Sa-Token | Spring Security |
| 方法数 | getPermissionList + getRoleList | loadUserByUsername |
| 调用时机 | 每次 checkPermission 时动态调用 | 每次认证时调用一次 |
| 返回值 | 权限/角色列表 | UserDetails 对象 |
| 缓存 | 可通过 Sa-Token Session 缓存 | 可通过 SecurityContext 缓存 |

**面试回答建议**：
> `StpInterface` 是 Sa-Token 的权限加载接口，每次执行权限校验时，框架会调用 `getPermissionList()` 获取当前用户的权限列表。这与 Spring Security 的 `UserDetailsService.getAuthorities()` 类似，但 StpInterface 更轻量，只需要返回字符串列表即可，不需要构建复杂的 GrantedAuthority 对象。

---

## 面试题 3：动态权限刷新是如何实现的？

```
管理员在后台新增/修改/删除资源
     │
     v
redisService.hSet("auth:pathResourceMap", resource.getUrl(), resource.getId())
     │
     v
Redis Hash "auth:pathResourceMap" 被更新
     │
     v
网关 SaReactorFilter 鉴权时从 Redis 实时读取
     │
     v
新的权限映射立即生效（无需重启网关）
```

**关键点**：权限映射是实时从 Redis 读取的，不是启动时一次性加载到内存的。所以数据变化后，网关下一次鉴权就会使用新规则。

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| RBAC 模型 | 用户→角色→权限，三层间接绑定 |
| StpInterface | Sa-Token 权限加载接口，返回权限/角色列表 |
| 动态权限 | 资源变更 → 更新 Redis → 网关实时读取 |
| 菜单树 | parentId 自关联，递归构建树形结构 |
| 登录日志 | 每次登录记录 IP/时间/结果到 ums_admin_login_log |
| Druid | 生产级连接池，支持 SQL 监控和防火墙 |

---

*学习日期：2026-07-08*
