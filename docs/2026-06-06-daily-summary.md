# 2026-06-06 全天工作记录 - Apache Camel 数据源集成 & 运行时修复

> 日期：2026-06-06
> 状态：实施完成，运行时验证遇阻（明天继续）

## 一、总体目标

按 10 步 TDD 流程一次性完成 Apache Camel 数据源集成方案：
- 领域/应用/API 层引入 `AgentDataSource` 通用数据源模型
- 基础设施层通过 Camel 接入 12 种 Component
- 第 5 个 PermissionStrategy（CRUD 细粒度）
- 全局 `audit_log` 覆盖 Agent 全生命周期
- 前端管理界面（数据源 + Schema + 权限 + 审计）

## 二、完成的 5 个工作块

| Block | 内容 | 关键产出 |
|-------|------|---------|
| 1 | 领域 + DB + Application 端口 | 12 domain + 12 application + 5 DB + 4 SQL（V100-V103） |
| 2 | Camel 引擎 + UseCase + 权限服务 | 5 camel + 5 UseCase + PermissionService + RateLimiter + AuditRecorder |
| 3 | API 层 + 集成测试 | 5 DTO + 5 Controller + 3 IntegrationTest |
| 4 | 双路径集成（ToolCallback + McpServer） | 3 tools/data_source + 3 web + 2 Listener |
| 5 | 前端 + ArchUnit | 2 View + 5 Component + 5 API 客户端 + 3 types + ArchUnit 19/19 |

## 三、关键架构决策

| 维度 | 决策 |
|------|------|
| 领域/应用/API 命名 | 统一 `AgentDataSource`（不含 Camel 字样） |
| Camel 代码位置 | **仅 `infrastructure.camel.*`** |
| 同进程 Agent 集成 | `ToolCallback` 直接调用 |
| 跨进程 MCP 实现 | MCP Java SDK 内置 `McpServer`（`io.modelcontextprotocol.sdk:mcp:1.1.0`） |
| 数据源 ToolCallback 归属 | `infrastructure.tools.data_source` 包（不引用 Camel） |
| 权限策略 | **CRUD 细粒度**（CREATE/READ/UPDATE/DELETE，无 ADMIN） |
| 审计日志 | **全局** `audit_log` 表，租户级查询 |
| Camel 引擎 | DefaultCamelContext 每 workspace + direct: 虚拟路由 |
| @Audited + AOP | 零侵入记录，AuditRecorder：BlockingQueue(10000) + 1s flush + batch 500 |

## 四、测试结果（修复前）

```
ArchUnit:                       19/19 ✅
AgentDataSource:                10/10 ✅
AgentDataSourceComponent:        2/2  ✅
DataSourceSchema:                6/6  ✅
PermissionStrategy:              4/4  ✅
AuditLog:                        4/4  ✅
─────────────────────────────────
Total:                          45/45 ✅
```

## 五、运行时调试过程（傍晚 ~ 22:00 起）

### 5.1 错误 #1：NoSuchBeanException - DataSource 未注册到 Camel registry

**现象**：
```
ResolveEndpointFailedException: No bean could be found in the registry for:
postgresql://localhost:5432/test of type: javax.sql.DataSource
```

**根因**：Camel `JdbcComponent.createEndpoint` 从 `jdbc:` 后的 URI 部分查找名为 DataSource 的 bean，但未注册。

**修复**：
- 新建 `JdbcDataSourceFactory.java`（HikariDataSource 实现）
- `CamelAgentDataSourceAdapter.registerDataSourceIfNeeded` 注入 factory，`ctx.getRegistry().bind(lookupKey, ds)`
- 关闭时 `closeDataSource` 调 `AutoCloseable.close()`

### 5.2 错误 #2：DirectConsumerNotAvailableException - 路由未注册

**现象**：
```
DirectConsumerNotAvailableException: No consumers available on endpoint:
direct://agent-data-source-{id}
```

**根因**：`bootstrap()` 只在显式调用时注册路由，但 `/test` 端点直接调用 `adapter.test()` 时路由未注册。

**修复**：
- 新增 `ensureBootstrapped(source)` 懒加载方法
- `test()` / `invoke()` 入口先调 `ensureBootstrapped()`

### 5.3 错误 #3：jdbcLookupKey 与 `?` 查询参数不匹配

