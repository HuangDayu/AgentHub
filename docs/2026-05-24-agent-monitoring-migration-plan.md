# Agent 监控追踪统计调试功能移植方案

## 一、方案概述

### 1.1 移植目标

将 AgentScope Studio 的 Agent 监控、追踪、统计、调试功能完整移植到 AgentHub 项目中，实现企业级 AI Agent 全生命周期的可视化管理。

### 1.2 核心功能

1. **Agent 监控**：实时监控 Agent 运行状态、性能指标、资源使用
2. **Agent 追踪**：完整的调用链追踪、消息流追踪、状态变化追踪
3. **Agent 统计**：多维度统计分析、可视化报表展示
4. **Agent 调试**：实时交互调试、错误诊断、日志查看

### 1.3 技术选型对比

| 功能模块 | AgentScope Studio | AgentHub（移植后） | 说明 |
|---------|------------------|-------------------|------|
| 后端框架 | Express + TypeScript | Spring Boot 4.1 + Java 21 | 保持 AgentHub 技术栈 |
| 前端框架 | React 19 + TypeScript | Vue 3.5 + TypeScript | 保持 AgentHub 技术栈 |
| 实时通信 | Socket.IO | WebSocket (Spring WebSocket) | 使用 Spring 原生支持 |
| 数据采集 | OpenTelemetry + gRPC | OpenTelemetry + gRPC | 保持一致 |
| 数据库 | SQLite + TypeORM | PostgreSQL + Spring Data JPA | 使用 AgentHub 数据库 |
| API 通信 | tRPC | RESTful API + WebSocket | 使用 Spring MVC |

---

## 二、架构设计

### 2.1 整洁架构分层

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Controller  │  │   WebSocket  │  │   gRPC       │  │
│  │    (REST)    │  │   Handler    │  │   Server     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Application Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Monitor    │  │    Trace     │  │  Statistics  │  │
│  │   Service    │  │   Service    │  │   Service    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │   Debug      │  │   Alert      │                     │
│  │   Service    │  │   Service    │                     │
│  └──────────────┘  └──────────────┘                     │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Span Entity │  │ Trace Entity │  │  Run Entity  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │Message Entity│  │Metric Entity │  │Alert Entity  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │         Domain Services & Repository Ports        │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                 Infrastructure Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  PostgreSQL  │  │    Redis     │  │  OpenTelemetry│  │
│  │  Repository  │  │   Cache      │  │   Collector   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  WebSocket   │  │   gRPC       │  │   File       │  │
│  │  Publisher   │  │   Client     │  │   Storage    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 模块划分

