# 自主决策 Agent 技术方案

> 日期：2026-05-30
> 状态：待审核

## 1. 目标

将 AgentHub 从"配置驱动的执行器"演进为"自主决策的智能体"。

**现状**：管理员配置模型、工具、知识库 → Agent 创建时固化 → Agent 按配置执行。
**目标**：管理员配置可用资源目录 → Agent 运行时自主决策用什么、怎么用、用多少个子 Agent。

核心原则：**一切都是数据和工具**。Agent 通过工具查询可用资源，通过工具创建子 Agent，通过工具规划执行流程。不需要新的运行时框架，不需要修改 Agent 构建生命周期。

## 2. 架构约束

### 2.1 多框架兼容

项目支持两套 Agent 运行时（Spring AI Alibaba + AgentScope Harness），后续可能更多。所有改动必须在 **SystemTools 层** 实现，不侵入任何特定框架的 Agent 实现。

```
Agent (框架无关)
  └── SystemTools (所有自主决策能力在此层实现)
        ├── PlanTools          ← 新增：执行计划管理
        ├── SubagentManageTools ← 增强：动态委派
        ├── ModelTools          ← 增强：模型选择决策支持
        ├── KnowledgeTools      ← 增强：知识库选择决策支持
        └── ... 其他现有工具
```

### 2.2 Agent 构建时序

Agent 对象在创建时注入所有 ToolCallback，之后不可变。这意味着：
- 不能运行时给 Agent 添加新工具
- 但 Agent 可以通过已有工具 **查询** 可用资源，然后通过已有工具 **操作** 这些资源
- 动态能力通过工具组合实现，而非运行时重配置

### 2.3 整洁架构

所有新增代码严格遵守四层架构：
- `domain/model/plan/` — 执行计划领域模型
- `application/usecase/` — 计划管理用例
- `application/port/out/` — 输出端口
- `infrastructure/tools/system_tools/` — SystemTools 实现

## 3. 设计方案

### 3.1 执行计划领域模型

```
domain/model/plan/
├── ExecutionPlan.java        // 执行计划聚合根
├── PlanStep.java             // 计划步骤
├── PlanStepStatus.java       // 步骤状态枚举
└── PlanStatus.java           // 计划状态枚举
```

**ExecutionPlan**:
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class ExecutionPlan {
    private String id;
    private String agentId;
    private String sessionId;
    private String goal;                    // 原始目标
    private String status;                  // PLANNING / EXECUTING / COMPLETED / FAILED
    private List<PlanStep> steps;
    private int currentStepIndex;           // 当前执行到第几步
    private String result;                  // 最终结果
    private Instant createdAt;
    private Instant updatedAt;
}
```

**PlanStep**:
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class PlanStep {
    private String id;
    private int order;                      // 执行顺序
    private String description;             // 步骤描述
    private String toolName;                // 使用的工具（可选，表示委派给子Agent时为空）
    private String toolInput;               // 工具调用参数（JSON）
    private String status;                  // PENDING / RUNNING / COMPLETED / FAILED / SKIPPED
    private String output;                  // 执行结果
    private String subagentId;              // 关联的子Agent ID（委派场景）
    private String subsessionId;            // 关联的子会话 ID
    private String dependsOn;               // 依赖的步骤ID列表（逗号分隔）
}
```

**状态枚举**:
```java
public enum PlanStatus {
    PLANNING, EXECUTING, COMPLETED, FAILED, CANCELLED
}

public enum PlanStepStatus {
    PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
}
```

### 3.2 计划管理用例

```
application/usecase/
├── ExecutionPlanUseCase.java           // 计划CRUD + 状态管理
├── command/CreatePlanCommand.java
├── command/AddPlanStepCommand.java
├── command/UpdatePlanStepCommand.java
├── dto/ExecutionPlanOutput.java
└── dto/PlanStepOutput.java
```

**ExecutionPlanUseCase** 职责：
- 创建/查询/更新执行计划
- 管理步骤状态流转
- 计算可执行步骤（依赖已满足的步骤）
- 标记计划完成/失败