**现象**：
```
ResolveEndpointFailedException: No bean could be found in the registry for:
postgresql://localhost:5432/agenthub of type: javax.sql.DataSource
（注意：原 lookup key 含 `?schema=app`）
```

**根因**：`JdbcComponent.createEndpoint` 解析 URI 时把 `?` 后部分当作查询参数剥离，只用 `?` 前查找 DataSource。Lookup key 含 `?schema=app` 不匹配。

**修复**：`jdbcLookupKey()` 去除 `?` 后部分：
```java
int q = tail.indexOf('?');
return q < 0 ? tail : tail.substring(0, q);
```

### 5.4 错误 #4：toD(endpointUri) 误把 body 当 URI

**现象**：
```
PSQLException: syntax error at or near "ping"  位置：1
```

**根因**：路由 `fromF("direct:%s").toD(endpointUri)`，Camel 把 body "ping" 当作 URI 一部分，组成 `jdbc:ping` 然后 JDBC component 把它当 SQL 执行。

**修复**：
1. `ping()` 改用 `DataSource.isValid(5)` 直接测试，绕过 Camel 路由
2. 路由 `toD` → `to(endpointUri)`，body 变 SQL

### 5.5 错误 #5：路由 `to()` 仍不识别 `?schema=app` 参数

**现象**：
```
FailedToCreateRouteException: Unknown parameters=[{schema=app}]
```

**根因**：`jdbc:...?schema=app` 中 `?schema=app` 是给 PostgreSQL JDBC driver 的，**不是** Camel JDBC Component 的端点参数。路由解析时报 unknown parameter。

**修复**：路由 endpoint URI 也需 strip 查询字符串。新增 `stripQuery()`：
```java
private RouteBuilder buildRoute(String routeId, String endpointUri) {
    String cleanUri = stripQuery(endpointUri);
    return new RouteBuilder() {
        @Override
        public void configure() {
            fromF("direct:%s", routeId).routeId(routeId).to(cleanUri);
        }
    };
}
```

## 六、新增集成测试

`CamelDataSourcePgIntegrationTest.java`（**新建**）：
- 用 application.yml 的 PostgreSQL 数据源（`jdbc:postgresql://localhost:5432/agenthub?schema=app`）
- @BeforeAll 自管 schema 迁移
- 2 个测试：
  - `shouldCreatePgDataSource`：用真实 PG URL + 凭据创建 AgentDataSource
  - `shouldTestConnectionToRealPg`：调用 `POST /test` 端点验证 `success=true`

## 七、ArchUnit 违规修复（≤10 行）

修复 6 个方法超 10 行：

1. `CamelAgentDataSourceAdapter.invoke()` 11 行 → 拆 `doInvoke()`
2. `JdbcDataSourceFactory.create()` 11 行 → 拆 `baseConfig()`
3. `JdbcDataSourceFactory.applyProperty()` 13 行 → 拆 `dispatchProperty()` + `applyNumericOrPassThrough()`
4. `JdbcDataSourceFactory.applyProperties()` 13 行 → 用 `map.forEach` 简化
5. `TenantContextObjectHandler.fillTenantId()` 11 行 → 拆 `shouldFillTenantId()` + `applyTenantIdIfValid()`
6. `TenantContextObjectHandler.fillWorkspaceId()` 11 行 → 拆 `shouldFillWorkspaceId()` + `applyWorkspaceIdIfValid()`

## 八、测试结果（修复后）

```
ArchUnit:                       19/19 ✅
AgentDataSource:                10/10 ✅
AgentDataSourceComponent:        2/2  ✅
DataSourceSchema:                6/6  ✅
PermissionStrategy:              4/4  ✅
AuditLog:                        4/4  ✅
CamelDataSourcePg (新):          2/2  ✅
─────────────────────────────────
Total:                          47/47 ✅
```

## 九、文件变更清单

### 新建（约 80 个）

