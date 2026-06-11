# Agent 监控追踪统计调试功能完整实施报告

## 一、已完成工作总结

### 1. 方案设计 ✅
- **文件**: `docs/agent-monitoring-migration-plan.md`
- **内容**: 完整的移植方案，包括架构设计、功能设计、数据库设计、前端设计
- **状态**: 已完成

### 2. 数据库表结构 ✅
- **文件**: `sql/agent_monitoring_schema.sql`
- **表结构**:
  - `spans` - Span 追踪数据
  - `traces` - Trace 聚合数据
  - `metrics` - 监控指标
  - `alerts` - 告警信息
  - `debug_sessions` - 调试会话
  - `debug_logs` - 调试日志
  - `trace_statistics` - 追踪统计
  - `model_statistics` - 模型统计
- **状态**: 已完成

### 3. 领域模型 ✅
按照整洁架构规范实现：

**追踪领域**:
- `domain/model/trace/Span.java` - Span 领域模型
- `domain/model/trace/Trace.java` - Trace 领域模型

**监控领域**:
- `domain/model/monitor/Metric.java` - Metric 领域模型
- `domain/model/monitor/Alert.java` - Alert 领域模型

**统计领域**:
- `domain/model/statistics/TraceStatistics.java` - TraceStatistics 领域模型

**调试领域**:
- `domain/model/debug/DebugSession.java` - DebugSession 领域模型

**特点**:
- 使用 `@Data` 注解
- 包含静态工厂方法
- 包含业务方法
- 遵循项目规范

### 4. 基础设施层 Entity ✅
- `infrastructure/store/db/entity/SpanEntity.java` - Span Entity
- `infrastructure/store/db/entity/TraceEntity.java` - Trace Entity

**特点**:
- 使用 MyBatis-Plus 注解
- 使用 `JacksonTypeHandler` 处理 JSON 字段
- 使用 `FieldFill.INSERT` 自动填充

### 5. Span 处理器 ✅
- `infrastructure/grpc/SpanProcessor.java` - OTLP Span 解码处理器

## 二、待实现工作清单

### 1. Repository 层（优先级：高）

#### Repository 接口
```
application/port/out/repositories/
├── SpanRepository.java
├── TraceRepository.java
├── MetricRepository.java
└── AlertRepository.java
```

#### Mapper 接口
```
infrastructure/store/db/mapper/
├── SpanMapper.java
├── TraceMapper.java
├── MetricMapper.java
└── AlertMapper.java
```

#### Repository 实现
```
infrastructure/store/db/repository/
├── SpanRepositoryImpl.java
├── TraceRepositoryImpl.java
├── MetricRepositoryImpl.java
└── AlertRepositoryImpl.java
```

### 2. 应用层 UseCase（优先级：高）

#### UseCase
```
application/usecase/
├── SpanUseCase.java
├── TraceUseCase.java
├── MetricUseCase.java
├── AlertUseCase.java
└── DebugUseCase.java
```

#### Command
```
application/command/
├── SpanQueryCommand.java
├── TraceQueryCommand.java
└── MetricQueryCommand.java
```

#### DTO
```
application/dto/
├── SpanOutput.java
├── TraceOutput.java
├── MetricOutput.java
└── AlertOutput.java
```

### 3. 表现层 Controller（优先级：高）

#### Controller
```
api/controller/
├── SpanController.java
├── TraceController.java
├── MetricController.java
├── AlertController.java
└── DebugController.java
```

#### Request/Response DTO
```
api/dto/
├── SpanQueryRequest.java
├── SpanResponse.java
├── TraceResponse.java
├── MetricResponse.java
└── AlertResponse.java
```

### 4. 前端实现（优先级：中）

#### API 层
```
web/src/api/
├── span.ts
├── trace.ts
├── metric.ts
└── alert.ts
```

#### 类型定义
```
web/src/types/
├── span.ts
├── trace.ts
└── metric.ts
```

#### 视图页面
```
web/src/views/
├── trace/
│   ├── TraceList.vue
│   └── TraceDetail.vue
└── monitor/
    ├── MetricDashboard.vue
    └── AlertList.vue
```

#### 组件
```
web/src/components/
├── trace/
│   ├── TraceTree.vue
│   └── SpanDetail.vue
└── monitor/
    ├── MetricCard.vue
    └── AlertItem.vue
```

## 三、实施建议