```
com.agenthub/
├── api/                          # 表现层
│   ├── controller/
│   │   ├── MonitorController.java
│   │   ├── TraceController.java
│   │   ├── StatisticsController.java
│   │   └── DebugController.java
│   ├── websocket/
│   │   ├── WebSocketConfig.java
│   │   ├── MonitorWebSocketHandler.java
│   │   └── DebugWebSocketHandler.java
│   ├── grpc/
│   │   ├── GrpcServerConfig.java
│   │   └── TraceServiceGrpcImpl.java
│   └── dto/
│       ├── request/
│       └── response/
│
├── application/                  # 应用层
│   ├── monitor/
│   │   ├── MonitorService.java
│   │   ├── MetricCollector.java
│   │   └── AlertManager.java
│   ├── trace/
│   │   ├── TraceService.java
│   │   ├── SpanProcessor.java
│   │   └── TraceAnalyzer.java
│   ├── statistics/
│   │   ├── StatisticsService.java
│   │   ├── TraceStatisticsCalculator.java
│   │   └── ModelStatisticsCalculator.java
│   ├── debug/
│   │   ├── DebugService.java
│   │   ├── InteractiveSession.java
│   │   └── BreakpointManager.java
│   └── port/                     # 端口（接口）
│       ├── repository/
│       ├── publisher/
│       └── cache/
│
├── domain/                       # 领域层
│   ├── monitor/
│   │   ├── entity/
│   │   │   ├── Metric.java
│   │   │   ├── Alert.java
│   │   │   └── ResourceUsage.java
│   │   ├── valueobject/
│   │   │   ├── MetricType.java
│   │   │   └── AlertLevel.java
│   │   └── service/
│   │       └── MetricAggregator.java
│   ├── trace/
│   │   ├── entity/
│   │   │   ├── Span.java
│   │   │   ├── Trace.java
│   │   │   ├── SpanEvent.java
│   │   │   └── SpanLink.java
│   │   ├── valueobject/
│   │   │   ├── SpanKind.java
│   │   │   ├── SpanStatus.java
│   │   │   └── TraceContext.java
│   │   └── service/
│   │       ├── TraceBuilder.java
│   │       └── SpanTreeBuilder.java
│   ├── statistics/
│   │   ├── entity/
│   │   │   ├── TraceStatistics.java
│   │   │   ├── ModelStatistics.java
│   │   │   └── RunStatistics.java
│   │   └── valueobject/
│   │       ├── TimeRange.java
│   │       └── AggregationType.java
│   └── debug/
│       ├── entity/
│       │   ├── DebugSession.java
│       │   ├── Breakpoint.java
│       │   └── ExecutionState.java
│       └── valueobject/
│           ├── SessionStatus.java
│           └── DebugCommand.java
│
└── infrastructure/               # 基础设施层
    ├── persistence/
    │   ├── repository/
    │   │   ├── SpanRepositoryImpl.java
    │   │   ├── TraceRepositoryImpl.java
    │   │   └── MetricRepositoryImpl.java
    │   ├── converter/
    │   │   ├── SpanConverter.java
    │   │   └── TraceConverter.java
    │   └── po/
    │       ├── SpanPO.java
    │       ├── TracePO.java
    │       └── MetricPO.java
    ├── websocket/
    │   ├── WebSocketPublisher.java
    │   └── WebSocketSessionManager.java
    ├── grpc/
    │   ├── OtelGrpcServer.java
    │   └── SpanDecoder.java
    ├── cache/
    │   ├── RedisCacheManager.java
    │   └── MetricCache.java
    └── config/
        ├── OpenTelemetryConfig.java
        ├── WebSocketConfig.java
        └── GrpcConfig.java
```

---

## 三、核心功能设计

### 3.1 Agent 监控功能

#### 3.1.1 监控指标体系

```java
/**
 * 监控指标类型
 */
public enum MetricType {
    // 性能指标
    LATENCY_NS,           // 延迟（纳秒）
    THROUGHPUT,           // 吞吐量
    
    // Token 指标
    INPUT_TOKENS,         // 输入 Token
    OUTPUT_TOKENS,        // 输出 Token
    TOTAL_TOKENS,         // 总 Token
    
    // 资源指标
    CPU_USAGE,            // CPU 使用率
    MEMORY_USAGE,         // 内存使用量
    DB_SIZE,              // 数据库大小
    
    // 状态指标
    RUN_STATUS,           // 运行状态
    ERROR_COUNT,          // 错误计数
    SPAN_COUNT            // Span 计数
}
```

#### 3.1.2 数据采集流程

```
Agent 运行
    ↓
OpenTelemetry SDK
    ↓ (gRPC)
OtelGrpcServer (端口 4317)
    ↓
SpanDecoder (解码 OTLP)
    ↓
SpanProcessor (处理转换)
    ↓
SpanRepository (持久化)
    ↓
WebSocketPublisher (实时推送)
    ↓
前端监控面板
```

#### 3.1.3 实时监控实现

**WebSocket 配置**：
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(
        WebSocketHandlerRegistry registry
    ) {
        registry
            .addHandler(monitorWebSocketHandler, "/ws/monitor")
            .setAllowedOrigins("*");
    }
}
```

**监控数据推送**：
```java
@Component
public class MonitorWebSocketHandler 
    extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions 
        = new ConcurrentHashMap<>();
    
    /**
     * 推送监控数据到所有客户端
     */
    public void broadcastMetrics(MetricData data) {
        String json = toJson(data);
        sessions.values().forEach(session -> {
            session.sendMessage(new TextMessage(json));
        });
    }
}
```

### 3.2 Agent 追踪功能

#### 3.2.1 追踪数据模型

**Span 实体**：
```java
/**
 * Span 实体
 */
