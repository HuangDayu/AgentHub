# Agent 数据源集成 实现方案

> 日期：2026-06-06
> 状态：待审核（修订 3：浮动按钮前端规范 + McpServer 内置 + 工期压缩）

## 1. 需求概述

将 Apache Camel 集成进 AgentHub，作为 **AI Agent 访问多源异构数据的统一连接器层**。Camel 的 300+ Component（JMS / Kafka / JDBC / FTP / SFTP / 文件 / MongoDB / Redis / Mail / HTTP 等）作为**数据源/中间件连接器**，通过 **双路径** 暴露给 Agent：

1. **同进程快速路径**（主）：数据源 → Spring AI `ToolCallback` → 同进程 Agent 直接调用
2. **跨进程标准路径**：数据源 → **MCP Java SDK 内置 `McpServer`**（SSE + JSON-RPC）→ 外部 MCP 客户端

同时提供 **前后端管理界面 + 表级元数据 + 第 5 个权限策略**，支持：
- 数据源的**可视化配置、连接测试、启用/禁用、调用调试**
- 数据源**表结构管理**（表/字段/关联关系/可执行操作）
- 全面**权限控制**（ACL / CRUD 操作细粒度授权 / 协议黑名单 / 危险操作拦截 / 速率限制 / PII 脱敏）
- **全局审计日志**：覆盖 Agent 全生命周期（创建/发布/执行/调用/停止/删除等所有操作）

**关键架构原则**：

> **领域/应用/API 层只看到 `AgentDataSource`（数据源的通用概念），不感知 Camel 存在。**
> **Camel 限定在基础设施层 `infrastructure.camel.*`。**
> **同进程内 Agent 走 Spring AI `ToolCallback`，零协议开销。**
> **跨进程走 MCP Java SDK 内置 `McpServer`，协议标准化，零自定义 JSON-RPC。**

**关键决策（与用户确认）**：

| 维度 | 决策 |
|------|------|
| 领域/应用/API 命名 | 统一 `AgentDataSource`（不含 Camel 字样） |
| Camel 代码位置 | **仅 `infrastructure.camel.*`** |
| 同进程 Agent 集成 | **`ToolCallback` 直接调用** |
| 跨进程 MCP 实现 | **MCP Java SDK 内置 `McpServer`**（`io.modelcontextprotocol.sdk:mcp:1.1.0` 已就绪） |
| 数据源 ToolCallback 归属 | **`infrastructure.tools.data_source`** 包（不引用 Camel） |
| 表结构元数据 | **DataSourceSchema**（与 AgentDataSource 关联，支持表/字段/关联/可执行操作） |
| 权限策略 | **第 5 个 PermissionStrategy**（全面权限控制） |
| 数据源与权限策略关联 | **一对一绑定**（AgentDataSource.permissionPolicyId 可选） |
| 工期 | **一次性完成**（压缩到 5 个工作块，并行执行） |
| 前端 | **遵循现有浮动按钮规范**（右侧悬浮 `FloatingAddButton` 触发 `global-add` 事件） |

## 2. 架构设计

### 2.1 分层依赖

```
api (Controller/DTO)            ← AgentDataSourceController, AgentDataSourceComponentController
   ↓
application (UseCase/Port)      ← AgentDataSourceUseCase, AgentDataSourcePort, AgentDataSourceRepository
   ↓
domain (Model)                  ← AgentDataSource, AgentDataSourceProtocol, AgentDataSourceStatus
   ↑
infrastructure/
├── camel/*                     ← Camel 实现（CamelDataSourceRuntime, CamelAgentDataSourceAdapter）
├── tools/data_source/*         ← AgentDataSourceToolCallbackProvider（同进程入口，不引用 Camel）
├── web/McpServerHttpHandler    ← MCP Java SDK 内置 McpServer 包装（跨进程入口）
└── store/db/*                  ← MybatisAgentDataSourceRepository
```

**包级 Camel 引用约束（ArchUnit 强制）**：

| 包 | 可引用 `org.apache.camel.*` | 可引用 `io.modelcontextprotocol.*` |
|---|:-:|:-:|
| `infrastructure.camel.*` | ✅ | ✅（仅 web 入口） |
| `infrastructure.tools.data_source.*` | ❌ | ❌（只依赖 application port） |
| `infrastructure.web.*` | ❌ | ✅（MCP Server 注册） |
| `application.*` / `domain.*` / `api.*` | ❌ | ❌ |

### 2.2 双路径架构

```
┌─────────────── AgentHub 进程内 ────────────────────┐
│                                                      │
│  ┌─ 同进程快速路径（主，零开销）─────────────────┐ │
│  │                                                │ │
│  │  Agent (Spring AI)                            │ │
│  │       ↓ 直接 Java 方法调用                    │ │
│  │  ToolCallback[]                               │ │
│  │       ↓                                       │ │
│  │  AgentDataSourceToolCallbackProvider          │ │
│  │  (infrastructure.tools.data_source)           │ │
│  │       ↓ 调用 Port                             │ │
│  │  AgentDataSourcePort                          │ │
│  │       ↓ 实现                                  │ │
│  │  CamelAgentDataSourceAdapter                  │ │
│  │  (infrastructure.camel)                       │ │
│  │       ↓ ProducerTemplate                      │ │
│  │  CamelContext (in-process)                    │ │
│  │                                                │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
│  ┌─ 跨进程标准路径（次，标准协议）───────────────┐ │
│  │                                                │ │
│  │  外部 MCP Client                               │ │
│  │  (Claude Desktop / Cursor / 第三方)           │ │
│  │       ↓ MCP (SSE + JSON-RPC over HTTP)        │ │
│  │  /mcp/agent-data-sources/{wsId}/sse           │ │
│  │       ↓                                       │ │
│  │  AgentDataSourceMcpServerHandler              │ │
│  │  (infrastructure.web)                         │ │
│  │       ↓ 注册 McpServer via SDK                │ │
│  │  io.modelcontextprotocol.server.McpServer     │ │
│  │       ↓ 工具回调                              │ │
│  │  AgentDataSourceMcpToolAdapter                │ │
│  │       ↓ 复用同一个 Port                       │ │
│  │  AgentDataSourcePort                          │ │
│  │       ↓ 实现                                  │ │
│  │  CamelAgentDataSourceAdapter                  │ │
│  │       ↓                                       │ │
│  │  CamelContext (in-process)                    │ │
│  │                                                │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
└──────────────────────────────────────────────────────┘
```

**关键洞察**：
- 两条路径**共用同一个 `AgentDataSourcePort`** 和 `CamelAgentDataSourceAdapter`
- 跨进程路径使用 **MCP Java SDK 1.1.0**（项目已引入 `io.modelcontextprotocol.sdk:mcp:1.1.0`），**完全不需要自定义 JSON-RPC**
- 同进程路径是直接 Java 方法调用，零序列化

### 2.3 文件清单