**Java（后端 ~ 56 个）**：
- domain: `AgentDataSource.java`, `AgentDataSourceProtocol.java`（enum）, `AgentDataSourceStatus.java`, `AgentDataSourceDescriptor.java`, `AgentDataSourceField.java`, `AgentDataSourceTestResult.java`, `AgentDataSourceInvokeResult.java`, `DataSourceSchema.java`, `DataSourceTable.java`, `DataSourceColumn.java`, `TableRelationship.java`, `PermissionStrategy.java`, `PermissionStrategyType.java`, `OperationLevel.java`, `AuditEvent.java`, `AuditLog.java`, `AuditAction.java`, `AuditStatus.java`, `ProtocolBlacklist.java`, `RateLimitWindow.java`, `DataSourcePermissionException.java`, `DataSourceRateLimitException.java`
- application: 12 个 command/dto/port/usecase（含 `AgentDataSourceUseCase`, `DataSourceSchemaUseCase`, `DataSourcePermissionService`, `InMemoryRateLimiter`, `@Audited` 注解, `RateLimitCheckCommand`, `QueryAuditLogCommand`, `DataSourceInvokeContext`, `WindowSpec`）
- infrastructure: 6 entity + 4 mapper + 4 repository + `InMemoryRateLimiter.java` + `AuditRecorder.java` + `TenantContextPortAdapter.java` + `CamelAgentDataSourceAdapter.java` + `CamelAgentDataSourcePortAdapter.java` + `CamelDataSourceRuntime.java` + `CamelComponentIntrospector.java` + `AuditAspect.java` + `JdbcDataSourceFactory.java`
- api: 5 Controller + 5 DTO + 1 mapper + 1 `ApiExceptionHandler` 增强

**TypeScript / Vue（前端 ~ 19 个）**：
- types: `agent-data-source.ts`, `permission-strategy.ts`, `audit-log.ts`
- api: 5 个 API 客户端
- components: `SchemaPanel.vue`, `PermissionStrategyPanel.vue`
- views: `AgentDataSourceView.vue`, `AuditLogView.vue`
- 修改：`SettingsView.vue`（+2 入口卡片）、`router/index.ts`（+2 路由）、`AgentHubLayout.vue`

**SQL（4 个迁移文件）**：
- `V100__add_agent_data_source_tables.sql`
- `V101__add_data_source_schema_tables.sql`
- `V102__add_permission_strategy_tables.sql`
- `V103__add_global_audit_log_table.sql`

**测试（7 个集成测试 + 1 单元测试）**：
- `AgentDataSourceControllerIntegrationTest`（10 测试）
- `AgentDataSourceComponentControllerIntegrationTest`（2 测试）
- `DataSourceSchemaControllerIntegrationTest`（6 测试）
- `PermissionStrategyControllerIntegrationTest`（4 测试）
- `AuditLogControllerIntegrationTest`（4 测试）
- `CamelDataSourcePgIntegrationTest`（2 测试，新建）
- `AgentHubCleanArchitectureTest`（19 规则，已含 Camel 引用约束）

**文档（2 个）**：
- `docs/2026-06-06-apache-camel-integration-plan.md`（1986 行主方案）
- `docs/2026-06-06-block5-frontend-plan.md`（前端实现方案）

### 修改（~10 个）

- `build.gradle`：camel 4.14.0（12 component，不含 kafka/ftp/sftp）+ hutool-json + spring-boot-starter-jdbc（传 HikariCP）+ postgresql driver
- `application.yml`：datasource 配置（已有）
- `src/main/java/com/agenthub/api/exception/ApiExceptionHandler.java`：+3 handler
- `src/main/java/com/agenthub/infrastructure/context/handler/TenantContextObjectHandler.java`：方法拆分（修 ArchUnit）
- `src/main/java/com/agenthub/infrastructure/store/db/entity/AuditLogEntity.java`：`workspaceId` 加 `@TableField(exist = false)`
- `src/main/java/com/agenthub/infrastructure/context/handler/BaseContextLineHandler.java`：异常返回 `true`
- `src/main/java/com/agenthub/infrastructure/context/handler/TenantContextObjectHandler.java`：改用 `findTenantThreadContext`
- `src/main/web/src/components/AgentHubLayout.vue`：showAddButton 加 2 路径
- `src/main/web/src/router/index.ts`：加 2 路由
- `src/main/web/src/views/agenthub/SettingsView.vue`：加 2 入口卡片

## 十、关键技术要点（明天可参考）

### 10.1 Camel JDBC + DataSource 动态注册

```java
// 1. lookupKey 必须 strip ? 查询参数
private String jdbcLookupKey(String endpointUri) {
    int idx = endpointUri.indexOf(':');
    String tail = idx < 0 ? endpointUri : endpointUri.substring(idx + 1);
    int q = tail.indexOf('?');
    return q < 0 ? tail : tail.substring(0, q);
}

// 2. 路由 endpoint URI 也要 strip
private RouteBuilder buildRoute(String routeId, String endpointUri) {
    String cleanUri = stripQuery(endpointUri);  // 关键
    return new RouteBuilder() {
        @Override
        public void configure() {
            fromF("direct:%s", routeId).routeId(routeId).to(cleanUri);  // 关键: to 不是 toD
        }
    };
}
```