@Entity
@Table(name = "spans")
@Index(name = "idx_trace_id", columnList = "traceId")
@Index(name = "idx_span_id", columnList = "spanId")
@Index(name = "idx_parent_span_id", columnList = "parentSpanId")
@Index(name = "idx_start_time", columnList = "startTimeUnixNano")
public class Span {
    
    @Id
    private String spanId;
    
    private String traceId;
    private String parentSpanId;
    private String name;
    
    @Enumerated(EnumType.STRING)
    private SpanKind kind;
    
    private String startTimeUnixNano;
    private String endTimeUnixNano;
    private Long latencyNs;
    
    @Type(type = "json")
    private Map<String, Object> attributes;
    
    @Type(type = "json")
    private List<SpanEvent> events;
    
    @Enumerated(EnumType.STRING)
    private SpanStatus status;
    
    private Integer statusCode;
    
    // GenAI 特定字段
    private String model;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;
    private String conversationId;
}
```

#### 3.2.2 追踪树构建

```java
/**
 * 追踪树构建器
 */
@Component
public class SpanTreeBuilder {
    
    /**
     * 构建追踪树
     */
    public TraceNode buildTraceTree(List<Span> spans) {
        // 1. 找到根 Span
        Span rootSpan = findRootSpan(spans);
        
        // 2. 构建树结构
        TraceNode rootNode = buildNode(rootSpan);
        
        // 3. 递归构建子节点
        buildChildren(rootNode, spans);
        
        return rootNode;
    }
    
    /**
     * 构建子节点
     */
    private void buildChildren(
        TraceNode parent, 
        List<Span> spans
    ) {
        List<Span> children = spans.stream()
            .filter(s -> parent.getSpanId().equals(s.getParentSpanId()))
            .sorted(Comparator.comparing(Span::getStartTimeUnixNano))
            .collect(Collectors.toList());
        
        for (Span child : children) {
            TraceNode childNode = buildNode(child);
            parent.addChild(childNode);
            buildChildren(childNode, spans);
        }
    }
}
```

### 3.3 Agent 统计功能

#### 3.3.1 统计维度

```java
/**
 * 追踪统计
 */
@Data
public class TraceStatistics {
    
    private Long totalTraces;        // 总追踪数
    private Long totalSpans;         // 总 Span 数
    private Long errorTraces;        // 错误追踪数
    private Double avgDuration;      // 平均持续时间
    private Long totalTokens;        // 总 Token 数
    
    @Type(type = "json")
    private List<StatusCount> tracesByStatus;  // 按状态分组
    
    @Type(type = "json")
    private List<ModelCount> spansByModel;     // 按模型分组
}
```

#### 3.3.2 统计计算

```java
/**
 * 追踪统计计算器
 */
@Component
public class TraceStatisticsCalculator {
    
    private final SpanRepository spanRepository;
    
    /**
     * 计算追踪统计
     */
    public TraceStatistics calculate(
        String runId, 
        TimeRange timeRange
    ) {
        TraceStatistics stats = new TraceStatistics();
        
        // 1. 总 Span 数
        stats.setTotalSpans(
            spanRepository.countByRunId(runId)
        );
        
        // 2. 唯一 Trace 数
        stats.setTotalTraces(
            spanRepository.countDistinctTraceIdByRunId(runId)
        );
        
        // 3. 错误 Trace 数
        stats.setErrorTraces(
            spanRepository.countByRunIdAndStatusCode(
                runId, 
                StatusCode.ERROR
            )
        );
        
        // 4. 平均持续时间
        stats.setAvgDuration(
            calculateAverageDuration(runId)
        );
        
        // 5. 总 Token 数
        stats.setTotalTokens(
            spanRepository.sumTotalTokensByRunId(runId)
        );
        
        // 6. 按状态分组
        stats.setTracesByStatus(
            spanRepository.groupByStatusCode(runId)
        );
        
        return stats;
    }
}
```

### 3.4 Agent 调试功能

#### 3.4.1 调试会话管理

```java
/**
 * 调试会话
 */
@Entity
@Table(name = "debug_sessions")
public class DebugSession {
    
    @Id
    private String sessionId;
    
    private String runId;
    private String agentId;
    
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    
    @Type(type = "json")
    private List<Breakpoint> breakpoints;
    