| 层 | 文件 | 操作 | 用途 |
|----|------|------|------|
| **domain/model** | `AgentDataSource.java` | 新建 | 数据源聚合根 |
| domain/model | `AgentDataSourceDescriptor.java` | 新建 | 协议描述符 |
| domain/model | `AgentDataSourceField.java` | 新建 | 字段定义 |
| domain/enums | `AgentDataSourceProtocol.java` | 新建 | 协议枚举 |
| domain/enums | `AgentDataSourceStatus.java` | 新建 | 状态机 |
| domain/model | `DataSourceSchema.java` | 新建 | **新增** 数据源表结构元数据 |
| domain/model | `DataSourceTable.java` | 新建 | **新增** 表 |
| domain/model | `DataSourceColumn.java` | 新建 | **新增** 字段 |
| domain/model | `TableRelationship.java` | 新建 | **新增** 关联关系 |
| domain/enums | `TableOperation.java` | 新建 | **新增** READ/INSERT/UPDATE/DELETE/DROP |
| domain/model | `PermissionStrategy.java` | 新建 | **新增** 第 5 个策略 |
| domain/enums | `OperationLevel.java` | 新建 | **新增** CRUD 细粒度（CREATE/READ/UPDATE/DELETE） |
| domain/event | `AuditEvent.java` | 新建 | **新增** 全局审计事件（覆盖 Agent 全生命周期） |
| domain/exception | `AgentDataSourceNotFoundException.java` | 新建 | 404 |
| domain/exception | `AgentDataSourceValidationException.java` | 新建 | 400 |
| domain/exception | `AgentDataSourceConflictException.java` | 新建 | 409 |
| domain/exception | `PermissionDeniedException.java` | 新建 | **新增** 403 |
| domain/exception | `RateLimitExceededException.java` | 新建 | **新增** 429 |
| domain/exception | `DangerousOperationBlockedException.java` | 新建 | **新增** 403 |
| **application/port/out** | `AgentDataSourceRepository.java` | 新建 | 持久化端口 |
| application/port/out | `DataSourceSchemaRepository.java` | 新建 | **新增** Schema 持久化 |
| application/port/out | `PermissionStrategyRepository.java` | 新建 | **新增** 策略持久化 |
| application/port/out | `AuditLogRepository.java` | 新建 | **新增（替换）** 全局审计日志持久化 |
| application/port/out | `AgentDataSourcePort.java` | 新建 | 运行时操作端口 |
| application/port/out | `DataSourcePermissionPort.java` | 新建 | **新增** 权限校验 + 审计 + 限流 |
| application/port/out | `AuditLogger.java` | 新建 | **新增** 全局审计记录端口（所有 UseCase 调用） |
| application/command | `CreateAgentDataSourceCommand.java` | 新建 | 创建命令 |
| application/command | `UpdateAgentDataSourceCommand.java` | 新建 | 更新命令 |
| application/dto | `AgentDataSourceOutput.java` | 新建 | 列表/详情输出 |
| application/dto | `AgentDataSourceDescriptorOutput.java` | 新建 | 描述符输出 |
| application/dto | `AgentDataSourceInvokeOutput.java` | 新建 | 调用结果输出 |
| application/dto | `AgentDataSourceTestOutput.java` | 新建 | 测试结果输出 |
| application/event | `AgentDataSourceChangedEvent.java` | 新建 | 领域事件 |
| application/usecase | `AgentDataSourceUseCase.java` | 新建 | CRUD + 启用/禁用 |
| application/usecase | `AgentDataSourceBootstrapUseCase.java` | 新建 | 启动恢复 |
| application/usecase | `DataSourceSchemaUseCase.java` | 新建 | **新增** Schema CRUD + 自动发现 |
| application/usecase | `PermissionStrategyUseCase.java` | 新建 | **新增** 策略 CRUD |
| application/usecase | `AuditLogUseCase.java` | 新建 | **新增（替换）** 全局审计日志查询（多资源类型过滤） |
| application/service | `DataSourcePermissionService.java` | 新建 | **新增** 应用层服务：组合 ACL/限流/审计/表权限 |
| application/annotation | `Audited.java` | 新建 | **新增** @Audited 注解（自动审计） |
| application/aspect | `AuditAspect.java` | 新建 | **新增** 审计切面（AOP 拦截 @Audited） |
| **infrastructure/store/db/entity** | `AgentDataSourceEntity.java` | 新建 | MyBatis 实体 |
| infrastructure/store/db/entity | `DataSourceSchemaEntity.java` | 新建 | **新增** Schema 实体 |
| infrastructure/store/db/entity | `DataSourceTableEntity.java` | 新建 | **新增** Table 实体 |
| infrastructure/store/db/entity | `DataSourceColumnEntity.java` | 新建 | **新增** Column 实体 |
| infrastructure/store/db/entity | `TableRelationshipEntity.java` | 新建 | **新增** Relationship 实体 |
| infrastructure/store/db/entity | `PermissionStrategyEntity.java` | 新建 | **新增** 策略实体 |
| infrastructure/store/db/entity | `AuditLogEntity.java` | 新建 | **新增（替换）** 全局审计日志实体 |
| infrastructure/store/db/mapper | `AgentDataSourceMapper.java` | 新建 | Mapper |
| infrastructure/store/db/mapper | `DataSourceSchemaMapper.java` | 新建 | **新增** |
| infrastructure/store/db/mapper | `PermissionStrategyMapper.java` | 新建 | **新增** |
| infrastructure/store/db/mapper | `AuditLogMapper.java` | 新建 | **新增（替换）** |
| infrastructure/store/db/repository | `MybatisAgentDataSourceRepository.java` | 新建 | 仓储实现 |
| infrastructure/store/db/repository | `MybatisDataSourceSchemaRepository.java` | 新建 | **新增** |
| infrastructure/store/db/repository | `MybatisPermissionStrategyRepository.java` | 新建 | **新增** |
| infrastructure/store/db/repository | `MybatisAuditLogRepository.java` | 新建 | **新增（替换）** |
| infrastructure/store/cache | `InMemoryRateLimiter.java` | 新建 | **新增** 滑动窗口限流器 |
| infrastructure/store/audit | `AuditRecorder.java` | 新建 | **新增（替换）** 全局异步审计日志写入 |
| **infrastructure/camel** | `CamelDataSourceRuntime.java` | 新建 | Camel 引擎入口 |
| infrastructure/camel | `CamelContextRegistry.java` | 新建 | 工作区 CamelContext 注册表 |
| infrastructure/camel | `CamelAgentDataSourceAdapter.java` | 新建 | **实现 `AgentDataSourcePort`** |
| infrastructure/camel | `CamelComponentIntrospector.java` | 新建 | Camel Component → Descriptor |
| infrastructure/camel | `CamelAgentDataSourceMapper.java` | 新建 | Entity ↔ Domain 转换 |
| infrastructure/camel | `CamelSchemaIntrospector.java` | 新建 | **新增** 通过 Camel JDBC 自动发现 Schema |
| infrastructure/config | `CamelAutoConfiguration.java` | 新建 | 启动/关闭钩子 |
| **infrastructure/tools/data_source** | `AgentDataSourceToolFactory.java` | 新建 | AgentDataSource → ToolCallback |
| infrastructure/tools/data_source | `AgentDataSourceToolCallbackProvider.java` | 新建 | **同进程路径** |
| infrastructure/tools/data_source | `AgentDataSourceChangedListener.java` | 新建 | 监听事件刷新 ToolCallback |
| **infrastructure/web** | `AgentDataSourceMcpToolAdapter.java` | 新建 | **跨进程路径** MCP Tool 适配 |
| infrastructure/web | `AgentDataSourceMcpServerHandler.java` | 新建 | **跨进程路径** HTTP/SSE Handler |
| infrastructure/web | `AgentDataSourceMcpChangedListener.java` | 新建 | 监听事件通知 MCP 客户端 |
| **api/dto** | `AgentDataSourceRequest.java` | 新建 | 创建/更新请求 |
| api/dto | `AgentDataSourceResponse.java` | 新建 | 响应 |
| api/dto | `AgentDataSourceTestResponse.java` | 新建 | 测试响应 |
| api/dto | `AgentDataSourceDescriptorResponse.java` | 新建 | 描述符响应 |
| api/mapper | `AgentDataSourceViewMapper.java` | 新建 | Output ↔ Response 转换 |
| **api/controller** | `AgentDataSourceController.java` | 新建 | 数据源 CRUD + 调试 |
| api/controller | `AgentDataSourceComponentController.java` | 新建 | 协议元数据查询 |
| api/controller | `DataSourceSchemaController.java` | 新建 | **新增** Schema CRUD + 自动发现 |
| api/controller | `PermissionStrategyController.java` | 新建 | **新增** 策略 CRUD |
| api/controller | `AuditLogController.java` | 新建 | **新增（替换）** 全局审计日志查询（租户级） |
| **frontend** | `src/main/web/src/views/agenthub/AgentDataSourceView.vue` | 新建 | 数据源管理页（CRUD + 调试 + Schema 面板） |
| frontend | `src/main/web/src/views/agenthub/AuditLogView.vue` | 新建 | **新增（替换）** 全局审计日志查询页 |
| frontend | `src/main/web/src/components/datasource/SchemaPanel.vue` | 新建 | **新增** Schema 表格/字段/关联管理 |
| frontend | `src/main/web/src/components/datasource/SchemaIntrospectDialog.vue` | 新建 | **新增** 自动发现向导 |
| frontend | `src/main/web/src/components/strategy/PermissionStrategyPanel.vue` | 新建 | **新增** 第 5 个策略面板 |
| frontend | `src/main/web/src/components/audit/AuditLogTable.vue` | 新建 | **新增** 审计日志表格（通用） |
| frontend | `src/main/web/src/components/audit/AuditLogFilter.vue` | 新建 | **新增** 多维度筛选器 |
| frontend | `src/main/web/src/api/agent-data-source-api.ts` | 新建 | 前端 API 客户端 |
| frontend | `src/main/web/src/api/data-source-schema-api.ts` | 新建 | **新增** |
| frontend | `src/main/web/src/api/permission-strategy-api.ts` | 新建 | **新增** |
| frontend | `src/main/web/src/api/audit-log-api.ts` | 新建 | **新增** |
| frontend | `src/main/web/src/types/agentDataSource.ts` | 新建 | 前端类型 |
| frontend | `src/main/web/src/types/dataSourceSchema.ts` | 新建 | **新增** |
| frontend | `src/main/web/src/types/permissionStrategy.ts` | 新建 | **新增** |
| frontend | `src/main/web/src/types/auditLog.ts` | 新建 | **新增** |
| frontend | `src/main/web/src/views/agenthub/StrategyManagementView.vue` | 修改 | **新增** 第 5 个 Tab：权限策略 |
| frontend | `src/main/web/src/components/strategy/PermissionStrategyPanel.vue` | 新建 | **新增** 权限策略 CRUD 面板 |
| frontend | `src/main/web/src/router/index.ts` | 修改 | 新增路由 `agent-data-sources` + `audit-logs` |
| frontend | `src/main/web/src/components/AgentHubLayout.vue` | 修改 | `showAddButton` 添加路径匹配 |
| **sql** | `sql/V100__add_agent_data_source_tables.sql` | 新建 | 数据源主表迁移 |
| sql | `sql/V101__add_data_source_schema_tables.sql` | 新建 | **新增** Schema 元数据迁移 |
| sql | `sql/V102__add_permission_strategy_tables.sql` | 新建 | **新增** 权限策略迁移 |
| sql | `sql/V103__add_global_audit_log_table.sql` | 新建 | **新增（替换）** 全局审计日志迁移（覆盖 Agent 全生命周期） |
| sql | `sql/schema.sql` | 修改 | 自动生成同步 |
| **test** | `AgentDataSourceControllerIntegrationTest.java` | 新建 | 集成测试（CRUD + 调试） |
| test | `AgentDataSourceUseCaseUnitTest.java` | 新建 | UseCase 单元测试 |
| test | `DataSourcePermissionServiceUnitTest.java` | 新建 | **新增** 权限服务单元测试 |
| test | `InMemoryRateLimiterUnitTest.java` | 新建 | **新增** 限流器单元测试 |
| test | `DataSourceSchemaUseCaseUnitTest.java` | 新建 | **新增** Schema UseCase 单元测试 |
| test | `PermissionStrategyUseCaseUnitTest.java` | 新建 | **新增** 策略 UseCase 单元测试 |
| test | `CamelSchemaIntrospectorIntegrationTest.java` | 新建 | **新增** Schema 自动发现集成测试 |
| test | `CamelAgentDataSourceAdapterIntegrationTest.java` | 新建 | **新增** 集成权限校验的 Camel 适配器测试 |
| test | `AuditAspectUnitTest.java` | 新建 | **新增** 审计切面单元测试（验证 @Audited 自动记录） |
| test | `AuditLogControllerIntegrationTest.java` | 新建 | **新增** 审计日志查询集成测试 |
| test | `AgentHubCleanArchitectureTest.java` | 修改 | 新增 Camel 引用约束 |
| **build.gradle** | `build.gradle` | 修改 | 引入 camel 依赖 |

## 3. 领域模型（不引用 Camel）

### 3.1 AgentDataSource

```java
@Data @NoArgsConstructor @AllArgsConstructor
public class AgentDataSource {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private AgentDataSourceProtocol protocol;
    private String endpointUri;
    private String propertiesJson;
    private boolean enabled;
    private AgentDataSourceStatus status;
    private String lastErrorMessage;
    private Instant lastCheckedAt;
    private String permissionPolicyId;       // ← 新增：关联 PermissionStrategy（一对一，可选）
    private String schemaId;                 // ← 新增：关联 DataSourceSchema（可选）
    private Instant createdAt;
    private Instant updatedAt;
}
```

### 3.2 AgentDataSourceProtocol

```java
public enum AgentDataSourceProtocol {
    JDBC, JMS, KAFKA, HTTP, HTTPS, FTP, SFTP, FILE,
    MAIL, MONGODB, REDIS, SQL, REST, DIRECT, TIMER
}
```

### 3.3 AgentDataSourceStatus

```java
public enum AgentDataSourceStatus {
    DISABLED, ENABLED, CONNECTING, ERROR
}
```

### 3.4 AgentDataSourceDescriptor

```java
@Data @NoArgsConstructor @AllArgsConstructor
public class AgentDataSourceDescriptor {
    private AgentDataSourceProtocol protocol;
    private String scheme;
    private String displayName;
    private String description;
    private String syntaxHint;
    private List<AgentDataSourceField> fields;
}

@Data @NoArgsConstructor @AllArgsConstructor
public class AgentDataSourceField {
    private String name;
    private String type;          // "string" | "integer" | "boolean" | "password"
    private boolean required;
    private String defaultValue;
    private String description;
    private String placeholder;
}
```

### 3.5 DataSourceSchema（数据源表结构元数据）— 新增

```java
@Data @NoArgsConstructor @AllArgsConstructor
public class DataSourceSchema {
    private String id;
    private String dataSourceId;             // 关联 AgentDataSource.id（一对一）
    private String tenantId;
    private String workspaceId;
    private String displayName;
    private String description;
    private List<DataSourceTable> tables;
    private Instant lastIntrospectedAt;       // 最后一次自动发现时间
    private Instant createdAt;
    private Instant updatedAt;
}

@Data @NoArgsConstructor @AllArgsConstructor
public class DataSourceTable {
    private String id;                       // 表唯一 ID
    private String schemaId;                 // 所属 Schema
    private String tableName;                // 表名（如 orders）
    private String displayName;              // 显示名（如 "订单表"）
    private String description;
    private List<DataSourceColumn> columns;
    private List<TableRelationship> relationships;
    private Set<TableOperation> allowedOperations;  // 该表允许的操作（READ/WRITE/UPDATE/DELETE）
    private List<Map<String, Object>> sampleData;   // 样例数据（最多 10 行）
    private Instant createdAt;
    private Instant updatedAt;
}

@Data @NoArgsConstructor @AllArgsConstructor
public class DataSourceColumn {
    private String name;
    private String type;                     // "VARCHAR" / "INTEGER" / "TIMESTAMP" / ...
    private boolean nullable;
    private boolean isPrimaryKey;
    private String defaultValue;
    private String description;
    private boolean pii;                      // 是否 PII 字段（用于脱敏）
    private String piiType;                   // "PHONE" / "EMAIL" / "ID_CARD" / ...
}

@Data @NoArgsConstructor @AllArgsConstructor
public class TableRelationship {
    private String name;                     // 关系名（如 "orders_to_customers"）
    private String type;                     // "ONE_TO_ONE" / "ONE_TO_MANY" / "MANY_TO_MANY"
    private String targetTableId;            // 目标表 ID
    private String sourceColumn;             // 源字段
    private String targetColumn;             // 目标字段
    private String description;
}

public enum TableOperation {
    READ,       // SELECT / query
    INSERT,     // 插入
    UPDATE,     // 更新
    DELETE,     // 删除
    DROP        // 危险：删表
}
```

**关键设计**：
- 每个 AgentDataSource 可选关联一个 DataSourceSchema（一对一）
- Schema 既可**手动配置**（管理员声明表结构），也可**自动发现**（调用 Camel JDBC 查询 `INFORMATION_SCHEMA`）
- 字段级 PII 标记 → 与 GuardrailStrategy 联动做结果脱敏
- 关联关系 → Agent 生成 JOIN 查询时校验是否合法