### 10.2 ping/test 走 DataSource 直接测试，不走 Camel 路由

```java
private String ping(AgentDataSource source) {
    ensureBootstrapped(source);
    DataSource ds = registeredDataSources.get(source.getId());
    if (ds == null) return null;
    try (Connection c = ds.getConnection()) {
        return c.isValid(5) ? "ok" : null;
    } catch (Exception e) {
        throw new RuntimeException("ping failed: " + e.getMessage(), e);
    }
}
```

### 10.3 懒加载 bootstrap

```java
public boolean test(AgentDataSource source) {
    try {
        ensureBootstrapped(source);  // 第一次调用时自动注册路由 + DataSource
        return ping(source) != null;
    } catch (Exception e) {
        log.warn("test connection failed for {}", source.getId(), e);
        return false;
    }
}

private void ensureBootstrapped(AgentDataSource source) {
    if (Boolean.TRUE.equals(registeredRoutes.get(source.getId()))) return;
    bootstrap(source);
}
```

## 十一、明天需要继续的 TODO

1. **用户验证**：明天用户重启应用，测试 `POST /test` 端点，确认 `success=true`。
2. **invoke 端点验证**：测试 `POST /agent-data-sources/{id}/invoke`，验证 SQL body 能正常执行。
3. **Schema 自动发现**：调用 `POST /agent-data-sources/{id}/introspect` 验证是否真的能自动发现 PostgreSQL 表结构（当前实现默认返回空 schema）。
4. **MCP 跨进程路径**：Block 4 集成 McpServer，需验证 SSE 端点可访问。
5. **Audit 日志持久化**：调用 `/test` 后查询 `/audit-logs` 是否产生记录。
6. **PermissionStrategy CRUD 验证**：前端 CRUD 完整流程，CRUD 操作拦截是否正确返回 403。
7. **写代码自检**：今天有些 helper 方法用了 `@Override`（来自 RouteBuilder 内部类）和 lambda 链式调用，未触发 ArchUnit 违规（感谢）但应保持警惕。

## 十二、反思

### 12.1 经验

1. **Camel endpoint 解析是双层的**：
   - `?` 前：用于查找 DataSource / 端点标识
   - `?` 后：用于 Camel 端点参数配置（不是 JDBC 驱动参数！）
   - 教训：先读 Camel 4.14 源码确认 lookup 逻辑
2. **`toD` vs `to` 的选择**：
   - `toD` 把 body 当 URI 一部分处理（动态路由）
   - `to` 静态 URI，body 是消息内容
   - 教训：JDBC body 是 SQL 字符串，必须用 `to`
3. **测试真实性**：原 `AgentDataSourceControllerIntegrationTest` 用 `jdbc:postgresql://localhost:5432/test`（不存在的库），导致测试都通过但真实环境失败。新增 `CamelDataSourcePgIntegrationTest` 用真实 PG 端到端验证。
4. **方法 ≤10 行 规则的连锁影响**：每个 helper 拆完后 ArchUnit 通过，但代码可读性确实下降（`create` 拆 `baseConfig` + `poolName` + 3 个 switch helper），需要在简洁和合规间权衡。

### 12.2 待改进

- 集成测试用 stub URL（`/test`）通过，但生产用真实 URL 失败 → 测试原则：集成测试**必须**用真实可用的依赖（PG / Redis / Kafka）
- 当 `camel-jdbc` 等 Component 升级时需重新验证 `?` 解析逻辑
- ArchUnit 应加一条新规则：`infrastructure.camel.*` 之外**禁止**在路由中使用 `toD(string)` 调用 JDBC endpoint

### 12.3 文档状态

- 主方案 `docs/2026-06-06-apache-camel-integration-plan.md` 已有「完成情况」+「反思」章节
- 本文档 `docs/2026-06-06-daily-summary.md`（新建）记录全部 Block 完成情况 + 5 个运行时调试错误 + 明天 TODO
- 方案文档 Block 5 部分（前端）独立在 `docs/2026-06-06-block5-frontend-plan.md`
