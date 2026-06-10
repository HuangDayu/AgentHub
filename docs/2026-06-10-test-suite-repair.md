# 集成测试全量修复总结
> 日期：2026-06-10 · 状态：已完成

## 1. 概述

全量集成测试从 **57 个失败** 修复至 **0 个失败**，44 个测试类共 361 个测试全部通过。

## 2. 修复项

### 2.1 JWT 令牌引导问题（核心根因）

JUnit 6.0 移除了 `ClassOrderer.OrderAnnotation`，导致 `@Order` 类排序完全失效。
`AuthControllerIntegrationTest`（负责写入 `token.txt`）无法保证在其他测试之前执行，
造成其他 32 个测试类读取到空/过期令牌，所有请求返回 401，继而 CREATE 正常但后续 GET/UPDATE/DELETE 返回 404。

**修复方案**：在 `TestAgentHubApplication.main()` 中启动时直接生成 JWT 令牌写入 `token.txt`，
不再依赖任何测试类的执行顺序。令牌在 Spring 上下文初始化后、任何测试运行前即就绪。

### 2.2 类排序配置清理

- 移除 `junit-platform.properties` 中的 `junit.jupiter.testclass.order.default`（JUnit 6 不支持）
- 移除 `AuthControllerIntegrationTest` 上的 `@Order(1)`（无效果且误导）

### 2.3 数据残留导致 409 Conflict

`AgentDataSourceControllerIntegrationTest` 和 `DataSourceSchemaControllerIntegrationTest` 的 CREATE 操作
因前序运行遗留的测试数据（同名实体）返回 409，导致 `createdId` 为 null，后续 14 个测试全部 cascading failure。

**修复方案**：创建资源名称使用 `System.currentTimeMillis()` 后缀确保唯一性。

## 3. 架构变更

| 文件 | 变更 | 原因 |
|------|------|------|
| `TestAgentHubApplication.java` | 新增 `writeToken()` | 启动时预生成 JWT 令牌 |
| `junit-platform.properties` | 清空 | JUnit 6 不支持 `ClassOrderer` |
| `AuthControllerIntegrationTest.java` | 移除 `@Order(1)` | 类排序不生效 |
| `AgentDataSourceControllerIntegrationTest.java` | 唯一名称 | 避免 409 冲突 |
| `DataSourceSchemaControllerIntegrationTest.java` | 唯一名称 | 避免 409 冲突 |

## 4. 测试结果

- 架构测试（ArchUnit）：1 类 / 19 规则 ✅
- 集成测试：40 类 / 327 测试 ✅
- 单元测试：3 类 / 34 测试 ✅
- **总计：44 类 / 361 测试 / 0 失败**

## 5. 反思

- **JUnit 6 兼容性**：`ClassOrderer.OrderAnnotation` 在 JUnit 6.0 中已被移除，需要关注升级后测试基础设施的变化。
- **令牌引导**：测试框架不应依赖特定测试类的执行顺序来初始化共享资源，应该在 Spring 上下文层面完成引导。
- **数据隔离**：集成测试需要处理前序运行的数据残留，唯一名称或 `@Sql` 清理是必要的防御措施。