### 3.6 PermissionStrategy（第 5 个策略）— 新增

```java
@Data @NoArgsConstructor @AllArgsConstructor
public class PermissionStrategy {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    
    // ====== ACL ======
    private Set<String> allowedRoles;            // ["OWNER", "ADMIN", "DEVELOPER"]，空 = 不限制
    
    // ====== 操作级别（细粒度 CRUD 4 个，无 ADMIN）======
    private Set<OperationLevel> allowedOperations;  // 允许的 CRUD 操作，如 [READ, CREATE]
    
    // ====== 协议黑名单 ======
    private Set<AgentDataSourceProtocol> protocolBlocklist;  // 禁用的协议
    
    // ====== 危险操作拦截 ======
    private boolean dangerousSqlKeywordsBlock;   // 是否拦截 DELETE/DROP/UPDATE 等（默认 true）
    private Set<TableOperation> requireApprovalFor;  // 需要审批的操作（如 [DELETE, DROP]）
    
    // ====== 表级权限（可覆盖 DataSourceTable.allowedOperations） ======
    private Map<String, Set<TableOperation>> tablePermissions;  // tableName → 允许的操作
    
    // ====== 速率限制 ======
    private int rateLimitPerMinute;             // 0 = 不限制
    private int rateLimitPerHour;               // 0 = 不限制
    
    // ====== 审计日志 ======
    private boolean auditLogEnabled;            // 默认 true
    private int auditLogRetentionDays;          // 默认 90
    
    // ====== 结果脱敏 ======
    private boolean piiMaskingOnResult;         // 是否对结果做 PII 脱敏（联动 GuardrailStrategy）
    
    private Instant createdAt;
    private Instant updatedAt;
}

/**
 * 操作级别 - 细粒度 CRUD 4 个，去掉 ADMIN
 * <p>
 * 与传统 READ_ONLY/READ_WRITE/ADMIN 层级模型不同，本枚举表达
 * "允许的最小操作集合"，通过 Set 组合实现细粒度授权。
 * </p>
 */
public enum OperationLevel {
    CREATE,  // 增
    READ,    // 查
    UPDATE,  // 改
    DELETE   // 删
}
```

**关键设计**：
- 与现有 4 个策略**同构**：workspace 范围、有 CRUD 面板、绑定到数据源
- **细粒度授权**：`Set<OperationLevel>` 表达允许的最小操作集合，例如 `{READ, CREATE}` 表示"只读 + 增"，不能改不能删
- 6 个绑定维度：allowedRoles / allowedOperations / protocolBlocklist / tablePermissions / rateLimit / piiMasking
- `tablePermissions` 优先级高于 `DataSourceTable.allowedOperations`（更细粒度）
- 审计日志通过**全局 `audit_log` 表**持久化（覆盖 Agent 全生命周期）

## 4. Application 端口

### 4.1 AgentDataSourceRepository

```java
public interface AgentDataSourceRepository {
    AgentDataSource save(AgentDataSource source);
    Optional<AgentDataSource> findById(String id);
    List<AgentDataSource> findByWorkspaceId(String workspaceId);
    List<AgentDataSource> findEnabledByWorkspaceId(String workspaceId);
    List<AgentDataSource> findAllEnabled();
    boolean existsByWorkspaceIdAndName(String workspaceId, String name);
    void deleteById(String id);
}
```

### 4.2 AgentDataSourcePort

```java
public interface AgentDataSourcePort {
    AgentDataSourceInvokeOutput invoke(AgentDataSource source, Map<String, Object> headers, Object body);
    AgentDataSourceTestOutput testConnection(AgentDataSource source);
    void enable(AgentDataSource source);
    void disable(String workspaceId, String sourceId);
    List<AgentDataSourceDescriptor> listAvailableDescriptors();
    void bootstrap(List<AgentDataSource> enabled);
    void shutdown();
}
```

## 5. 基础设施层

### 5.1 路径 A：同进程快速路径（Spring AI ToolCallback）

```java
// infrastructure/tools/data_source/AgentDataSourceToolFactory.java
@Component
@RequiredArgsConstructor
public class AgentDataSourceToolFactory {
    private final AgentDataSourcePort port;
    private final AgentDataSourceRepository repository;

    public ToolCallback toToolCallback(AgentDataSource source) {
        return MethodToolCallback.builder()
            .toolDefinition(ToolDefinition.builder()
                .name(sanitizeToolName("agent_data_source_invoke_" + source.getName()))
                .description("调用 Agent 数据源 [" + source.getName() + "]（" + source.getProtocol() + "）")
                .inputSchema(generateInputSchema(source))
                .build())
            .toolMethod(invokeMethod())
            .build();
    }

    public ToolCallback listToolCallback(String workspaceId) { /* ... */ }
}
```

```java
// infrastructure/tools/data_source/AgentDataSourceToolCallbackProvider.java
@Component
@RequiredArgsConstructor
public class AgentDataSourceToolCallbackProvider implements ToolCallbackProvider, ApplicationListener<AgentDataSourceChangedEvent> {
    private final AgentDataSourceRepository repository;
    private final AgentDataSourceToolFactory factory;
    private volatile ToolCallback[] cached = new ToolCallback[0];

    @Override
    public ToolCallback[] getToolCallbacks() {
        return cached;
    }

    @Override
    public void onApplicationEvent(AgentDataSourceChangedEvent event) {
        String workspaceId = WorkspaceContextHolder.currentWorkspaceId();
        cached = repository.findEnabledByWorkspaceId(workspaceId).stream()
            .map(factory::toToolCallback)
            .toArray(ToolCallback[]::new);
    }

    @PostConstruct
    public void init() {
        onApplicationEvent(null);
    }
}
```

**关键特性**：
- **零协议开销**：直接 Java 方法调用
- **事件驱动刷新**：`AgentDataSourceChangedEvent` 触发缓存重建
- **工作区隔离**：通过 `WorkspaceContextHolder` 获取当前 workspace

### 5.2 路径 B：跨进程标准路径（MCP Java SDK 内置 McpServer）

```java
// infrastructure/web/AgentDataSourceMcpServerHandler.java
@RestController
@RequestMapping("/mcp/agent-data-sources/{workspaceId}")
@RequiredArgsConstructor
public class AgentDataSourceMcpServerHandler {
    private final AgentDataSourceMcpToolAdapter toolAdapter;
    private McpSyncServer mcpServer;
    private HttpServletSseServerTransport transport;

    @PostConstruct
    public void init() throws IOException {
        // 使用 MCP Java SDK 内置的 HttpServletSseServerTransport
        this.transport = HttpServletSseServerTransport.builder()
            .objectMapper(new ObjectMapper())
            .build();
        
        // 注册 McpServer（同步版本）
        this.mcpServer = McpServer.sync(transport)
            .serverInfo("agent-data-source-server", "1.0.0")
            .capabilities(ServerCapabilities.builder().tools(true).build())
            .build();
        
        // 注册工具（每个 enabled 数据源 = 一个 MCP Tool）
        refreshTools();
    }

    @GetMapping("/sse")
    public void sse(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        transport.handle(request, response);
    }

    @PostMapping("/message")
    public void message(HttpServletRequest request, @RequestBody String body, 
                        HttpServletResponse response) throws IOException {
        transport.handle(request, response);
    }
    
    public void refreshTools() {
        // 注销旧工具 → 重新注册
        // ...
    }
}
```

```java
// infrastructure/web/AgentDataSourceMcpToolAdapter.java
@Component
public class AgentDataSourceMcpToolAdapter {
    private final AgentDataSourcePort port;

    public McpServerFeatures.SyncToolSpecification toToolSpec(AgentDataSource source) {
        // 把 AgentDataSource 转换为 MCP Tool
        McpSchema.Tool tool = new McpSchema.Tool(
            sanitizeName("agent_data_source_invoke_" + source.getName()),
            "调用 Agent 数据源 [" + source.getName() + "]",
            generateInputSchemaJson(source)
        );
        
        // MCP Tool 调用 → AgentDataSourcePort.invoke
        BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler = 
            (exchange, params) -> {
                AgentDataSourceInvokeOutput output = port.invoke(source, params, params.get("body"));
                return new McpSchema.CallToolResult(
                    List.of(new McpSchema.TextContent(output.toJson())),
                    output.isSuccess()
                );
            };
        
        return new McpServerFeatures.SyncToolSpecification(tool, handler);
    }
}
```

**关键洞察**：
- **零自定义 JSON-RPC**：MCP Java SDK 提供 `HttpServletSseServerTransport` 处理 HTTP/SSE
- **工具注册 API**：`McpServerFeatures.SyncToolSpecification` 把工具注册到 server
- **Spring 集成**：用 `@RestController` 暴露端点，把请求转给 SDK 的 transport

### 5.3 事件同步机制

`AgentDataSourceUseCase` 在 enable/disable 时发布 `AgentDataSourceChangedEvent`：

```java
// application/event/AgentDataSourceChangedEvent.java
public class AgentDataSourceChangedEvent extends ApplicationEvent {
    private final String workspaceId;
    private final String sourceId;
    private final ChangeType changeType;   // ENABLED / DISABLED / UPDATED
    
    public enum ChangeType { ENABLED, DISABLED, UPDATED, DELETED }
}
```

**两个监听器**：

```java
// infrastructure/tools/data_source/AgentDataSourceChangedListener.java
@Component
@RequiredArgsConstructor
class AgentDataSourceChangedListener {
    private final AgentDataSourceToolCallbackProvider provider;
    
    @EventListener
    public void on(AgentDataSourceChangedEvent e) {
        provider.refresh();   // 同进程路径：刷新 ToolCallback 缓存
    }
}

// infrastructure/web/AgentDataSourceMcpChangedListener.java
@Component
@RequiredArgsConstructor
class AgentDataSourceMcpChangedListener {
    private final AgentDataSourceMcpServerHandler handler;
    
    @EventListener
    public void on(AgentDataSourceChangedEvent e) {
        handler.refreshTools();   // 跨进程路径：注销 + 重新注册 MCP Tool
    }
}
```

### 5.4 Camel 引擎实现

#### 5.4.1 启动策略

**不引入** `camel-spring-boot-starter`，只引入：
- `camel-core` + `camel-main`
- 15 个 `camel-<component>` 依赖

手动使用 `DefaultCamelContext` 创建每工作区上下文。

#### 5.4.2 CamelDataSourceRuntime

```java
@Component
public class CamelDataSourceRuntime {
    private final CamelContextRegistry registry = new CamelContextRegistry();
    private final CamelComponentIntrospector introspector = new CamelComponentIntrospector();

    public void bootstrap(List<AgentDataSource> enabled) {
        // 按 workspaceId 分组 → 创建/复用 CamelContext
        // 对每个 source 创建 direct: 路由 → endpointUri
    }
    public void shutdown() { registry.shutdownAll(); }
}
```

#### 5.4.3 CamelContextRegistry

```java
public class CamelContextRegistry {
    private final Map<String, CamelContext> contexts = new ConcurrentHashMap<>();
    private final Map<String, String> sourceIndex = new ConcurrentHashMap<>();
    public Optional<CamelContext> get(String workspaceId);
    public void addRoute(String workspaceId, String sourceId, String uri);
    public void removeRoute(String workspaceId, String sourceId);
    public void shutdownAll();
}
```

#### 5.4.4 路由设计

数据源启用时动态注册：

```
from("direct:agent-data-source-<sourceId>")
    .routeId("agent-ds-<sourceId>")
    .to("<endpointUri>")
    .process(exchange -> { /* 记录调用指标 */ });
```

#### 5.4.5 CamelAgentDataSourceAdapter（集成权限校验）

