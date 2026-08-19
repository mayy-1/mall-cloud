## 多接口数据并行汇总（线程池 + CompletableFuture）

### 场景

`buyNowConfirm` 是立即购买确认单接口，需要调用 4 个互不依赖的 Feign 接口获取数据：

| 序号 | 接口 | 依赖关系 |
|------|------|----------|
| ① | `memberClient.getCurrentMember()` | 获取当前用户，后续积分取值依赖 |
| ② | `memberAddressClient.list()` | 无依赖 |
| ③ | `marketingCouponClient.listCart()` | 无依赖（只需 cartList） |
| ④ | `memberClient.getIntegrationConsumeSetting()` | 无依赖 |

②③④ 互不依赖，改前串行调用耗时 ≈ T② + T③ + T④。

### 线程池配置

```java
@Bean("orderConfirmExecutor")
public Executor orderConfirmExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);          // 确认订单属低频操作
    executor.setMaxPoolSize(4);           // 峰值扩容
    executor.setKeepAliveSeconds(30);     // 空闲线程回收
    executor.setQueueCapacity(20);        // 阻塞队列防 OOM
    executor.setRejectedExecutionHandler(
        new ThreadPoolExecutor.CallerRunsPolicy()); // 过载兜底
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.initialize();
    return executor;
}
```

### 并行实现

```java
// 3 个互不依赖的 Feign 调用同时提交到线程池
CompletableFuture<List<MemberAddressDTO>> addressFuture =
        CompletableFuture.supplyAsync(
            () -> memberAddressClient.list().getData(), orderConfirmExecutor);
CompletableFuture<List<CouponHistoryDetailDTO>> couponFuture =
        CompletableFuture.supplyAsync(
            () -> marketingCouponClient.listCart(cartList, 1).getData(), orderConfirmExecutor);
CompletableFuture<IntegrationConsumeSettingDTO> integFuture =
        CompletableFuture.supplyAsync(
            () -> memberClient.getIntegrationConsumeSetting().getData(), orderConfirmExecutor);

// getCurrentMember 留在主线程（同步），其返回的积分可直接取值无需等待远端
result.setMemberIntegration(currentMember.getIntegration());

// 等待 3 个并行任务全部完成并汇总
CompletableFuture.allOf(addressFuture, couponFuture, integFuture).join();
result.setMemberReceiveAddressList(addressFuture.join());
result.setCouponHistoryDetailList(couponFuture.join());
result.setIntegrationConsumeSetting(integFuture.join());
```

### 效果

```
串行调用：memberClient → addressClient → couponClient → settingClient
         耗时 = T1 + T2 + T3           ≈ 150ms

并行调用：memberClient（主线程）
          addressClient ──┐
          couponClient  ──┤ 线程池并行
          settingClient ──┘
         耗时 = T1 + max(T2, T3)      ≈ 80ms

节省 ≈ 70ms，约 47% 响应时间
```

### 关键决策

| 决策点 | 选择 | 原因 |
|--------|------|------|
| 线程池而非 `new Thread()` | Spring `ThreadPoolTaskExecutor` | 线程复用、监控、优雅关闭 |
| `CallerRunsPolicy` 拒绝策略 | 调用线程兜底 | 宁可变串行也不丢请求 |
| `CompletableFuture` 而非 `Future` | `allOf().join()` 等待全部 | 链式组合更简洁 |
| ①仍串行 | member 查询放主线程 | 后续步骤不需要等待，且避免 Sa-Token 上下文跨线程 |