    @Type(type = "json")
    private ExecutionState currentState;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 3.4.2 交互式调试

```java
/**
 * 调试服务
 */
@Service
public class DebugService {
    
    private final DebugSessionRepository sessionRepository;
    private final WebSocketPublisher webSocketPublisher;
    
    /**
     * 创建调试会话
     */
    public DebugSession createSession(
        String runId, 
        String agentId
    ) {
        DebugSession session = new DebugSession();
        session.setSessionId(generateId());
        session.setRunId(runId);
        session.setAgentId(agentId);
        session.setStatus(SessionStatus.ACTIVE);
        session.setBreakpoints(new ArrayList<>());
        
        return sessionRepository.save(session);
    }
    
    /**
     * 设置断点
     */
    public void setBreakpoint(
        String sessionId, 
        Breakpoint breakpoint
    ) {
        DebugSession session = getSession(sessionId);
        session.getBreakpoints().add(breakpoint);
        sessionRepository.save(session);
    }
    
    /**
     * 中断执行
     */
    public void interrupt(String sessionId) {
        DebugSession session = getSession(sessionId);
        session.setStatus(SessionStatus.INTERRUPTED);
        
        // 通知 Agent 中断
        webSocketPublisher.publish(
            session.getAgentId(), 
            new InterruptCommand()
        );
    }
    
    /**
     * 继续执行
     */
    public void resume(String sessionId) {
        DebugSession session = getSession(sessionId);
        session.setStatus(SessionStatus.RUNNING);
        
        // 通知 Agent 继续
        webSocketPublisher.publish(
            session.getAgentId(), 
            new ResumeCommand()
        );
    }
}
```

---

## 四、数据库设计

### 4.1 表结构设计

#### 4.1.1 spans 表

```sql
CREATE TABLE spans (
    span_id VARCHAR(64) PRIMARY KEY,
    trace_id VARCHAR(64) NOT NULL,
    parent_span_id VARCHAR(64),
    name VARCHAR(255) NOT NULL,
    kind VARCHAR(32) NOT NULL,
    
    start_time_unix_nano VARCHAR(32) NOT NULL,
    end_time_unix_nano VARCHAR(32) NOT NULL,
    latency_ns BIGINT NOT NULL,
    
    attributes JSONB,
    events JSONB,
    links JSONB,
    
    status_code INTEGER,
    status_message TEXT,
    
    resource JSONB,
    scope JSONB,
    
    -- GenAI 字段
    model VARCHAR(128),
    input_tokens INTEGER,
    output_tokens INTEGER,
    total_tokens INTEGER,
    conversation_id VARCHAR(64),
    
    run_id VARCHAR(64),
    created_at TIMESTAMP NOT NULL,
    
    INDEX idx_trace_id (trace_id),
    INDEX idx_parent_span_id (parent_span_id),
    INDEX idx_start_time (start_time_unix_nano),
    INDEX idx_status_code (status_code),
    INDEX idx_run_id (run_id),
    INDEX idx_model (model)
);
```

#### 4.1.2 traces 表

```sql
CREATE TABLE traces (
    trace_id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL,
    
    root_span_id VARCHAR(64) NOT NULL,
    span_count INTEGER NOT NULL,
    
    start_time_unix_nano VARCHAR(32) NOT NULL,
    end_time_unix_nano VARCHAR(32) NOT NULL,
    duration_ns BIGINT NOT NULL,
    
    status_code INTEGER,
    error_message TEXT,
    
    total_tokens INTEGER,
    
    created_at TIMESTAMP NOT NULL,
    
    INDEX idx_run_id (run_id),
    INDEX idx_start_time (start_time_unix_nano),
    INDEX idx_status_code (status_code)
);
```

#### 4.1.3 metrics 表

```sql
CREATE TABLE metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_type VARCHAR(32) NOT NULL,
    metric_name VARCHAR(128) NOT NULL,
    
    metric_value DOUBLE PRECISION NOT NULL,
    
    run_id VARCHAR(64),
    agent_id VARCHAR(64),
    trace_id VARCHAR(64),
    span_id VARCHAR(64),
    
    labels JSONB,
    
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    
    INDEX idx_metric_type (metric_type),
    INDEX idx_run_id (run_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_timestamp (timestamp)
);
```

#### 4.1.4 debug_sessions 表

```sql
CREATE TABLE debug_sessions (
    session_id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL,
    agent_id VARCHAR(64) NOT NULL,
    
    status VARCHAR(32) NOT NULL,
    
    breakpoints JSONB,
    current_state JSONB,
    
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    INDEX idx_run_id (run_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_status (status)
);
```

#### 4.1.5 alerts 表

```sql
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_level VARCHAR(16) NOT NULL,
    alert_type VARCHAR(64) NOT NULL,
    
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    
    run_id VARCHAR(64),
    agent_id VARCHAR(64),
    trace_id VARCHAR(64),
    
    metadata JSONB,
    
    is_resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP,
    
    created_at TIMESTAMP NOT NULL,
    
    INDEX idx_alert_level (alert_level),
    INDEX idx_run_id (run_id),
    INDEX idx_is_resolved (is_resolved),
    INDEX idx_created_at (created_at)
);
```

---

## 五、前端设计

### 5.1 页面结构

```
views/
├── monitor/
│   ├── MonitorDashboard.vue       # 监控仪表盘
│   ├── MetricPanel.vue            # 指标面板
│   ├── AlertPanel.vue             # 告警面板
│   └── ResourcePanel.vue          # 资源面板
│
├── trace/
│   ├── TraceDashboard.vue         # 追踪仪表盘
│   ├── TraceTree.vue              # 追踪树
│   ├── SpanDetail.vue             # Span 详情
│   ├── MessageFlow.vue            # 消息流
│   └── Timeline.vue               # 时间线
│
├── statistics/
│   ├── StatisticsDashboard.vue    # 统计仪表盘
│   ├── TraceStatistics.vue        # 追踪统计
│   ├── ModelStatistics.vue        # 模型统计
│   └── Charts.vue                 # 图表组件
│
└── debug/
    ├── DebugConsole.vue           # 调试控制台
    ├── BreakpointPanel.vue        # 断点面板
    ├── VariableViewer.vue         # 变量查看器
    └── LogViewer.vue              # 日志查看器
```

### 5.2 组件设计

#### 5.2.1 监控组件

**MonitorDashboard.vue**：
```vue
<template>
  <div class="monitor-dashboard">
    <!-- 实时指标卡片 -->
    <a-row :gutter="16">
      <a-col :span="6" v-for="metric in realTimeMetrics">
        <MetricCard 
          :title="metric.title"
          :value="metric.value"
          :unit="metric.unit"
          :trend="metric.trend"
        />
      </a-col>
    </a-row>
    
    <!-- 性能图表 -->
    <a-row :gutter="16" class="mt-4">
      <a-col :span="12">
        <PerformanceChart :data="performanceData" />
      </a-col>
      <a-col :span="12">
        <TokenUsageChart :data="tokenData" />
      </a-col>
    </a-row>
    
    <!-- 告警列表 -->
    <AlertPanel :alerts="activeAlerts" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useWebSocket } from '@/composables/useWebSocket';

const { connect, disconnect, subscribe } = useWebSocket();

// 实时指标
const realTimeMetrics = ref<Metric[]>([]);

// 性能数据
const performanceData = ref<PerformanceData[]>([]);

// Token 数据
const tokenData = ref<TokenData[]>([]);

// 告警列表
const activeAlerts = ref<Alert[]>([]);

onMounted(() => {
  // 连接 WebSocket
  connect('/ws/monitor');
  
  // 订阅监控数据
  subscribe('metrics', (data) => {
    realTimeMetrics.value = data;
  });
  
  // 订阅告警
  subscribe('alerts', (data) => {
    activeAlerts.value = data;
  });
});

onUnmounted(() => {
  disconnect();
});
</script>
```

#### 5.2.2 追踪组件

**TraceTree.vue**：
```vue
<template>
  <div class="trace-tree">
    <a-input-search
      v-model:value="searchText"
      placeholder="搜索 Span"
      @search="handleSearch"
    />
    
    <a-tree
      :tree-data="treeData"
      :selected-keys="selectedKeys"
      :expanded-keys="expandedKeys"
      @select="handleSelect"
    >
      <template #title="{ span }">
        <SpanNode :span="span" />
      </template>
    </a-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { Span, TraceNode } from '@/types/trace';

const props = defineProps<{
  spans: Span[];
}>();

// 构建追踪树
const treeData = computed(() => {
  return buildTraceTree(props.spans);
});

// 选中的 Span
const selectedKeys = ref<string[]>([]);

// 展开的节点
const expandedKeys = ref<string[]>([]);

// 搜索文本
const searchText = ref('');

// 处理选择
const handleSelect = (keys: string[]) => {
  selectedKeys.value = keys;
  emit('select', keys[0]);
};
</script>
```

#### 5.2.3 统计组件

**StatisticsDashboard.vue**：
```vue
<template>
  <div class="statistics-dashboard">
    <!-- 统计卡片 -->
    <a-row :gutter="16">
      <a-col :span="6">
        <StatisticCard
          title="总追踪数"
          :value="statistics.totalTraces"
        />
      </a-col>
      <a-col :span="6">
        <StatisticCard
          title="总 Span 数"
          :value="statistics.totalSpans"
        />
      </a-col>
      <a-col :span="6">
        <StatisticCard
          title="错误追踪"
          :value="statistics.errorTraces"
          status="error"
        />
      </a-col>
      <a-col :span="6">
        <StatisticCard
          title="平均延迟"
          :value="statistics.avgDuration"
          unit="ms"
        />
      </a-col>
    </a-row>
    
    <!-- 图表 -->
    <a-row :gutter="16" class="mt-4">
      <a-col :span="12">
        <StatusDistributionChart 
          :data="statistics.tracesByStatus"
        />
      </a-col>
      <a-col :span="12">
        <TokenUsageByModelChart 
          :data="statistics.tokensByModel"
        />
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getTraceStatistics } from '@/api/statistics';

const statistics = ref<TraceStatistics>({});

onMounted(async () => {
  statistics.value = await getTraceStatistics();
});
</script>
```

#### 5.2.4 调试组件

**DebugConsole.vue**：
```vue
<template>
  <div class="debug-console">
    <!-- 控制按钮 -->
    <a-space>
      <a-button 
        @click="handleInterrupt"
        :disabled="!isRunning"
      >
        中断
      </a-button>
      <a-button 
        @click="handleResume"
        :disabled="isRunning"
      >
        继续
      </a-button>
      <a-button @click="handleStep">
        单步执行
      </a-button>
    </a-space>
    
    <!-- 断点面板 -->
    <BreakpointPanel
      :breakpoints="breakpoints"
      @add="handleAddBreakpoint"
      @remove="handleRemoveBreakpoint"
    />
    
    <!-- 变量查看器 -->
    <VariableViewer :variables="currentVariables" />
    
    <!-- 日志查看器 -->
    <LogViewer :logs="logs" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useWebSocket } from '@/composables/useWebSocket';

const { subscribe, emit } = useWebSocket();

// 会话状态
const isRunning = ref(true);

// 断点列表
const breakpoints = ref<Breakpoint[]>([]);

// 当前变量
const currentVariables = ref<Variables>({});

// 日志
const logs = ref<Log[]>([]);

// 订阅调试事件
subscribe('debug:state', (state) => {
  isRunning.value = state.isRunning;
  currentVariables.value = state.variables;
});

subscribe('debug:log', (log) => {
  logs.value.push(log);
});

// 中断执行
const handleInterrupt = () => {
  emit('debug:interrupt');
};

// 继续执行
const handleResume = () => {
  emit('debug:resume');
};
</script>
```

### 5.3 API 设计

```typescript
// api/monitor.ts
export const monitorApi = {
  // 获取实时指标
  getRealTimeMetrics: (runId: string) => 
    request.get(`/api/monitor/metrics/${runId}`),
  
  // 获取告警列表
  getAlerts: (params: AlertQueryParams) => 
    request.get('/api/monitor/alerts', { params }),
  
  // 解决告警
  resolveAlert: (alertId: string) => 
    request.put(`/api/monitor/alerts/${alertId}/resolve`),
};

// api/trace.ts
export const traceApi = {
  // 获取追踪列表
  getTraces: (params: TraceQueryParams) => 
    request.get('/api/traces', { params }),
  
  // 获取追踪详情
  getTrace: (traceId: string) => 
    request.get(`/api/traces/${traceId}`),
  
  // 获取 Span 详情
  getSpan: (spanId: string) => 
    request.get(`/api/spans/${spanId}`),
  
  // 搜索 Span
  searchSpans: (params: SpanSearchParams) => 
    request.get('/api/spans/search', { params }),
};

// api/statistics.ts
export const statisticsApi = {
  // 获取追踪统计
  getTraceStatistics: (params: StatisticsParams) => 
    request.get('/api/statistics/traces', { params }),
  
  // 获取模型统计
  getModelStatistics: (params: StatisticsParams) => 
    request.get('/api/statistics/models', { params }),
  
  // 获取运行统计
  getRunStatistics: (runId: string) => 
    request.get(`/api/statistics/runs/${runId}`),
};

// api/debug.ts
export const debugApi = {
  // 创建调试会话
  createSession: (runId: string, agentId: string) => 
    request.post('/api/debug/sessions', { runId, agentId }),
  
  // 设置断点
  setBreakpoint: (sessionId: string, breakpoint: Breakpoint) => 
    request.post(`/api/debug/sessions/${sessionId}/breakpoints`, breakpoint),
  
  // 删除断点
  removeBreakpoint: (sessionId: string, breakpointId: string) => 
    request.delete(`/api/debug/sessions/${sessionId}/breakpoints/${breakpointId}`),
  
  // 中断执行
  interrupt: (sessionId: string) => 
    request.post(`/api/debug/sessions/${sessionId}/interrupt`),
  
  // 继续执行
  resume: (sessionId: string) => 
    request.post(`/api/debug/sessions/${sessionId}/resume`),
};
```

### 5.4 WebSocket 事件

```typescript
// types/websocket.ts
export enum WebSocketEvent {
  // 监控事件
  MONITOR_METRICS = 'monitor:metrics',
  MONITOR_ALERT = 'monitor:alert',
  
  // 追踪事件
  TRACE_SPANS = 'trace:spans',
  TRACE_MESSAGES = 'trace:messages',
  
  // 调试事件
  DEBUG_STATE = 'debug:state',
  DEBUG_LOG = 'debug:log',
  DEBUG_INTERRUPT = 'debug:interrupt',
  DEBUG_RESUME = 'debug:resume',
}

// composables/useWebSocket.ts
export function useWebSocket() {
  const socket = ref<WebSocket | null>(null);
  const listeners = new Map<string, Set<Function>>();
  
  const connect = (path: string) => {
    socket.value = new WebSocket(`ws://${location.host}${path}`);
    