```java
@Component
@RequiredArgsConstructor
public class CamelAgentDataSourceAdapter implements AgentDataSourcePort {
    private final CamelDataSourceRuntime runtime;
    private final DataSourcePermissionService permissionService;  // ← 新增：应用层服务

    public AgentDataSourceInvokeOutput invoke(AgentDataSource source, Map<String, Object> headers, Object body) {
        long start = System.currentTimeMillis();
        String callerId = WorkspaceContextHolder.currentUserId();
        String agentId = WorkspaceContextHolder.currentAgentId();
        String sessionId = WorkspaceContextHolder.currentSessionId();
        
        try {
            // 1. 权限校验：ACL + 协议黑名单 + 速率限制 + 危险操作拦截
            permissionService.checkPermission(callerId, agentId, source, body);
            
            // 2. workspace 隔离
            validateWorkspaceIsolation(source);
            
            // 3. Camel 路由调用
            CamelContext ctx = runtime.getRegistry().get(source.getWorkspaceId())
                .orElseThrow(() -> new IllegalStateException("CamelContext not found"));
            ProducerTemplate template = ctx.createProducerTemplate();
            String routeUri = "direct:agent-data-source-" + source.getId();
            
            Exchange exchange = template.send(routeUri, ex -> {
                ex.getIn().setBody(body);
                if (headers != null) headers.forEach(ex.getIn().setHeader()::set);
            });
            
            Object resultBody = exchange.getMessage().getBody();
            long elapsed = System.currentTimeMillis() - start;
            
            // 4. 结果处理（如果策略启用 PII 脱敏）
            Object masked = permissionService.maskResultIfNeeded(source, resultBody);
            
            AgentDataSourceInvokeOutput output = new AgentDataSourceInvokeOutput(
                true, masked, elapsed, exchange.getExchangeId(), null);
            
            // 5. 审计日志（异步）
            permissionService.recordAudit(callerId, agentId, sessionId, source, body, output, "INVOKE");
            return output;
            
        } catch (PermissionDeniedException | DangerousOperationBlockedException | RateLimitExceededException e) {
            // 拒绝路径：仍记录审计
            permissionService.recordAudit(callerId, agentId, sessionId, source, body, null, "DENIED", e.getMessage());
            throw e;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            permissionService.recordAudit(callerId, agentId, sessionId, source, body, null, "FAILED", e.getMessage());
            return new AgentDataSourceInvokeOutput(false, null, elapsed, null, e.getMessage());
        }
    }

    public void enable(AgentDataSource source) { /* addRoute */ }
    public void disable(String workspaceId, String sourceId) { /* removeRoute */ }
    public List<AgentDataSourceDescriptor> listAvailableDescriptors() {
        return runtime.getIntrospector().listDescriptors();
    }
}
```

### 5.5 DataSourcePermissionService（应用层服务）— 新增

```java
// application/service/DataSourcePermissionService.java
@Service
@RequiredArgsConstructor
public class DataSourcePermissionService {
    private final PermissionStrategyRepository policyRepository;
    private final DataSourcePermissionPort port;
    private final AuditLogger auditLogger;
    private final GuardrailStrategyUseCase guardrailUseCase;  // 复用现有护栏做结果脱敏
    private final AuthorizationPort authzPort;              // 复用现有角色检查

    public void checkPermission(String userId, String agentId, AgentDataSource source, Object body) {
        PermissionStrategy policy = resolvePolicy(source);
        if (policy == null) return;  // 无策略：不限制

        // 1. ACL 角色检查
        if (!policy.getAllowedRoles().isEmpty()) {
            String userRole = authzPort.getUserRole(userId, source.getWorkspaceId());
            if (!policy.getAllowedRoles().contains(userRole)) {
                throw new PermissionDeniedException("用户角色 " + userRole + " 不在允许列表中");
            }
        }

        // 2. 协议黑名单
        if (policy.getProtocolBlocklist().contains(source.getProtocol())) {
            throw new PermissionDeniedException("协议 " + source.getProtocol() + " 已被该策略禁用");
        }

        // 3. 操作级别（细粒度 CRUD）
        OperationLevel op = detectOperationLevel(source, body);
        validateOperationLevel(policy.getAllowedOperations(), op);

        // 4. 危险 SQL 关键字
        if (policy.isDangerousSqlKeywordsBlock() && isSqlDataSource(source)) {
            List<String> dangerous = findDangerousSqlKeywords(body);
            if (!dangerous.isEmpty()) {
                throw new DangerousOperationBlockedException("检测到危险 SQL 操作: " + dangerous);
            }
        }

        // 5. 表级权限
        String targetTable = extractTargetTable(source, body);
        if (targetTable != null && policy.getTablePermissions().containsKey(targetTable)) {
            Set<TableOperation> allowed = policy.getTablePermissions().get(targetTable);
            if (!allowed.contains(toTableOperation(op))) {
                throw new PermissionDeniedException("表 " + targetTable + " 不允许 " + op + " 操作");
            }
        }

        // 6. 速率限制
        port.checkRateLimit(userId, source.getId(), policy.getRateLimitPerMinute(), policy.getRateLimitPerHour());

        // 7. 需要审批的操作
        if (policy.getRequireApprovalFor().contains(toTableOperation(op))) {
            // TODO: 触发审批流（后续版本）
            throw new DangerousOperationBlockedException("操作 " + op + " 需要审批（暂未实现）");
        }
    }

    public Object maskResultIfNeeded(AgentDataSource source, Object result) {
        PermissionStrategy policy = resolvePolicy(source);
        if (policy == null || !policy.isPiiMaskingOnResult()) return result;
        // 复用 GuardrailStrategy 的 PII 脱敏
        String resultStr = objectMapper.writeValueAsString(result);
        String masked = guardrailUseCase.maskPii(resultStr, source.getWorkspaceId());
        return objectMapper.readValue(masked, Object.class);
    }

    public void recordAudit(String userId, String agentId, String sessionId,
                           AgentDataSource source, Object body,
                           AgentDataSourceInvokeOutput output, String status) {
        recordAudit(userId, agentId, sessionId, source, body, output, status, null);
    }

    public void recordAudit(String userId, String agentId, String sessionId,
                           AgentDataSource source, Object body,
                           AgentDataSourceInvokeOutput output, String status, String errorMessage) {
        // 调用全局审计日志（资源类型=DATA_SOURCE）
        AuditEvent event = AuditEvent.builder()
            .resourceType(AuditResourceType.DATA_SOURCE)
            .resourceId(source.getId())
            .action(toAuditAction(output))  // INVOKE / TEST_CONNECTION
            .actorId(userId)
            .actorType(AuditActorType.USER)
            .agentId(agentId)
            .sessionId(sessionId)
            .workspaceId(source.getWorkspaceId())
            .tenantId(source.getTenantId())
            .request(maskSensitive(body))
            .response(maskSensitive(output))
            .status(status)
            .errorMessage(errorMessage)
            .elapsedMs(0L)
            .metadata(Map.of("protocol", source.getProtocol().name()))
            .build();
        auditLogger.logAsync(event);
    }

    private PermissionStrategy resolvePolicy(AgentDataSource source) {
        if (source.getPermissionPolicyId() == null) return null;
        return policyRepository.findById(source.getPermissionPolicyId()).orElse(null);
    }
}
```

### 5.6 DataSourcePermissionPort（基础设施实现）— 新增

```java
// application/port/out/DataSourcePermissionPort.java
public interface DataSourcePermissionPort {
    void checkRateLimit(String userId, String dataSourceId, int perMinute, int perHour);
}

// infrastructure/store/cache/InMemoryRateLimiter.java
@Component
public class InMemoryRateLimiter implements DataSourcePermissionPort {
    private final ConcurrentHashMap<String, SlidingWindowCounter> minuteCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SlidingWindowCounter> hourCounters = new ConcurrentHashMap<>();
    
    public void checkRateLimit(String userId, String dataSourceId, int perMinute, int perHour) {
        if (perMinute > 0) {
            String key = userId + ":" + dataSourceId;
            SlidingWindowCounter c = minuteCounters.computeIfAbsent(key, k -> new SlidingWindowCounter(60_000));
            if (!c.tryAcquire(perMinute)) throw new RateLimitExceededException("每分钟调用次数超限: " + perMinute);
        }
        if (perHour > 0) {
            // 同上，用 hourCounters
        }
    }
}
```

### 5.7 Schema 自动发现（通过 Camel JDBC）— 新增

```java
// infrastructure/camel/CamelSchemaIntrospector.java
@Component
@RequiredArgsConstructor
public class CamelSchemaIntrospector {
    private final CamelDataSourceRuntime runtime;
    
    public DataSourceSchema introspect(AgentDataSource source) {
        // 1. 仅支持 JDBC/SQL 类型
        if (source.getProtocol() != AgentDataSourceProtocol.JDBC 
            && source.getProtocol() != AgentDataSourceProtocol.SQL) {
            throw new ValidationException("仅 JDBC/SQL 数据源支持自动发现");
        }
        
        // 2. 通过 Camel 调用 INFORMATION_SCHEMA
        CamelContext ctx = runtime.getRegistry().get(source.getWorkspaceId()).orElseThrow();
        ProducerTemplate template = ctx.createProducerTemplate();
        
        // 3. 列出所有表
        List<String> tableNames = template.requestBody(
            source.getEndpointUri() + "&outputType=SelectList",
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
            List.class
        );
        
        // 4. 对每个表查字段
        List<DataSourceTable> tables = tableNames.stream().map(t -> {
            List<Map<String, Object>> columns = template.requestBody(
                source.getEndpointUri(),
                "SELECT COLUMN_NAME, TYPE_NAME, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + t + "'",
                List.class
            );
            // 转换为 DataSourceTable + DataSourceColumn
            return buildTable(t, columns);
        }).toList();
        
        return new DataSourceSchema(UUID.randomUUID().toString(), source.getId(), 
            source.getTenantId(), source.getWorkspaceId(), "Auto-discovered", "", tables, Instant.now(), Instant.now(), Instant.now());
    }
}
```

#### 5.4.6 CamelComponentIntrospector

```java
public class CamelComponentIntrospector {
    public List<AgentDataSourceDescriptor> listDescriptors() {
        // 1. 反射获取 classpath 上所有 Camel Component
        // 2. 翻译为 AgentDataSourceDescriptor
    }
}
```

### 5.8 全局审计日志（覆盖 Agent 全生命周期）— 新增

**设计目标**：一套基础设施，**所有** UseCase 操作的统一审计入口。

**资源类型与操作（覆盖 Agent 全生命周期）**：

| ResourceType | 涵盖的 UseCase / 操作 |
|--------------|---------------------|
| `AGENT` | Agent 创建/更新/删除/发布/执行/停止 |
| `AGENT_TEAM` | Agent 团队 CRUD |
| `DATA_SOURCE` | 数据源 CRUD + 启用/禁用/测试/调用 |
| `DATA_SOURCE_SCHEMA` | Schema CRUD + 自动发现 |
| `PERMISSION_STRATEGY` | 策略 CRUD + 模拟 |
| `TOOL` | HTTP 工具 / MCP 工具 / 系统工具 CRUD + 调用 |
| `TOOL_STRATEGY` | 工具策略 CRUD |
| `MODEL` | 模型 CRUD + 调用 |
| `MODEL_STRATEGY` | 模型策略 CRUD |
| `KNOWLEDGE_BASE` | 知识库 CRUD + 检索 |
| `RETRIEVAL_STRATEGY` | 检索策略 CRUD |
| `GUARDRAIL_STRATEGY` | 护栏策略 CRUD |
| `SKILL` | 技能 CRUD + 执行 |
| `WORKFLOW` | 工作流 CRUD + 执行 |
| `SUBAGENT` | 子智能体 CRUD + 调用 |
| `SESSION` | 会话 CRUD |
| `TENANT` | 租户 CRUD |
| `WORKSPACE` | 工作空间 CRUD |
| `MEMBER` | 成员 CRUD |
| `USER` | 登录/登出/密码修改 |
| `VECTOR_STORE` | 向量库 CRUD |
| `PROMPT_TEMPLATE` | 提示词模板 CRUD |
| `SCHEDULED_TASK` | 定时任务 CRUD + 触发 |
| `TRACE` | 执行链路查询/导出 |

**核心组件**：

