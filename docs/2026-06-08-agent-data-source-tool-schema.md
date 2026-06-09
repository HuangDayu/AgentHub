# AgentDataSourceToolFactory 协议感知 Schema 重构方案

> 日期：2026-06-08 · 状态：已完成

## 1. 需求概述

Agent 调用数据源工具时，由于 INPUT_SCHEMA 和 description 对所有协议统一为 `{"body": "string"}`，LLM 无法知道 body 应该填什么格式。需要按协议生成差异化的 inputSchema、description 和示例。

## 2. 最终方案：typed-params 模式

**放弃手写 JSON Schema 字符串**，改用 Java POJO + `@JsonPropertyDescription` 注解 + `SchemaGenerator` 自动生成 Schema。与 `SqlTools`/`RestfulTools` 的 `@Tool`/`@ToolParam` 模式一致。

### 核心设计

```
AgentDataSourceToolFactory
├── buildJdbcTool()    → JdbcParams (sql)
├── buildHttpTool()    → HttpParams (method/path/body/queryParams)
├── buildMongoTool()   → MongoParams (collection/operation/query/document)
├── buildKafkaTool()   → KafkaParams (topic/message/key/headers)
├── buildRedisTool()   → RedisParams (command/key/value/args)
├── buildFileTool()    → FileParams (operation/path/content)
└── buildDefaultTool() → BodyParams (body)
```

每个 POJO 的字段用 `@JsonPropertyDescription("...")` 标注，`SchemaGenerator` 反射读取后自动生成 JSON Schema。

## 3. 架构设计

### 3.1 新增文件

| 层 | 文件 | 操作 |
|----|------|------|
| infrastructure | `params/JdbcParams.java` | 新建 |
| infrastructure | `params/HttpParams.java` | 新建 |
| infrastructure | `params/MongoParams.java` | 新建 |
| infrastructure | `params/KafkaParams.java` | 新建 |
| infrastructure | `params/RedisParams.java` | 新建 |
| infrastructure | `params/FileParams.java` | 新建 |
| infrastructure | `params/BodyParams.java` | 新建 |
| infrastructure | `params/SchemaGenerator.java` | 新建 |
| infrastructure | `AgentDataSourceToolFactory.java` | 重写 |
| ~~infrastructure~~ | ~~`ProtocolSchemaRegistry.java`~~ | ~~已删除~~ |

### 3.2 工具命名规则

| 协议 | 前缀 | 示例 |
|------|------|------|
| JDBC/SQL | `sql_query_` | `sql_query_订单库` |
| HTTP/REST | `http_call_` | `http_call_用户API` |
| MongoDB | `mongo_op_` | `mongo_op_日志库` |
| Kafka | `kafka_send_` | `kafka_send_事件总线` |
| Redis | `redis_cmd_` | `redis_cmd_缓存` |
| FTP/SFTP/FILE | `file_op_` | `file_op_报表目录` |
| 其他 | `datasource_invoke_` | — |

## 4. 边界情况

- [x] protocol 为 null → 使用 BodyParams 默认 schema
- [x] 数据源未启用 → 不生成 ToolCallback（已有逻辑）
- [x] endpointUri 为 null → description 中 URI 显示 ""
- [x] 字段名含特殊字符 → `sanitize()` 替换为 `_`

## 5. 检查清单

- [x] 方法 ≤10 行
- [x] 方法 ≤3 参数
- [x] 分层依赖正确（infrastructure 内部）
- [x] 命名符合约定
- [x] ArchUnit 19/19 通过
- [x] 无 `record` / `@Builder` / `@Autowired`

## 6. 测试结果

| 测试 | 结果 |
|------|------|
| SchemaGeneratorTest（单元） | 8/8 ✅ |
| AgentDataSourceToolCallbackProviderIntegrationTest | 14/14 ✅ |
| AgentHubCleanArchitectureTest | 19/19 ✅ |
| **全量测试** | **381/383**（2个预存失败） |

### 预存失败（非本次变更）

`AuditLogControllerIntegrationTest` 的 2 个测试因 `WorkspacesContextLineHandler.getTenantId()` 返回 null → JSQLParser `StringValue(null)` NPE → 500 错误。此问题在之前的审计日志重构中已部分修复，但 AuditLog 查询路径仍受影响。

## 7. 反思

### 做得好的
- 放弃手写 JSON Schema 字符串，改用 POJO + 注解 + 反射自动生成，大幅降低维护成本
- 每种协议有独立的参数类，结构清晰
- 工具命名包含协议前缀，LLM 一看就知道是什么类型的操作

### 遇到的问题
- 最初设计的 `ProtocolSchemaRegistry` 用手写 JSON Schema 字符串，维护困难且容易出错（如转义引号）
- 用户反馈后切换到 typed-params 模式，更符合项目中 `SqlTools`/`RestfulTools` 的风格

### 改进建议
- 可以为每种协议的参数 POJO 添加 `@ToolParam` 注解（如果 Spring AI 支持从 POJO 字段读取）
- 可以考虑为常用操作预定义模板（如 `SELECT * FROM {table} WHERE {condition}`）