    socket.value.onmessage = (event) => {
      const { type, data } = JSON.parse(event.data);
      listeners.get(type)?.forEach(fn => fn(data));
    };
  };
  
  const subscribe = (event: string, callback: Function) => {
    if (!listeners.has(event)) {
      listeners.set(event, new Set());
    }
    listeners.get(event)!.add(callback);
  };
  
  const emit = (event: string, data?: any) => {
    socket.value?.send(JSON.stringify({ type: event, data }));
  };
  
  return { connect, subscribe, emit };
}
```

---

## 六、实施计划

### 6.1 阶段划分

#### 第一阶段：基础设施搭建（1-2 周）

**后端任务**：
1. 创建数据库表结构
2. 配置 OpenTelemetry gRPC 服务器
3. 实现 Span 解码和处理器
4. 配置 WebSocket 服务器
5. 实现基础 Repository

**前端任务**：
1. 创建页面路由
2. 实现基础布局
3. 配置 WebSocket 连接
4. 实现基础 API 调用

#### 第二阶段：监控功能实现（1-2 周）

**后端任务**：
1. 实现监控指标采集
2. 实现告警管理
3. 实现监控数据推送
4. 编写单元测试

**前端任务**：
1. 实现监控仪表盘
2. 实现指标卡片组件
3. 实现告警面板
4. 实现实时数据更新

#### 第三阶段：追踪功能实现（2-3 周）

**后端任务**：
1. 实现追踪数据存储
2. 实现追踪树构建
3. 实现追踪查询 API
4. 实现消息流处理
5. 编写单元测试

**前端任务**：
1. 实现追踪仪表盘
2. 实现追踪树组件
3. 实现 Span 详情面板
4. 实现消息流组件
5. 实现时间线组件

#### 第四阶段：统计功能实现（1-2 周）

**后端任务**：
1. 实现统计计算器
2. 实现聚合查询
3. 实现统计 API
4. 编写单元测试

**前端任务**：
1. 实现统计仪表盘
2. 实现图表组件
3. 实现数据导出功能

#### 第五阶段：调试功能实现（2-3 周）

**后端任务**：
1. 实现调试会话管理
2. 实现断点管理
3. 实现交互式调试
4. 实现日志收集
5. 编写单元测试

**前端任务**：
1. 实现调试控制台
2. 实现断点面板
3. 实现变量查看器
4. 实现日志查看器

#### 第六阶段：集成测试和优化（1-2 周）

1. 编写集成测试
2. 性能优化
3. UI/UX 优化
4. 文档编写

### 6.2 技术难点和解决方案

#### 6.2.1 OpenTelemetry 数据处理

**难点**：
- OTLP 格式复杂，需要正确解码
- 数据量大，需要高效处理
- 时间戳精度要求高（纳秒级）

**解决方案**：
1. 使用 OpenTelemetry Java SDK 处理 OTLP 数据
2. 使用批量处理和异步写入提高性能
3. 使用 BigDecimal 处理纳秒级时间戳

#### 6.2.2 实时数据推送

**难点**：
- WebSocket 连接管理
- 大量客户端的广播性能
- 断线重连处理

**解决方案**：
1. 使用 Spring WebSocket 的会话管理
2. 使用消息队列（Redis Pub/Sub）实现广播
3. 实现心跳检测和自动重连

#### 6.2.3 追踪树构建

**难点**：
- 大量 Span 的树构建性能
- 不完整追踪的处理
- 搜索和过滤性能

**解决方案**：
1. 使用索引优化查询性能
2. 使用缓存存储构建好的追踪树
3. 实现增量更新机制

#### 6.2.4 整洁架构约束

**难点**：
- 严格的分层约束
- 方法长度限制（10 行）
- 参数数量限制（3 个）

**解决方案**：
1. 使用 ArchUnit 自动检查架构规则
2. 使用 Builder 模式处理多参数
3. 使用私有方法拆分长方法

---

## 七、质量保证

### 7.1 架构规则

使用 ArchUnit 确保整洁架构：

```java
@AnalyzeClasses(packages = "com.agenthub")
public class ArchitectureTest {
    