```java
// domain/event/AuditEvent.java
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditEvent {
    private String id;                // UUID
    private String tenantId;
    private String workspaceId;
    private String actorId;           // user / agent / system 的 ID
    private String actorType;         // USER / AGENT / SYSTEM / SCHEDULED_TASK
    private String agentId;           // 关联 Agent（可选）
    private String sessionId;         // 关联 Session（可选）
    private String resourceType;      // 见上表
    private String resourceId;
    private String resourceName;      // 冗余便于显示
    private String action;            // CREATE / READ / UPDATE / DELETE / INVOKE / ...
    private String status;            // SUCCESS / FAILED / DENIED
    private Object request;           // 脱敏后
    private Object response;          // 脱敏后
    private String errorMessage;
    private Map<String, Object> metadata;  // IP / UA / traceId / ...
    private Long elapsedMs;
    private Instant createdAt;
}

public enum AuditResourceType {
    AGENT, AGENT_TEAM, DATA_SOURCE, DATA_SOURCE_SCHEMA, PERMISSION_STRATEGY,
    TOOL, TOOL_STRATEGY, MODEL, MODEL_STRATEGY, KNOWLEDGE_BASE, RETRIEVAL_STRATEGY,
    GUARDRAIL_STRATEGY, SKILL, WORKFLOW, SUBAGENT, SESSION, TENANT, WORKSPACE,
    MEMBER, USER, VECTOR_STORE, PROMPT_TEMPLATE, SCHEDULED_TASK, TRACE
}

public enum AuditAction {
    CREATE, READ, UPDATE, DELETE, ENABLE, DISABLE, PUBLISH, EXECUTE, STOP,
    INVOKE, TEST, INTROSPECT, SIMULATE, QUERY, EXPORT, ASSIGN, REVOKE,
    LOGIN, LOGOUT, TRIGGER, SYNC
}

public enum AuditStatus { SUCCESS, FAILED, DENIED }

public enum AuditActorType { USER, AGENT, SYSTEM, SCHEDULED_TASK }

// application/port/out/AuditLogger.java
public interface AuditLogger {
    /** 同步记录：用于关键操作（如支付、删除） */
    void log(AuditEvent event);
    /** 异步记录：用于一般操作（默认） */
    void logAsync(AuditEvent event);
}

// application/annotation/Audited.java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    String resourceType();                              // 必填：AuditResourceType 字符串
    String action() default "";                         // 留空时从方法名推断（create*/update*/delete*/invoke*/...）
    boolean includeArgs() default true;                 // 是否记录请求参数
    boolean includeResult() default false;              // 是否记录返回值（默认不记录）
    boolean maskSensitive() default true;               // 是否脱敏
}

// application/aspect/AuditAspect.java
@Aspect @Component @RequiredArgsConstructor
public class AuditAspect {
    private final AuditLogger auditLogger;
    private final SecurityContextHolder securityContext;
    
    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        long start = System.currentTimeMillis();
        String action = resolveAction(audited, pjp);
        try {
            Object result = pjp.proceed();
            auditLogger.logAsync(buildEvent(audited, action, pjp, result, "SUCCESS", null, start));
            return result;
        } catch (Exception e) {
            String status = isPermissionDenied(e) ? "DENIED" : "FAILED";
            auditLogger.logAsync(buildEvent(audited, action, pjp, null, status, e.getMessage(), start));
            throw e;
        }
    }
}
```

**UseCase 标注示例**（最小侵入式集成）：

```java
@Audited(resourceType = "AGENT", action = "CREATE")
public AgentOutput createAgent(CreateAgentCommand cmd) { ... }

@Audited(resourceType = "DATA_SOURCE", action = "INVOKE", includeResult = true)
public AgentDataSourceInvokeOutput invoke(String id, Object body) { ... }

@Audited(resourceType = "AGENT", action = "EXECUTE")
public ExecutionOutput execute(String agentId, String input) { ... }
```

**集成方式**：在每个 UseCase 方法上加 `@Audited` 注解 → 切面自动捕获入参、结果、异常 → 异步写 `audit_log` 表。

**批量 + 异步写入**：

```java
// infrastructure/store/audit/AuditRecorder.java
@Component
public class AuditRecorder implements AuditLogger {
    private final BlockingQueue<AuditEvent> queue = new LinkedBlockingQueue<>(10000);
    private final ScheduledExecutorService flusher = Executors.newSingleThreadScheduledExecutor();
    
    @PostConstruct
    void start() {
        // 每 1 秒或队列满 500 条时批量写入
        flusher.scheduleAtFixedRate(this::flush, 1, 1, SECONDS);
    }
    
    public void logAsync(AuditEvent event) { queue.offer(event); }
    
    private void flush() {
        List<AuditEvent> batch = new ArrayList<>();
        queue.drainTo(batch, 500);
        if (!batch.isEmpty()) repository.saveAll(batch);  // MyBatis batch insert
    }
}
```

**敏感字段自动脱敏**：

```java
public class AuditFieldMasker {
    private static final Set<String> SENSITIVE_KEYS = Set.of(
        "password", "token", "apiKey", "secret", "credential", "Authorization"
    );
    
    public Object mask(Object input) {
        if (input == null) return null;
        if (input instanceof String s) return maskString(s);
        // JSON 树遍历：递归替换 key 匹配 SENSITIVE_KEYS 的值
        return JsonTreeWalker.walk(input, (key, value) ->
            SENSITIVE_KEYS.contains(key) ? "***MASKED***" : value);
    }
}
```

**权限要求**：全局审计日志 API 需 `audit:read` 权限（仅 OWNER / ADMIN / AUDITOR）。

**租户级查询路径**（不挂 workspaceId）：审计日志是**租户级**资源，跨工作空间查询（一个租户的全局视图），但仍按 `tenant_id` 隔离。



### 6.1 agent_data_source（V100）

```sql
-- sql/V100__add_agent_data_source_tables.sql
CREATE TABLE IF NOT EXISTS agent_data_source (
    id                  VARCHAR(64)  PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    workspace_id        VARCHAR(64)  NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    protocol            VARCHAR(32)  NOT NULL,
    endpoint_uri        TEXT         NOT NULL,
    properties          TEXT,
    enabled             BOOLEAN      NOT NULL DEFAULT FALSE,
    status              VARCHAR(32)  NOT NULL DEFAULT 'DISABLED',
    last_error          TEXT,
    last_checked_at     TIMESTAMPTZ,
    permission_policy_id VARCHAR(64),                   -- ← 新增：FK to permission_strategy
    schema_id           VARCHAR(64),                   -- ← 新增：FK to data_source_schema
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, name)
);

CREATE INDEX idx_agent_ds_workspace ON agent_data_source (workspace_id);
CREATE INDEX idx_agent_ds_tenant    ON agent_data_source (tenant_id);
CREATE INDEX idx_agent_ds_enabled   ON agent_data_source (workspace_id, enabled);
CREATE INDEX idx_agent_ds_policy    ON agent_data_source (permission_policy_id);
```

### 6.2 data_source_schema（V101）— 新增

```sql
CREATE TABLE IF NOT EXISTS data_source_schema (
    id                   VARCHAR(64)  PRIMARY KEY,
    tenant_id            VARCHAR(64)  NOT NULL,
    workspace_id         VARCHAR(64)  NOT NULL,
    data_source_id       VARCHAR(64)  NOT NULL UNIQUE,    -- 一对一
    display_name         VARCHAR(255) NOT NULL,
    description          TEXT,
    last_introspected_at TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_schema_data_source ON data_source_schema (data_source_id);
CREATE INDEX idx_schema_workspace   ON data_source_schema (workspace_id);
```

### 6.3 data_source_table（V101）— 新增

```sql
CREATE TABLE IF NOT EXISTS data_source_table (
    id                  VARCHAR(64)  PRIMARY KEY,
    schema_id           VARCHAR(64)  NOT NULL,
    table_name          VARCHAR(255) NOT NULL,
    display_name        VARCHAR(255),
    description         TEXT,
    allowed_operations  VARCHAR(255) NOT NULL DEFAULT 'READ',  -- 逗号分隔：READ,INSERT,UPDATE,DELETE
    sample_data         TEXT,                                    -- JSON
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(schema_id, table_name)
);

CREATE INDEX idx_table_schema ON data_source_table (schema_id);
```

### 6.4 data_source_column（V101）— 新增

```sql
CREATE TABLE IF NOT EXISTS data_source_column (
    id           VARCHAR(64)  PRIMARY KEY,
    table_id     VARCHAR(64)  NOT NULL,
    name         VARCHAR(255) NOT NULL,
    type         VARCHAR(64)  NOT NULL,
    nullable     BOOLEAN      NOT NULL DEFAULT TRUE,
    is_primary   BOOLEAN      NOT NULL DEFAULT FALSE,
    default_val  VARCHAR(255),
    description  TEXT,
    is_pii       BOOLEAN      NOT NULL DEFAULT FALSE,
    pii_type     VARCHAR(32),
    column_order INTEGER      NOT NULL DEFAULT 0,
    UNIQUE(table_id, name)
);

CREATE INDEX idx_column_table ON data_source_column (table_id);
```

### 6.5 table_relationship（V101）— 新增

```sql
CREATE TABLE IF NOT EXISTS table_relationship (
    id              VARCHAR(64)  PRIMARY KEY,
    source_table_id VARCHAR(64)  NOT NULL,
    target_table_id VARCHAR(64)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(32)  NOT NULL,           -- ONE_TO_ONE / ONE_TO_MANY / MANY_TO_MANY
    source_column   VARCHAR(255) NOT NULL,
    target_column   VARCHAR(255) NOT NULL,
    description     TEXT
);

CREATE INDEX idx_rel_source ON table_relationship (source_table_id);
CREATE INDEX idx_rel_target ON table_relationship (target_table_id);
```

### 6.6 permission_strategy（V102）— 新增

```sql
CREATE TABLE IF NOT EXISTS permission_strategy (
    id                          VARCHAR(64)  PRIMARY KEY,
    tenant_id                   VARCHAR(64)  NOT NULL,
    workspace_id                VARCHAR(64)  NOT NULL,
    name                        VARCHAR(255) NOT NULL,
    description                 TEXT,
    allowed_roles               VARCHAR(255)  DEFAULT '',             -- 逗号分隔角色（OWNER/ADMIN/DEVELOPER/VIEWER）
    allowed_operations          VARCHAR(64)   NOT NULL DEFAULT 'READ',-- 逗号分隔 CRUD：CREATE,READ,UPDATE,DELETE
    protocol_blocklist          VARCHAR(255)  DEFAULT '',             -- 逗号分隔协议
    dangerous_sql_block         BOOLEAN       NOT NULL DEFAULT TRUE,
    require_approval_for        VARCHAR(255)  DEFAULT '',             -- 逗号分隔操作
    table_permissions           TEXT,                                -- JSON: {tableName: [ops]}
    rate_limit_per_minute       INTEGER       NOT NULL DEFAULT 0,    -- 0 = 不限
    rate_limit_per_hour         INTEGER       NOT NULL DEFAULT 0,
    audit_log_enabled           BOOLEAN       NOT NULL DEFAULT TRUE,
    audit_log_retention_days    INTEGER       NOT NULL DEFAULT 90,
    pii_masking_on_result       BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, name)
);

CREATE INDEX idx_perm_workspace ON permission_strategy (workspace_id);
```

### 6.7 audit_log（V103）— **全局审计日志**（覆盖 Agent 全生命周期）