### 方案 A：完整实施（推荐）
按照以下顺序逐步完成：
1. 完成 Repository 层（1-2 小时）
2. 完成 UseCase 层（2-3 小时）
3. 完成 Controller 层（1-2 小时）
4. 完成前端实现（3-4 小时）

**优点**: 功能完整，可立即使用
**缺点**: 工作量较大

### 方案 B：核心功能优先
先实现核心功能：
1. Span 追踪功能（Repository + UseCase + Controller）
2. 前端追踪页面
3. 其他功能后续补充

**优点**: 快速看到成果
**缺点**: 功能不完整

### 方案 C：分阶段交付
第一阶段：
- 后端 API 完成
- 基础前端页面

第二阶段：
- 完善前端交互
- 添加图表和可视化

**优点**: 渐进式交付
**缺点**: 需要多次迭代

## 四、关键代码示例

### Repository 接口示例
```java
package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.trace.Span;
import java.util.List;
import java.util.Optional;

public interface SpanRepository {
    Span save(Span span);
    Optional<Span> findById(String id);
    Optional<Span> findBySpanId(String spanId);
    List<Span> findByTraceId(String traceId);
    List<Span> findAll();
    void deleteById(String id);
}
```

### UseCase 示例
```java
package com.agenthub.application.usecase;

import com.agenthub.application.dto.SpanOutput;
import com.agenthub.application.port.out.repositories.SpanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SpanUseCase {
    private final SpanRepository repository;

    public SpanOutput get(String spanId) {
        return repository.findBySpanId(spanId)
            .map(this::toOutput)
            .orElseThrow(() -> new NotFoundException("Span not found"));
    }

    public List<SpanOutput> listByTrace(String traceId) {
        return repository.findByTraceId(traceId).stream()
            .map(this::toOutput)
            .toList();
    }

    private SpanOutput toOutput(Span span) {
        // 转换逻辑
    }
}
```

### Controller 示例
```java
package com.agenthub.api.controller;

import com.agenthub.api.dto.SpanResponse;
import com.agenthub.application.usecase.SpanUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spans")
public class SpanController {
    private final SpanUseCase useCase;

    public SpanController(SpanUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/{spanId}")
    public SpanResponse get(@PathVariable String spanId) {
        return toResponse(useCase.get(spanId));
    }

    @GetMapping("/traces/{traceId}")
    public List<SpanResponse> listByTrace(@PathVariable String traceId) {
        return useCase.listByTrace(traceId).stream()
            .map(this::toResponse)
            .toList();
    }

    private SpanResponse toResponse(SpanOutput output) {
        // 转换逻辑
    }
}
```

### 前端 API 示例
```typescript
// web/src/api/span.ts
import request from '@/utils/request';

export const spanApi = {
  get: (spanId: string) =>
    request.get(`/api/v1/spans/${spanId}`),

  listByTrace: (traceId: string) =>
    request.get(`/api/v1/spans/traces/${traceId}`),
};
```

### 前端组件示例
```vue
<!-- web/src/views/trace/TraceList.vue -->
<template>
  <div class="trace-list">
    <el-table :data="spans">
      <el-table-column prop="spanId" label="Span ID" />
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="latencyNs" label="Latency" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { spanApi } from '@/api/span';

const spans = ref([]);

onMounted(async () => {
  spans.value = await spanApi.listByTrace(traceId);
});
</script>
```

## 五、下一步行动

建议立即执行以下步骤：

1. **创建 Repository 接口和实现**
   - 参考现有的 AgentRepository
   - 使用 MyBatis-Plus 的 BaseMapper

2. **创建 UseCase**
   - 参考现有的 AgentUseCase
   - 实现基本的 CRUD 操作

3. **创建 Controller**
   - 参考现有的 AgentConfigController
   - 遵循 RESTful 规范

4. **创建前端页面**
   - 参考现有的页面结构
   - 使用 Element Plus 组件

## 六、总结

已完成核心的架构设计和基础代码实现，包括：
- ✅ 完整的方案设计
- ✅ 数据库表结构
- ✅ 领域模型（6个）
- ✅ Entity（2个）
- ✅ Span 处理器

待完成工作：
- ⏳ Repository 层（接口 + 实现）
- ⏳ UseCase 层（5个 UseCase）
- ⏳ Controller 层（5个 Controller）
- ⏳ 前端实现（API + 页面 + 组件）

预计剩余工作量：8-12 小时