不包含执行逻辑 —— 执行由 SystemTools 驱动。

### 3.3 PlanTools（新增）

```java
@AgentTools(name = "PlanTools", description = "执行计划工具，用于自主规划和跟踪任务执行")
public class PlanTools {

    @Tool(description = "创建执行计划，定义完成目标的步骤")
    ExecutionPlanOutput createPlan(String goal, List<PlanStepInput> steps, ToolContext ctx);

    @Tool(description = "获取当前执行计划")
    ExecutionPlanOutput getCurrentPlan(ToolContext ctx);

    @Tool(description = "添加步骤到当前计划")
    void addStep(PlanStepInput step, ToolContext ctx);

    @Tool(description = "更新步骤状态和输出")
    void updateStep(String stepId, String status, String output, ToolContext ctx);

    @Tool(description = "获取下一步可执行的步骤")
    List<PlanStepOutput> getNextSteps(ToolContext ctx);

    @Tool(description = "标记计划完成")
    void completePlan(String result, ToolContext ctx);

    @Tool(description = "取消当前计划")
    void cancelPlan(String reason, ToolContext ctx);
}
```

**工作流程**：
1. Agent 收到用户任务
2. 调用 `ModelTools.getModelConfigs()` 查看可用模型
3. 调用 `KnowledgeTools.getKnowledgeBases()` 查看可用知识库
4. 调用 `ToolTools.getHttpTools()` / `getMcpTools()` 等查看可用工具
5. 调用 `PlanTools.createPlan(goal, steps)` 创建执行计划
6. 循环：调用 `PlanTools.getNextSteps()` → 执行步骤 → `PlanTools.updateStep()`
7. 全部完成 → `PlanTools.completePlan(result)`

### 3.4 增强 SubagentManageTools

当前 `SubagentManageTools` 已支持创建子 Agent，需增强：

**新增输入参数**:
```java
@Data
public class CreateSubagentToolInput {
    private String name;
    private String systemPrompt;
    private String task;
    private List<String> tools;
    private String knowledgeIds;
    private String modelConfigId;
    // 新增
    private String planStepId;          // 关联的计划步骤ID
    private String priority;            // 优先级：HIGH / NORMAL / LOW
    private int timeoutSeconds;         // 超时时间（秒）
}
```

**新增方法**:
```java
@Tool(description = "等待子Agent完成并获取结果（阻塞直到完成或超时）")
SubagentRuntimeOutput awaitSubagent(String subagentId, String subsessionId,
                                     int timeoutSeconds, ToolContext ctx);

@Tool(description = "批量创建子Agent并等待全部完成")
List<SubagentRuntimeOutput> createSubagentsBatch(
    List<CreateSubagentToolInput> requests, ToolContext ctx);
```

### 3.5 增强 ModelTools

为 Agent 提供模型选择决策支持信息：

```java
// 新增方法
@Tool(description = "获取模型能力摘要（包含擅长领域、成本等级、速度等级）")
List<ModelCapabilitySummary> getModelCapabilities(ToolContext ctx);

@Tool(description = "根据任务特征推荐最适合的模型")
ModelRecommendation recommendModel(String taskDescription, String complexity,
                                     ToolContext ctx);
```

**ModelCapabilitySummary**:
```java
@Data
public class ModelCapabilitySummary {
    private String modelConfigId;
    private String modelName;
    private String supplier;
    private String擅长领域;        // e.g., "代码生成", "文本分析", "多模态"
    private String costLevel;      // LOW / MEDIUM / HIGH
    private String speedLevel;     // FAST / MEDIUM / SLOW
    private int maxTokens;
    private boolean available;     // 是否可用（API key 有效等）
}
```

### 3.6 增强 KnowledgeTools

为 Agent 提供知识库选择决策支持：

```java
// 新增方法
@Tool(description = "获取知识库摘要（包含文档数量、类型、更新时间）")
List<KnowledgeBaseSummary> getKnowledgeBaseSummaries(ToolContext ctx);

@Tool(description = "根据查询主题推荐最相关的知识库")
List<KnowledgeBaseRecommendation> recommendKnowledgeBase(String topic, ToolContext ctx);
```