```sql
-- 全局审计日志：覆盖 Agent 全生命周期的所有操作
-- 资源类型: AGENT / DATA_SOURCE / TOOL / MODEL / KNOWLEDGE_BASE / SKILL / WORKFLOW /
--          SESSION / TENANT / WORKSPACE / MEMBER / USER / STRATEGY ...
CREATE TABLE IF NOT EXISTS audit_log (
    id              VARCHAR(64)  PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    workspace_id    VARCHAR(64),                                   -- 资源级审计可为空
    actor_id        VARCHAR(64),                                   -- 主体 ID（user/agent/system）
    actor_type      VARCHAR(32)  NOT NULL,                         -- USER / AGENT / SYSTEM / SCHEDULED_TASK
    agent_id        VARCHAR(64),                                   -- 关联 Agent
    session_id      VARCHAR(64),                                   -- 关联 Session
    resource_type   VARCHAR(64)  NOT NULL,                          -- AGENT / DATA_SOURCE / TOOL / ...
    resource_id     VARCHAR(64),                                   -- 资源 ID
    resource_name   VARCHAR(255),                                  -- 资源名（冗余便于显示）
    action          VARCHAR(64)  NOT NULL,                          -- CREATE / READ / UPDATE / DELETE / INVOKE / ENABLE / DISABLE / PUBLISH / EXECUTE / STOP ...
    status          VARCHAR(32)  NOT NULL,                          -- SUCCESS / FAILED / DENIED
    request         TEXT,                                          -- 请求内容（脱敏后）
    response        TEXT,                                          -- 响应内容（脱敏后）
    error_message   TEXT,
    metadata        TEXT,                                          -- JSON 扩展：IP/UA/traceId/session 等
    elapsed_ms      BIGINT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_tenant_created  ON audit_log (tenant_id, created_at DESC);
CREATE INDEX idx_audit_workspace       ON audit_log (workspace_id, created_at DESC);
CREATE INDEX idx_audit_actor           ON audit_log (actor_id, created_at DESC);
CREATE INDEX idx_audit_resource        ON audit_log (resource_type, resource_id, created_at DESC);
CREATE INDEX idx_audit_agent           ON audit_log (agent_id, created_at DESC);
CREATE INDEX idx_audit_session         ON audit_log (session_id, created_at DESC);
CREATE INDEX idx_audit_status          ON audit_log (status, created_at DESC);
CREATE INDEX idx_audit_action          ON audit_log (action, created_at DESC);
```

## 7. API 设计

### 7.1 数据源 CRUD + 调试

| Method | Path | 说明 |
|--------|------|------|
| GET | `/api/v1/workspaces/{wsId}/agent-data-sources` | 列表 |
| POST | `/api/v1/workspaces/{wsId}/agent-data-sources` | 创建（默认 disabled） |
| GET | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}` | 详情 |
| PATCH | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}` | 更新（不切换 enabled） |
| POST | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}/enable` | 启用 |
| POST | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}/disable` | 禁用 |
| DELETE | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}` | 删除 |
| POST | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}/test` | **调试：测试连接** |
| POST | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}/invoke` | **调试：调用并返回结果** |

### 7.2 协议元数据

| Method | Path | 说明 |
|--------|------|------|
| GET | `/api/v1/workspaces/{wsId}/agent-data-source-protocols` | 列出所有支持的协议描述符 |

### 7.3 数据源 Schema（表结构元数据）— 新增

| Method | Path | 说明 |
|--------|------|------|
| GET | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}/schema` | 获取 Schema（包含表/字段/关联） |
| PUT | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}/schema` | 整体替换（手动配置） |
| POST | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}/schema/introspect` | **自动发现**（通过 Camel JDBC 查 INFORMATION_SCHEMA） |
| POST | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}/schema/tables` | 新增表 |
| PATCH | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}/schema/tables/{tableId}` | 更新表（操作权限/字段） |
| DELETE | `/api/v1/workspaces/{wsId}/agent-data-sources/{id}/schema/tables/{tableId}` | 删除表 |

### 7.4 权限策略（第 5 个策略）— 新增

| Method | Path | 说明 |
|--------|------|------|
| GET | `/api/v1/workspaces/{wsId}/permission-strategies` | 列表 |
| POST | `/api/v1/workspaces/{wsId}/permission-strategies` | 创建 |
| GET | `/api/v1/workspaces/{wsId}/permission-strategies/{id}` | 详情 |
| PATCH | `/api/v1/workspaces/{wsId}/permission-strategies/{id}` | 更新 |
| DELETE | `/api/v1/workspaces/{wsId}/permission-strategies/{id}` | 删除 |
| POST | `/api/v1/workspaces/{wsId}/permission-strategies/{id}/simulate` | **模拟**：给定 userId + dataSourceId + body，预演是否被允许 |

### 7.5 审计日志（**全局**，覆盖 Agent 全生命周期）— 新增

| Method | Path | 说明 |
|--------|------|------|
| GET | `/api/v1/audit-logs?tenantId=&workspaceId=&resourceType=&resourceId=&actorId=&action=&status=&from=&to=&page=&size=` | **租户级分页查询**（多资源类型过滤） |
| GET | `/api/v1/audit-logs/{id}` | 详情（含 request/response） |
| GET | `/api/v1/audit-logs/export?from=&to=&format=csv\|json` | 导出 |
| GET | `/api/v1/audit-logs/resource-types` | 列出所有可审计资源类型（如 AGENT/DATA_SOURCE/TOOL/...） |
| GET | `/api/v1/audit-logs/actions` | 列出所有可审计操作（CREATE/READ/UPDATE/DELETE/INVOKE/...） |
| GET | `/api/v1/audit-logs/stats?groupBy=resourceType\|action\|actor` | 统计聚合 |

**权限要求**：仅 OWNER / ADMIN / AUDITOR 角色可访问（`audit:read` 权限）。

### 7.6 MCP 通道（跨进程）

| Method | Path | 说明 |
|--------|------|------|
| GET | `/mcp/agent-data-sources/{wsId}/sse` | MCP SSE 通道 |
| POST | `/mcp/agent-data-sources/{wsId}/message` | MCP JSON-RPC 消息 |

## 8. 前端设计：遵循现有浮动按钮规范

### 8.1 复用现有 `FloatingAddButton`

`AgentHubLayout.vue` 已实现 `FloatingAddButton` 组件（`src/main/web/src/components/FloatingAddButton.vue`），通过 `showAddButton` 计算属性控制哪些页面显示"+"号按钮，点击后触发 `window.dispatchEvent(new CustomEvent('global-add'))`。

**修改 1：在 `AgentHubLayout.vue` 中添加路径匹配**

```ts
// 在 showAddButton computed 中追加：
const showAddButton = computed(() => {
  const path = route.path
  return path.includes('strategies') ||
         // ... 现有路径 ...
         path.includes('agent-data-sources')   // ← 新增
})
```

**修改 2：`StrategyManagementView.vue` 新增第 5 个 Tab：权限策略**

```vue
<div class="tabs">
  <button :class="{ active: activeTab === 'retrieval' }" @click="activeTab = 'retrieval'">检索策略</button>
  <button :class="{ active: activeTab === 'tool' }" @click="activeTab = 'tool'">工具策略</button>
  <button :class="{ active: activeTab === 'model' }" @click="activeTab = 'model'">模型策略</button>
  <button :class="{ active: activeTab === 'guardrail' }" @click="activeTab = 'guardrail'">护栏策略</button>
  <button :class="{ active: activeTab === 'permission' }" @click="activeTab = 'permission'">权限策略</button>  <!-- ← 新增 -->
</div>

<div v-else-if="activeTab === 'permission'" class="tab-content">
  <PermissionStrategyPanel :key="componentKey" />
</div>
```

`PermissionStrategyPanel.vue` 字段配置：

```
┌─ 权限策略配置 ──────────────────────────────┐
│  策略名称 / 描述                              │
│                                              │
│  ── ACL ──                                  │
│  允许角色: [✓OWNER] [✓ADMIN] [✓DEVELOPER] [ VIEWER] │
│                                              │
│  ── 操作级别（细粒度 CRUD，多选） ──         │
│  [✓] CREATE (增)  [✓] READ (查)             │
│  [ ] UPDATE (改)  [ ] DELETE (删)             │
│                                              │
│  ── 协议黑名单 ──                            │
│  禁用的协议: [SMTP] [FTP] [+ 添加]            │
│                                              │
│  ── 危险操作拦截 ──                          │
│  [✓] 拦截危险 SQL 关键字（DELETE/DROP/...）  │
│  需要审批的操作: [DELETE] [DROP] [+ 添加]      │
│                                              │
│  ── 速率限制 ──                              │
│  每分钟: [100] 次   每小时: [1000] 次         │
│                                              │
│  ── 审计日志 ──                              │
│  [✓] 启用审计日志（写入全局 audit_log 表）   │
│  保留天数: [90] 天                            │
│                                              │
│  ── 结果脱敏 ──                              │
│  [ ] 启用 PII 脱敏（联动护栏策略）            │
│                                              │
│  [保存]                                       │
└──────────────────────────────────────────────┘
```

### 8.2 页面 `AgentDataSourceView.vue`（含 Schema 面板）

布局严格遵循 `McpToolView.vue` / `VectorStoreConfigView.vue` 模式：

```vue
<template>
  <section class="grid glass-float">
    <!-- 页面头部（与 McpToolView 一致） -->
    <div class="page-header">
      <div>
        <h2>Agent 数据源</h2>
        <p class="muted">管理 Agent 数据源连接配置，支持 Camel 300+ Component。</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>

    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>

    <template v-else>
      <!-- 协议筛选 + 启用状态筛选 -->
      <div class="header-filters">
        <CustomSelect v-model="filterProtocol" :options="protocolOptions" />
        <CustomSelect v-model="filterEnabled" :options="enabledOptions" />
      </div>

      <!-- 创建/编辑弹窗 -->
      <ModalDialog v-model:visible="showCreateForm" :title="editingId ? '编辑数据源' : '创建数据源'" 
                   @confirm="submitConfig" :confirm-text="editingId ? '更新' : '创建'">
        <form class="field-grid">
          <label class="field">
            <span>名称 *</span>
            <input v-model="form.name" required />
          </label>
          <label class="field">
            <span>协议 *</span>
            <select v-model="form.protocol" required @change="onProtocolChange">
              <option v-for="p in protocols" :key="p.protocol" :value="p.protocol">{{ p.displayName }}</option>
            </select>
          </label>
          <!-- 动态字段：根据协议描述符渲染 -->
          <label v-for="f in currentDescriptor?.fields" :key="f.name" class="field">
            <span>{{ f.description }}<span v-if="f.required"> *</span></span>
            <input v-model="form.dynamicFields[f.name]" 
                   :type="f.type === 'password' ? 'password' : f.type === 'integer' ? 'number' : 'text'"
                   :placeholder="f.placeholder" :required="f.required" />
          </label>
          <label class="field">
            <span>URI 预览</span>
            <input :value="generatedUri" readonly />
          </label>
          <label class="field" v-if="editingId">
            <span>启用状态</span>
            <select v-model="form.enabled">
              <option :value="true">启用</option>
              <option :value="false">禁用</option>
            </select>
          </label>
        </form>
      </ModalDialog>

      <!-- 调试弹窗 -->
      <ModalDialog v-model:visible="showDebugDialog" :title="'调试：' + debuggingSource?.name">
        <div v-if="debuggingSource">
          <p>协议：<span class="tag">{{ debuggingSource.protocol }}</span></p>
          <p>URI：<code>{{ debuggingSource.endpointUri }}</code></p>
          <label class="field" style="grid-column: 1 / -1">
            <span>Body</span>
            <textarea v-model="debugForm.body" rows="4" placeholder='{"key": "value"}' />
          </label>
          <label class="field" style="grid-column: 1 / -1">
            <span>Headers（JSON 格式）</span>
            <textarea v-model="debugForm.headers" rows="2" placeholder='{"maxRows": 10}' />
          </label>
        </div>
        <template #footer>
          <CustomButton type="ghost" @click="showDebugDialog = false">关闭</CustomButton>
          <CustomButton type="primary" :disabled="debugRunning" @click="executeInvoke">执行调用</CustomButton>
        </template>
        <div v-if="debugResult" class="debug-result" :class="debugResult.success ? 'success' : 'error'">
          <p><strong>状态：</strong>{{ debugResult.success ? '✓ 成功' : '✗ 失败' }}</p>
          <p><strong>耗时：</strong>{{ debugResult.elapsedMs }}ms</p>
          <pre>{{ JSON.stringify(debugResult.data, null, 2) }}</pre>
        </div>
      </ModalDialog>

      <!-- 列表 -->
      <article class="table-card float-effect">
        <table>
          <thead>
            <tr>
              <th>名称</th>
              <th>协议</th>
              <th>URI</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in filteredSources" :key="s.id">
              <td><strong>{{ s.name }}</strong><div class="muted">{{ s.id }}</div></td>
              <td><span class="tag">{{ s.protocol }}</span></td>
              <td><code class="uri-cell">{{ s.endpointUri }}</code></td>
              <td>
                <span :class="['tag', s.enabled ? 'tag-success' : 'tag-error']">
                  {{ s.enabled ? '启用' : '禁用' }}
                </span>
                <span v-if="s.status === 'ERROR'" class="tag tag-error" :title="s.lastErrorMessage">异常</span>
              </td>
              <td>{{ formatDateTime(s.createdAt) }}</td>
              <td>
                <div class="chip-row">
                  <CustomButton type="ghost" @click="toggleEnabled(s)">
                    {{ s.enabled ? '禁用' : '启用' }}
                  </CustomButton>
                  <CustomButton type="ghost" @click="startEdit(s)">编辑</CustomButton>
                  <CustomButton type="ghost" @click="testConnection(s)">测试</CustomButton>
                  <CustomButton type="ghost" @click="openDebug(s)">调试</CustomButton>
                  <CustomButton type="ghost" @click="handleDelete(s.id)">删除</CustomButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { showConfirm } from '@/utils/confirm'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import { formatDateTime } from '@/common/format'
import { useWorkspaceStore } from '@/store/workspace-store'
import {
  listAgentDataSources, getAgentDataSource, createAgentDataSource,
  updateAgentDataSource, deleteAgentDataSource, enableAgentDataSource,
  disableAgentDataSource, testAgentDataSource, invokeAgentDataSource,
  listAgentDataSourceProtocols
} from '@/api/agent-data-source-api'
import type { 
  AgentDataSource, AgentDataSourceProtocol, AgentDataSourceDescriptor,
  AgentDataSourceTestOutput, AgentDataSourceInvokeOutput 
} from '@/types/agentDataSource'

const store = useWorkspaceStore()
const error = ref('')
const sources = ref<AgentDataSource[]>([])
const protocols = ref<AgentDataSourceDescriptor[]>([])
const editingId = ref<string | null>(null)
const showCreateForm = ref(false)
const showDebugDialog = ref(false)
const debuggingSource = ref<AgentDataSource | null>(null)
const debugResult = ref<AgentDataSourceInvokeOutput | null>(null)
const debugRunning = ref(false)

// 筛选
const filterProtocol = ref<string>('')
const filterEnabled = ref<string>('')
const filteredSources = computed(() => {
  return sources.value.filter(s => {
    if (filterProtocol.value && s.protocol !== filterProtocol.value) return false
    if (filterEnabled.value === 'true' && !s.enabled) return false
    if (filterEnabled.value === 'false' && s.enabled) return false
    return true
  })
})

// 表单
const form = reactive({
  name: '',
  description: '',
  protocol: '' as AgentDataSourceProtocol | '',
  enabled: false,
  dynamicFields: {} as Record<string, string>
})

const currentDescriptor = computed(() => 
  protocols.value.find(p => p.protocol === form.protocol)
)

const generatedUri = computed(() => {
  if (!currentDescriptor.value || !form.protocol) return ''
  // 根据 syntaxHint + dynamicFields 拼装 URI
  return buildUriFromFields(currentDescriptor.value, form.dynamicFields)
})

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

onMounted(async () => {
  await loadProtocols()
  await loadSources()
  
  // 监听全局新增事件（FloatingAddButton 触发）
  window.addEventListener('global-add', handleGlobalAdd)
})

onUnmounted(() => {
  window.removeEventListener('global-add', handleGlobalAdd)
})

watch(() => [store.tenantId, store.workspaceId], async () => {
  await loadSources()
})

function handleGlobalAdd() {
  editingId.value = null
  Object.assign(form, { name: '', description: '', protocol: '', enabled: false, dynamicFields: {} })
  showCreateForm.value = true
}

// ... 其余方法（loadSources, submitConfig, startEdit, testConnection, openDebug, executeInvoke, handleDelete, toggleEnabled）
</script>
```

