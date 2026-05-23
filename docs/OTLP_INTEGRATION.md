# OTLP集成说明文档

## 概述

本项目已成功集成嵌入式OTLP（OpenTelemetry Protocol）协议服务，实现了对OpenTelemetry追踪、指标和日志数据的接收、存储和可视化展示。

## 架构设计

### 后端架构

1. **数据存储层**
   - `OtlpSpanEntity`: 存储追踪Span数据
   - `OtlpMetricEntity`: 存储指标数据
   - `OtlpLogEntity`: 存储日志数据

2. **数据访问层**
   - `OtlpSpanMapper`: Span数据Mapper
   - `OtlpMetricMapper`: Metric数据Mapper
   - `OtlpLogMapper`: Log数据Mapper

3. **服务层**
   - `OtlpStorageService`: 负责数据的存储和查询

4. **API层**
   - `OtlpController`: 提供REST API端点

### 前端架构

1. **API服务**
   - `src/api/otlp.ts`: 封装OTLP API调用

2. **UI组件**
   - `OtlpRuntimePanel.vue`: OTLP监控面板组件
   - 集成到`RuntimeChatView.vue`的运行时管理模块

## API端点

### 数据接收端点（OTLP协议）

- `POST /api/v1/otlp/traces`: 接收追踪数据
- `POST /api/v1/otlp/metrics`: 接收指标数据
- `POST /api/v1/otlp/logs`: 接收日志数据

### 数据查询端点

- `GET /api/v1/otlp/spans?limit=100`: 查询最近的Span数据
- `GET /api/v1/otlp/traces/{traceId}`: 根据TraceId查询Span数据
- `GET /api/v1/otlp/metrics/query?limit=100`: 查询最近的Metric数据
- `GET /api/v1/otlp/logs/query?limit=100`: 查询最近的Log数据
- `GET /api/v1/otlp/statistics`: 获取统计信息

## 数据库表结构

### otlp_span表

存储OpenTelemetry追踪Span数据，包含以下字段：
- span_id: Span唯一标识
- trace_id: 追踪ID
- parent_span_id: 父Span ID
- operation_name: 操作名称
- service_name: 服务名称
- kind: Span类型
- start_timestamp/end_timestamp: 时间戳
- duration: 持续时间
- status: 状态
- attributes/events/links: 扩展数据

### otlp_metric表

存储OpenTelemetry指标数据，包含以下字段：
- metric_name: 指标名称
- metric_type: 指标类型
- service_name: 服务名称
- value: 指标值
- attributes: 属性数据

### otlp_log表

存储OpenTelemetry日志数据，包含以下字段：
- log_id: 日志ID
- trace_id/span_id: 关联的追踪信息
- service_name: 服务名称
- severity: 日志级别
- body: 日志内容
- attributes: 属性数据

## 使用说明

### 1. 初始化数据库

执行以下SQL脚本创建OTLP相关表：

```bash
psql -U things -d agenthub -f sql/otlp_schema.sql
```

### 2. 配置OpenTelemetry

OpenTelemetry配置已自动指向嵌入式OTLP服务端点：

```java
TelemetryTracer.builder()
    .endpoint("http://localhost:8080/api/v1/otlp/traces")
    .build()
```

### 3. 访问监控面板

1. 启动应用后，访问聊天页面
2. 点击左下角的"运行时管理"展开按钮
3. 在运行时管理面板中可以查看：
   - 统计概览（追踪数、指标数、日志数）
   - 追踪数据列表
   - 指标数据列表
   - 日志数据列表

### 4. 发送追踪数据

应用会自动将追踪数据发送到OTLP端点，也可以手动发送测试数据：

```bash
curl -X POST http://localhost:8080/api/v1/otlp/traces \
  -H "Content-Type: application/json" \
  -d '{
    "spanId": "test-span-001",
    "traceId": "test-trace-001",
    "operationName": "test-operation",
    "serviceName": "test-service",
    "kind": "INTERNAL",
    "startTimestamp": 1234567890000000,
    "endTimestamp": 1234567891000000,
    "status": "OK"
  }'
```

## 功能特性

1. **实时监控**: 自动刷新数据，默认30秒刷新一次
2. **多维度展示**: 支持追踪、指标、日志三种数据类型
3. **详细信息**: 展示完整的Span、Metric、Log信息
4. **统计概览**: 提供数据总量统计
5. **友好界面**: 现代化的UI设计，支持展开/收起

## 技术栈

- **后端**: Spring Boot, MyBatis-Plus, PostgreSQL
- **前端**: Vue 3, TypeScript, Axios
- **协议**: OpenTelemetry Protocol (OTLP)

## 注意事项

1. 确保数据库已创建OTLP相关表
2. 前端组件已自动集成到运行时管理模块
3. 数据默认保留在数据库中，可根据需要添加清理策略
4. 支持多租户和工作空间隔离

## 扩展建议

1. 添加数据清理策略，定期清理过期数据
2. 实现更复杂的数据分析和可视化
3. 添加告警功能
4. 支持导出数据
5. 集成更多OpenTelemetry SDK功能
