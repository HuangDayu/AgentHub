# AgentHub Telemetry 模块

基于OpenTelemetry规范的Agent运行时链路追踪功能实现。

## 功能特性

- ✅ 基于OpenTelemetry标准的分布式链路追踪
- ✅ 自动追踪Agent调用、模型调用、工具执行、RAG检索
- ✅ 支持OTLP协议导出到外部平台（Langfuse、Jaeger等）
- ✅ 追踪数据持久化到数据库
- ✅ 提供REST API查询追踪数据
- ✅ 支持多种采样策略
- ✅ 与AgentScope框架无缝集成

## 架构设计

```
com.agenthub.infrastructure.telemetry
├── config/                          # 配置层
│   ├── TelemetryProperties.java     # Telemetry配置属性
│   └── OpenTelemetryConfig.java     # OpenTelemetry配置类
├── tracing/                         # 链路追踪核心
│   ├── AgentTracer.java            # Agent追踪器
│   ├── SpanContext.java            # Span上下文
│   └── TraceInterceptor.java       # 追踪拦截器（AOP）
├── exporter/                        # 数据导出
│   └── DatabaseSpanExporter.java   # 数据库导出器
├── model/                           # 数据模型
│   └── TraceSpanEntity.java        # Span实体
└── repository/                      # 数据存储
    ├── TraceSpanMapper.java        # MyBatis Mapper
    └── TelemetryRepository.java    # 遥测数据仓储
```

## 快速开始

### 1. 配置文件

在 `application.yml` 中添加配置：

```yaml
agenthub:
  telemetry:
    enabled: true
    service-name: agenthub
    record-to-database: true
    otlp:
      enabled: false
      endpoint: http://localhost:4317
    sampling:
      strategy: always
```

### 2. 数据库初始化

执行数据库迁移脚本：

```bash
psql -d agenthub -f src/main/resources/db/migration/V1.0.0__Create_Telemetry_Tables.sql
```

### 3. 自动追踪

追踪功能会自动生效，无需额外代码：

- **Agent调用**：每次 `agent.call()` 自动生成Span
- **模型调用**：每次LLM调用自动生成Span
- **工具执行**：每次工具调用自动生成Span
- **RAG检索**：每次检索操作自动生成Span

### 4. 查询追踪数据

使用REST API查询：

```bash
# 查询完整链路
GET /api/v1/telemetry/traces/{traceId}

# 按时间范围查询
GET /api/v1/telemetry/spans?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00&tenantId=default&workspaceId=default

# 按类型查询
GET /api/v1/telemetry/spans/type/AGENT_CALL?tenantId=default&workspaceId=default

# 查询慢Span
GET /api/v1/telemetry/spans/slow?thresholdMs=1000&tenantId=default&workspaceId=default

# 获取统计数据
GET /api/v1/telemetry/statistics?tenantId=default&workspaceId=default
```

## 配置说明

### TelemetryProperties

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `enabled` | 是否启用Telemetry | `true` |
| `service-name` | 服务名称 | `agenthub` |
| `record-to-database` | 是否记录到数据库 | `true` |

### OTLP配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `otlp.enabled` | 是否启用OTLP导出 | `false` |
| `otlp.endpoint` | OTLP端点URL | `http://localhost:4317` |
| `otlp.timeout` | 请求超时（毫秒） | `10000` |
| `otlp.headers` | HTTP请求头 | `{}` |

### 采样配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `sampling.strategy` | 采样策略 | `always` |
| `sampling.ratio` | 采样比例 | `1.0` |

采样策略：
- `always`：总是采样
- `never`：从不采样
- `ratio`：按比例采样

## 集成外部平台

### Langfuse

```yaml
agenthub:
  telemetry:
    otlp:
      enabled: true
      endpoint: https://cloud.langfuse.com/api/public/otel/v1/traces
      headers:
        Authorization: Bearer your-langfuse-key
```

### Jaeger

```yaml
agenthub:
  telemetry:
    otlp:
      enabled: true
      endpoint: http://localhost:14268/api/traces
```

## API接口

### 查询追踪链路

```http
GET /api/v1/telemetry/traces/{traceId}
```

响应示例：

```json
[
  {
    "id": 1,
    "traceId": "abc123",
    "spanId": "span1",
    "spanName": "agent.call",
    "spanType": "AGENT_CALL",
    "startTime": "2024-01-01T10:00:00",
    "endTime": "2024-01-01T10:00:05",
    "durationMs": 5000,
    "statusCode": "OK",
    "attributes": {
      "agent.id": "agent-001",
      "agent.name": "MyAgent",
      "session.id": "session-123"
    }
  }
]
```

### 查询统计数据

```http
GET /api/v1/telemetry/statistics?tenantId=default&workspaceId=default
```

响应示例：

```json
{
  "totalSpans": 1000,
  "spansByType": {
    "AGENT_CALL": 100,
    "MODEL_CALL": 500,
    "TOOL_EXECUTION": 300,
    "RAG_RETRIEVAL": 100
  },
  "avgDurationMs": 250.5,
  "maxDurationMs": 5000,
  "minDurationMs": 10,
  "errorRate": 0.02,
  "slowSpanCount": 50
}
```

## 技术实现

### 核心组件

1. **OpenTelemetryConfig**：初始化OpenTelemetry SDK
2. **AgentTracer**：提供追踪API
3. **TraceInterceptor**：AOP拦截器，自动追踪方法调用
4. **DatabaseSpanExporter**：将追踪数据导出到数据库
5. **TelemetryRepository**：数据持久化和查询

### 追踪流程

```
1. Agent调用 → TraceInterceptor拦截
2. 创建Span → AgentTracer.startAgentSpan()
3. 执行业务逻辑
4. 记录属性和事件
5. 结束Span → AgentTracer.endSpan()
6. 导出数据 → DatabaseSpanExporter.export()
7. 持久化 → TelemetryRepository.saveSpan()
```

## 注意事项

1. 确保数据库表已创建
2. 生产环境建议使用适当的采样策略
3. OTLP导出需要配置外部平台
4. 追踪数据会占用存储空间，建议定期清理

## 参考文档

- [OpenTelemetry官方文档](https://opentelemetry.io/docs/)
- [AgentScope可观测性文档](https://java.agentscope.io/zh/task/observability.html)
- [Langfuse集成指南](https://langfuse.com/docs/integrations/opentelemetry)