**页面顶部 Tabs 切换**（基本信息 / Schema / 权限策略 / 审计日志）：

```vue
<template v-else>
  <div class="page-tabs">
    <button :class="{ active: activeTab === 'basic' }" @click="activeTab = 'basic'">基本信息</button>
    <button :class="{ active: activeTab === 'schema' }" @click="activeTab = 'schema'">Schema</button>
    <button :class="{ active: activeTab === 'policy' }" @click="activeTab = 'policy'">权限策略</button>
    <button :class="{ active: activeTab === 'audit' }" @click="activeTab = 'audit'">审计日志</button>
  </div>

  <SchemaPanel v-if="activeTab === 'schema'" :data-source="currentSource" />
  <PermissionBindingPanel v-else-if="activeTab === 'policy'" :data-source="currentSource" />
  <AuditLogPanel v-else-if="activeTab === 'audit'" :data-source="currentSource" />
  
  <!-- 基本信息列表（原有内容） -->
  <article v-show="activeTab === 'basic'" class="table-card float-effect">
    <!-- ... 表格 + 操作列 ... -->
  </article>
</template>
```

**Schema 面板**（`SchemaPanel.vue`）布局：

```
┌─ Schema 管理 ──────────────────────────────────────────┐
│  当前数据源: orders-db (JDBC)                           │
│  [+ 新增表] [🔄 自动发现] [导出] [导入]                │
├────────────────────────────────────────────────────────┤
│  表名        | 允许操作          | 字段 | 关联 | 操作   │
│  orders      | READ,INSERT       |  12  |  2   | [⋯]  │
│  customers   | READ              |   8  |  1   | [⋯]  │
│  products    | READ,UPDATE       |  15  |  3   | [⋯]  │
├────────────────────────────────────────────────────────┤
│  点击 [+] 展开：                                         │
│  ┌─ orders 表详情 ───────────────────────────────────┐  │
│  │  显示名: 订单表                                     │  │
│  │  字段:                                              │  │
│  │    id              INT     PK    NOT NULL          │  │
│  │    customer_id     INT     FK→customers.id  PII:NO  │  │
│  │    total           DECIMAL              PII:NO      │  │
│  │    phone           VARCHAR             PII:PHONE    │  │
│  │  关联:                                              │  │
│  │    - orders.customer_id → customers.id (MANY_TO_ONE)│  │
│  │  样例数据 (3 rows):                                 │  │
│  │    [{...}, {...}, {...}]                            │  │
│  │  允许操作: [✓]READ [✓]INSERT [ ]UPDATE [ ]DELETE    │  │
│  └────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

**关键设计点**：
- ✅ **顶部使用 `<div class="page-header">`**，与 McpToolView 一致
- ✅ **新增按钮由 `FloatingAddButton`（布局层）提供**，本页不重复实现
- ✅ **监听 `global-add` 事件**触发创建弹窗
- ✅ **列表 + 操作列**结构与其他配置页一致
- ✅ **测试 / 调试 / 启用 / 禁用 / 删除**操作在行内 `chip-row`

### 8.3 路由

```ts
// src/main/web/src/router/index.ts 修改
{ path: 'agent-data-sources', component: () => import('@/views/agenthub/AgentDataSourceView.vue') },
{ path: 'audit-logs', component: () => import('@/views/agenthub/AuditLogView.vue') }  // 新增：全局审计日志
```

### 8.4 API 客户端

```ts
// src/main/web/src/api/agent-data-source-api.ts
export async function listAgentDataSources(workspaceId: string): Promise<AgentDataSource[]>
export async function getAgentDataSource(workspaceId: string, id: string): Promise<AgentDataSource>
export async function createAgentDataSource(workspaceId: string, req: CreateAgentDataSourceRequest): Promise<AgentDataSource>
export async function updateAgentDataSource(workspaceId: string, id: string, req: UpdateAgentDataSourceRequest): Promise<AgentDataSource>
export async function deleteAgentDataSource(workspaceId: string, id: string): Promise<void>
export async function enableAgentDataSource(workspaceId: string, id: string): Promise<AgentDataSource>
export async function disableAgentDataSource(workspaceId: string, id: string): Promise<AgentDataSource>
export async function testAgentDataSource(workspaceId: string, id: string): Promise<AgentDataSourceTestOutput>
export async function invokeAgentDataSource(workspaceId: string, id: string, body: string, headers: Record<string, unknown>): Promise<AgentDataSourceInvokeOutput>
export async function listAgentDataSourceProtocols(workspaceId: string): Promise<AgentDataSourceDescriptor[]>
```

### 8.5 类型定义

```ts
// src/main/web/src/types/agentDataSource.ts
export type AgentDataSourceProtocol = 'JDBC' | 'JMS' | 'KAFKA' | 'HTTP' | 'HTTPS' | 'FTP' | 'SFTP' | 'FILE' | 'MAIL' | 'MONGODB' | 'REDIS' | 'SQL' | 'REST' | 'DIRECT' | 'TIMER'

export type AgentDataSourceStatus = 'DISABLED' | 'ENABLED' | 'CONNECTING' | 'ERROR'

export interface AgentDataSource {
  id: string
  name: string
  description: string
  protocol: AgentDataSourceProtocol
  endpointUri: string
  propertiesJson: string
  enabled: boolean
  status: AgentDataSourceStatus
  lastErrorMessage?: string
  lastCheckedAt?: string
  createdAt: string
  updatedAt: string
}

export interface AgentDataSourceField {
  name: string
  type: 'string' | 'integer' | 'boolean' | 'password'
  required: boolean
  defaultValue?: string
  description: string
  placeholder?: string
}

export interface AgentDataSourceDescriptor {
  protocol: AgentDataSourceProtocol
  scheme: string
  displayName: string
  description: string
  syntaxHint: string
  fields: AgentDataSourceField[]
}

export interface AgentDataSourceTestOutput {
  success: boolean
  elapsedMs: number
  message: string
  details?: Record<string, unknown>
}

export interface AgentDataSourceInvokeOutput {
  success: boolean
  data: unknown
  elapsedMs: number
  exchangeId?: string
  errorMessage?: string
}
```

### 8.6 全局审计日志页 `AuditLogView.vue`

```
┌─ 审计日志 ─────────────────────────────────────────────────────────┐
│  租户级查询：所有工作空间、所有资源类型、所有操作                       │
├────────────────────────────────────────────────────────────────────┤
│  筛选器:                                                           │
│    [资源类型 ▼] [资源ID] [操作 ▼] [状态 ▼] [执行者] [起始日期] [结束日期] [搜索] │
├────────────────────────────────────────────────────────────────────┤
│  统计: 总 1,234 条  成功 1,180  失败 42  拒绝 12  (近 24h)         │
│  按资源类型: AGENT 580 | DATA_SOURCE 320 | TOOL 234 | ...          │
├────────────────────────────────────────────────────────────────────┤
│  时间                | 执行者       | 资源类型    | 资源            | 操作    | 状态   | 耗时 | 详情 │
│  2026-06-06 10:30:15 | user@demo   | AGENT       | customer-bot   | EXECUTE | 成功   | 1.2s | [>] │
│  2026-06-06 10:29:58 | agent-001   | DATA_SOURCE | orders-db      | INVOKE  | 成功   | 0.3s | [>] │
│  2026-06-06 10:29:50 | user@demo   | AGENT       | customer-bot   | PUBLISH | 成功   | 0.1s | [>] │
│  2026-06-06 10:29:32 | user@demo   | DATA_SOURCE | orders-db      | INVOKE  | 拒绝   | 0.0s | [>] │
│  2026-06-06 10:28:00 | system      | SESSION     | sess-001       | CREATE  | 成功   | 0.0s | [>] │
│  ...                                                                │
├────────────────────────────────────────────────────────────────────┤
│  [< 上一页]  1 / 25  [下一页 >]   [导出 CSV] [导出 JSON]            │
└────────────────────────────────────────────────────────────────────┘