**KnowledgeBaseSummary**:
```java
@Data
public class KnowledgeBaseSummary {
    private String knowledgeBaseId;
    private String name;
    private String description;
    private int documentCount;
    private String lastUpdatedAt;
    private List<String> supportedQueryTypes;  // e.g., "语义检索", "关键词检索"
}
```

### 3.7 动态系统提示词注入

在 `AgentContextUseCase.buildContext()` 中，系统提示词注入可用资源摘要：

```java
private String resolveSystemPrompt(List<AgentConfig> configs) {
    String promptId = findConfigId(configs, AgentConfigCategory.PROMPT, AgentConfigType.SYSTEM_PROMPT);
    if (promptId == null) return buildAutoPrompt(configs);
    String basePrompt = promptTemplateRepository.findById(promptId)
        .map(PromptTemplateInfo::getContent).orElse(null);
    return basePrompt != null ? basePrompt : buildAutoPrompt(configs);
}

private String buildAutoPrompt(List<AgentConfig> configs) {
    // 根据 AgentConfig 自动生成资源摘要提示词
    // 告诉 Agent 它有哪些工具、模型、知识库可用
}
```

**自动提示词模板**:
```
你是一个自主决策的 AI Agent。你可以使用以下工具来完成任务：

## 可用工具
{根据 AgentConfig 中 TOOL 类别自动生成}

## 可用模型
{根据 AgentConfig 中 MODEL 类别自动生成}

## 可用知识库
{根据 AgentConfig 中 KNOWLEDGE 类别自动生成}

## 工作方式
1. 分析任务，制定执行计划
2. 使用 PlanTools 创建执行计划
3. 逐步执行计划，使用合适的工具
4. 可以创建子Agent来并行处理子任务
5. 根据中间结果调整计划

## 重要原则
- 优先使用最合适的工具，不要手动完成可以工具化的任务
- 对于复杂任务，分解为子任务并行执行
- 如果某个步骤失败，分析原因并调整计划
```

### 3.8 计划持久化

**Port 接口**:
```java
public interface ExecutionPlanRepository {
    ExecutionPlan save(ExecutionPlan plan);
    Optional<ExecutionPlan> findById(String id);
    Optional<ExecutionPlan> findActiveBySessionId(String sessionId);
    List<ExecutionPlan> findByAgentId(String agentId);
    void deleteById(String id);
}
```

**Infrastructure 实现**: `MybatisExecutionPlanRepository` + `ExecutionPlanEntity` + `ExecutionPlanMapper`

**数据库表**:
```sql
CREATE TABLE execution_plan (
    id VARCHAR(36) PRIMARY KEY,
    agent_id VARCHAR(36) NOT NULL,
    session_id VARCHAR(36) NOT NULL,
    goal TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    current_step_index INT NOT NULL DEFAULT 0,
    result TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE plan_step (
    id VARCHAR(36) PRIMARY KEY,
    plan_id VARCHAR(36) NOT NULL REFERENCES execution_plan(id),
    step_order INT NOT NULL,
    description TEXT NOT NULL,
    tool_name VARCHAR(100),
    tool_input TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    output TEXT,
    subagent_id VARCHAR(36),
    subsession_id VARCHAR(36),
    depends_on VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

## 4. 不改动的部分

| 组件 | 原因 |
|------|------|
| Agent 运行时框架（Alibaba/AgentScope） | 框架无关，所有能力在 SystemTools 层 |
| Agent 构建生命周期（AgentContextUseCase） | 仅增强系统提示词，不改变构建时序 |
| AgentPoolUseCase | 缓存策略不变 |
| 现有 DAG 工作流引擎 | 保留作为静态编排选项，与自主规划并存 |
| SubagentEngine | 执行引擎不变，通过增强的 SubagentManageTools 调用 |
| AgentConfig 体系 | 管理员配置入口不变，只是 Agent 使用方式变化 |

## 5. 文件变更清单

### 新增文件
```
src/main/java/com/agenthub/
├── domain/model/plan/
│   ├── ExecutionPlan.java
│   ├── PlanStep.java
│   ├── PlanStatus.java
│   └── PlanStepStatus.java
├── application/usecase/
│   └── ExecutionPlanUseCase.java
├── application/command/
│   ├── CreatePlanCommand.java
│   ├── AddPlanStepCommand.java
│   └── UpdatePlanStepCommand.java
├── application/dto/
│   ├── ExecutionPlanOutput.java
│   └── PlanStepOutput.java
├── application/port/out/repositories/
│   └── ExecutionPlanRepository.java
├── infrastructure/store/db/entity/
│   ├── ExecutionPlanEntity.java
│   └── PlanStepEntity.java
├── infrastructure/store/db/mapper/
│   ├── ExecutionPlanMapper.java
│   └── PlanStepMapper.java
├── infrastructure/store/db/repository/
│   ├── MybatisExecutionPlanRepository.java
│   └── MybatisPlanStepRepository.java
├── infrastructure/tools/system_tools/base_tools/
│   └── PlanTools.java
├── infrastructure/tools/system_tools/data_tools/
│   ├── dto/ModelCapabilitySummary.java
│   ├── dto/ModelRecommendation.java
│   ├── dto/KnowledgeBaseSummary.java
│   └── dto/KnowledgeBaseRecommendation.java
└── infrastructure/tools/system_tools/base_tools/dto/
    ├── CreateSubagentToolInput.java  (修改：增加字段)
    └── PlanStepInput.java           (新增)