    // 领域层不依赖其他层
    @ArchTest
    static final ArchRule domain_layer_rule = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..infrastructure..", "..api..");
    
    // 应用层不依赖基础设施层
    @ArchTest
    static final ArchRule application_layer_rule = 
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..");
    
    // 方法长度不超过 10 行
    @ArchTest
    static final ArchRule method_length_rule = 
        methods()
            .that().areDeclaredInClassesThat()
            .resideInAPackage("..domain..")
            .should().haveRawBodyLineCountLessThanOrEqualTo(10);
}
```

### 7.2 测试策略

#### 7.2.1 单元测试

- 领域层：100% 覆盖
- 应用层：80% 覆盖
- 基础设施层：60% 覆盖

#### 7.2.2 集成测试

- Controller 集成测试：100% 覆盖
- WebSocket 集成测试：关键场景覆盖
- gRPC 集成测试：关键场景覆盖

#### 7.2.3 性能测试

- 追踪数据写入性能
- 追踪树构建性能
- WebSocket 广播性能

---

## 八、总结

本方案详细规划了将 AgentScope Studio 的 Agent 监控、追踪、统计、调试功能移植到 AgentHub 项目的完整流程。方案遵循整洁架构原则，确保代码质量和可维护性。

**核心优势**：
1. 完整的功能覆盖：监控、追踪、统计、调试四大功能
2. 清晰的架构分层：符合整洁架构和 SOLID 原则
3. 实时数据推送：基于 WebSocket 的实时通信
4. 标准化数据采集：基于 OpenTelemetry 的标准化追踪
5. 可视化展示：丰富的图表和交互界面
6. 高质量保证：ArchUnit 架构检查 + 完整测试覆盖

**预期成果**：
- 实现企业级 Agent 全生命周期可视化管理
- 提供强大的调试和诊断能力
- 支持大规模 Agent 运行监控
- 为 Agent 优化提供数据支撑