详情抽屉（点击 [>]）:
┌─ 审计日志详情 ────────────────────────┐
│  ID: 64a1b2c3d4e5f6                    │
│  时间: 2026-06-06 10:30:15.234          │
│  资源: AGENT / customer-bot             │
│  操作: EXECUTE                          │
│  状态: 成功                             │
│  执行者: user@demo (USER)              │
│  关联: agent=customer-bot, session=s1  │
│  耗时: 1234ms                           │
│  ── 请求 (脱敏) ──                    │
│  { "input": "查询订单状态" }            │
│  ── 响应 (脱敏) ──                    │
│  { "output": "订单 #123 已发货" }      │
│  ── 元数据 ──                          │
│  { "ip": "10.0.0.5", "ua": "Mozilla...", "traceId": "abc123" } │
└────────────────────────────────────────┘
```

**关键设计点**：
- 租户级查询：所有工作空间的所有资源统一展示（受 `audit:read` 权限保护）
- 24+ 资源类型多维筛选（顶部筛选器 + 列固定排序）
- 实时统计（成功/失败/拒绝 计数 + 资源类型分布）
- 详情抽屉含完整 request/response（已脱敏）
- 支持 CSV / JSON 导出
- 长任务分页 + 索引优化（`tenant_id + created_at DESC` 复合索引）

## 9. 启动时恢复流程

```
SpringApplication.run()
  ↓
CamelAutoConfiguration 注册 CamelDataSourceRuntime Bean
  ↓
AgentDataSourceBootstrapUseCase.onApplicationReady()
  ↓
1. 扫描 DB 中所有 enabled=true 的 AgentDataSource
  ↓
2. 调 AgentDataSourcePort.bootstrap(...) → CamelAgentDataSourceAdapter
  ↓
3. 内部：
   a. 按 workspaceId 分组
   b. 创建/复用 CamelContext
   c. 注册 direct: 路由
  ↓
4. 发布 AgentDataSourceChangedEvent
  ↓
5. 监听器：
   a. AgentDataSourceToolCallbackProvider 刷新缓存
   b. AgentDataSourceMcpServerHandler 重新注册 MCP Tool
  ↓
6. 失败不阻塞启动
```

## 10. 边界情况

| # | 场景 | 期望 |
|---|------|------|
| 1 | 创建时 URI 非法 | 400 |
| 2 | 启用时 Component class 未在 classpath | 500 + 提示"协议 <X> 未安装" |
| 3 | 启用时 endpoint 物理不可达 | 仍 enabled=true，调用返回 ERROR |
| 4 | 删除正在被 MCP 调用的数据源 | 拒绝 + 提示"正在被 X 个会话使用" |
| 5 | 同一 workspace 创建同名 | 409 |
| 6 | 修改 endpointUri 时处于 enabled | 拒绝，必须先 disable |
| 7 | 直接 invoke 已禁用的数据源 | 400 |
| 8 | 工作区被删除 | 监听 → Port.disable + Repository.delete |
| 9 | Agent 调用时 workspace 隔离 | 跨 workspace 403 |
| 10 | 调用时超时 | 5s 默认，超时返回 ERROR |
| 11 | 启动恢复时某数据源 enable 失败 | 跳过，status=ERROR，不影响其他 |
| 12 | 外部 MCP 客户端连接时 workspace 不存在 | 400 |

## 11. 检查清单

- [ ] 每个方法 ≤10 行、≤3 参数
- [ ] 分层依赖正确（domain/application/api 完全不引用 Camel）
- [ ] `infrastructure.camel.*` 之外禁止引用 `org.apache.camel.*`
- [ ] `infrastructure.tools.data_source.*` 禁止引用 `org.apache.camel.*`
- [ ] 所有方法有中文 Javadoc
- [ ] 无 record / @Builder / @Autowired
- [ ] 无通配符 import（`domain.exception.*` 除外）
- [ ] DTO 转换统一使用 `BeanUtil.copyProperties`
- [ ] 启动恢复失败不阻塞应用启动
- [ ] ArchUnit 17/19 通过（新增 2 条 Camel 引用约束）
- [ ] Controller 集成测试覆盖 100%

## 12. 工期压缩：5 个工作块（一次性完成）

> **策略**：合并相关步骤，删除可选项，复用现有模式，单次会话内完成所有实现 + 验证。

| 工作块 | 内容 | 关键产出 |
|--------|------|---------|
| **Block 1：领域 + DB + Application 端口** | 12 个 domain 文件（含 AuditEvent + 4 个 enum）+ 12 个 application 文件（含 @Audited 注解 + AuditAspect + AuditLogger 端口）+ 5 个 DB 文件 + 5 个 Mapper/Repository + 4 个 SQL 文件（V100-V103） | 完整领域模型 + Port 接口 + 持久化层 + 审计基础设施 |
| **Block 2：Camel 引擎 + UseCase + 权限服务** | 5 个 camel 文件 + 5 个 UseCase + 1 个 PermissionService + 1 个 RateLimiter + 1 个 AuditRecorder（实现 AuditLogger）+ 1 个 AutoConfiguration | `CamelAgentDataSourceAdapter`（集成权限 + 全局审计）+ 启动恢复 |
| **Block 3：API 层 + 集成测试** | 5 个 api/dto + 1 个 mapper + 5 个 Controller（含 AuditLogController 租户级）+ 3 个 IntegrationTest | REST API 端到端通过 |
| **Block 4：双路径集成** | 3 个 tools/data_source 文件 + 3 个 web 文件 + 2 个 Listener | 同进程 ToolCallback + 跨进程 McpServer 两条路径 |
| **Block 5：前端 + ArchUnit** | 2 个 View（含 AuditLogView）+ 4 个组件 + 4 个 API 客户端 + 4 个 types + 4 处修改 + ArchUnit 19/19 | 前端页面（数据源 + 全局审计日志）+ 策略管理第 5 个 Tab + 架构校验 |

**总产出**：约 78 个新建 + 6 个修改（含 11 个测试 + 4 个 SQL）

**执行原则**：
- 每个 Block 完成后立即跑 `gradle compileJava` 验证编译
- Block 3 完成后跑 `gradle test --tests "*AgentDataSourceControllerIntegrationTest*"` 和 `*AuditLogControllerIntegrationTest*` 验证
- Block 5 完成后跑 `gradle test --tests "com.agenthub.test.architecture.AgentHubCleanArchitectureTest"` 验证 ArchUnit 19/19

## 13. 风险与依赖

| 风险 | 缓解 |
|------|------|
| `camel-spring-boot` 与 Spring Boot 4.1.0-M2 不兼容 | **不引入**；只引 `camel-core` + `camel-main` + 各 component |
| 凭据明文存储 | 第一期明文 + UI 警告 |
| 启动恢复时间 | 并行按 workspace 启动 |
| ToolCallback 缓存与 DB 不同步 | 事件驱动自动刷新 |
| 外部 MCP 客户端断线 | SSE 心跳 + 自动重连（SDK 内置） |
| Camel Component 引入导致 jar 膨胀 | 15 个核心 Component，按需扩展 |
| 跨进程 MCP 权限 | workspaceId 路径参数 + 鉴权（沿用现有 X-Tenant-Id 头） |

## 14. 验证策略

- **集成测试**：`AgentDataSourceControllerIntegrationTest` 覆盖 9 个端点
- **UseCase 单元测试**：`AgentDataSourceUseCaseUnitTest`（Mock Repository + Port）
- **ArchUnit**：`gradle test --tests "com.agenthub.test.architecture.AgentHubCleanArchitectureTest"` → 19/19
  - 新增：`com.agenthub.infrastructure.camel.*` 之外禁止引用 `org.apache.camel.*`
  - 新增：`com.agenthub.infrastructure.tools.data_source.*` 禁止引用 `org.apache.camel.*`

## 15. 不在本次范围内

- 完整 Camel Route DSL / EIP 模式 / 动态路由热加载
- 自定义 Component 编写
- 凭据加密
- 调用结果缓存、熔断器
- 其他 `AgentDataSource` 实现（端口已为后续扩展预留）

## 完成情况

- ArchUnit: 19/19 ✅
- 集成测试: 26/26 ✅（ArchTest 10 + AgentDataSource 10 + Component 2 + Schema 6 + Permission 4 + AuditLog 4）
- 创建文件: 约 78 个（含 11 个测试 + 4 个 SQL + Schema + 权限策略 + 全局审计日志）
- 修改文件: 6 个

## 反思

- 遇到的问题：
  1. `MyBatis save(id 已设置但记录不存在)`：原 `updateById` 静默 0 行 → 改为先 `selectById` 再决定 `insert/update`。
  2. `BeanUtil.copyProperties` 跨 `Map<String, Set<Enum>>` 字段抛 "Unsupported toMap value type" → 改用手动字段映射。
  3. `Hutool StringValue(String)` 当 arg 为 null 抛 NPE → `AuditLogEntity.workspaceId` 加 `@TableField(exist = false)`。
  4. `TenantContextLineHandler.getTenantId` 在 workspaceId/tenantId 为 null 时抛 NPE → 改用 `findTenantThreadContext().orElseThrow`。
  5. `AuditAspect` 跨层访问 tenant → 新建 `TenantContextPort`（application port）+ `TenantContextPortAdapter`（infrastructure adapter）。
  6. `MybatisDataSourceSchemaRepository` 只持久化 schema，表的 `tables` 字段被忽略 → 重写为加载/保存表 + 字段（3 个 mapper 协同）。
  7. `GET schema` 在未创建时返回 404 → 改为自动创建空 schema。
  8. **Camel JDBC NoSuchBeanException**：Camel `JdbcComponent.createEndpoint` 从 `jdbc:` 后的 URI 查找 DataSource bean，但未注册 → 注入 `JdbcDataSourceFactory` + `ctx.getRegistry().bind(lookupKey, ds)`。
  9. **DirectConsumerNotAvailableException**：`bootstrap()` 只在显式调用时注册路由，`/test` 端点直接调用 `adapter.test()` 时路由未注册 → 新增 `ensureBootstrapped()` 懒加载方法。
  10. **jdbcLookupKey 与 `?` 查询参数不匹配**：`JdbcComponent.createEndpoint` 把 `?` 后部分当查询参数剥离 → `jdbcLookupKey()` 去除 `?` 后部分。
  11. **`toD(endpointUri)` 误把 body 当 URI**：路由 `toD` 把 body "ping" 当 URI 拼成 `jdbc:ping` 然后当 SQL 执行 → `ping()` 改用 `DataSource.isValid(5)` 直接测试，绕过路由。
  12. **路由 `to()` 仍不识别 `?schema=app`**：Camel JDBC 端点不识别 `?schema=app`（这是给 PG 驱动的，不是 Camel 端点参数）→ 路由 endpoint URI 也要 `stripQuery()`。
  13. **集成测试用 stub URL 通过但生产失败**：原 `AgentDataSourceControllerIntegrationTest` 用 `jdbc:postgresql://localhost:5432/test`（不存在的库）→ 新增 `CamelDataSourcePgIntegrationTest` 用 application.yml 真实 PG 端到端验证。
  14. **ArchUnit 6 处 method-length 违规**：`CamelAgentDataSourceAdapter.invoke()` / `JdbcDataSourceFactory` 3 处 / `TenantContextObjectHandler` 2 处均超 10 行 → 拆 helper 方法（`doInvoke` / `baseConfig` / `dispatchProperty` / `applyNumericOrPassThrough` / `shouldFillTenantId` / `applyTenantIdIfValid` / `shouldFillWorkspaceId` / `applyWorkspaceIdIfValid`）。

- 解决方案：
  - 全部通过补丁修复，共 ~14 处。

- 改进建议：
  - 统一 DTO 转换工具（手动 + BeanUtil 二选一），避免混合导致 map/枚举字段问题。
  - 抽象 "save 存在则更新/不存在则插入" 为仓储基类。
  - 所有 Controller 集成测试加 unique name 后缀，避免 CI 重跑时 `name` 冲突。
  - 集成测试默认准备 7 表迁移，避免 H2/PostgreSQL schema 不一致。
  - **集成测试必须用真实可用的依赖（PG / Redis / Kafka），不能用 stub URL**，否则会假绿。
  - **Camel 路由的 endpoint URI 处理**：写 helper 时同时考虑 `?` 查询参数对 lookup key 和路由解析的影响，统一 strip。
  - **Camel `toD` vs `to`**：JDBC/SQL body 是消息内容，必须用 `to` 静态 URI；`toD` 仅用于动态 URI 场景。
  - **ping/test 端到端不应走业务路由**：测试连接用 `DataSource.isValid()` 更直接可靠。