```

### 修改文件
```
src/main/java/com/agenthub/
├── infrastructure/tools/system_tools/data_tools/
│   ├── ModelTools.java          (增加 getModelCapabilities, recommendModel)
│   └── KnowledgeTools.java      (增加 getKnowledgeBaseSummaries, recommendKnowledgeBase)
├── infrastructure/tools/system_tools/base_tools/
│   └── SubagentManageTools.java (增加 awaitSubagent, createSubagentsBatch)
└── application/usecase/
    └── AgentContextUseCase.java  (增强系统提示词构建)
```

### 新增 SQL
```
sql/
└── V2__add_execution_plan_tables.sql
```

### 新增测试
```
src/test/java/com/agenthub/test/
├── integration/
│   ├── PlanToolsIntegrationTest.java
│   ├── ExecutionPlanUseCaseIntegrationTest.java
│   └── EnhancedModelToolsIntegrationTest.java
└── unit/
    ├── ExecutionPlanUseCaseTest.java
    └── PlanToolsTest.java
```

## 6. 实施顺序

| 阶段 | 内容 | 依赖 |
|------|------|------|
| **Phase 1** | 领域模型 + Repository + SQL | 无 |
| **Phase 2** | ExecutionPlanUseCase + 测试 | Phase 1 |
| **Phase 3** | PlanTools + 测试 | Phase 2 |
| **Phase 4** | 增强 ModelTools / KnowledgeTools + 测试 | 无（可并行） |
| **Phase 5** | 增强 SubagentManageTools + 测试 | Phase 3 |
| **Phase 6** | 系统提示词增强 + 集成测试 | Phase 3-5 |
| **Phase 7** | 全量测试 + ArchUnit 验证 | 全部 |

## 7. 测试策略

### 单元测试
- `ExecutionPlanUseCaseTest`: 计划状态流转、步骤依赖计算
- `PlanToolsTest`: 工具方法与 UseCase 的交互

### 集成测试
- `PlanToolsIntegrationTest`: 端到端计划创建→执行→完成
- `ExecutionPlanUseCaseIntegrationTest`: 持久化 + 状态管理
- `EnhancedModelToolsIntegrationTest`: 模型推荐逻辑

### ArchUnit 验证
- 所有新增类遵守分层依赖规则
- domain 层无框架依赖
- 工具层通过 UseCase 访问领域逻辑

### 约束
- 每个方法不超过 10 行
- 每个方法参数不超过 3 个
- 所有方法有 Javadoc（中文）
- 禁止使用 record 类和 @Builder
- 使用 BeanUtil.copyProperties 进行 DTO 转换
