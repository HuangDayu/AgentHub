# 工作流执行引擎实现方案

## 一、概述

### 1.1 目标

实现一个基于DAG（有向无环图）的工作流执行引擎，支持AI Agent工作流的编排、执行、监控和管理。该引擎将集成到AgentHub项目中，遵循整洁架构原则，提供完整的前后端功能。

### 1.2 核心特性

- **DAG编排**：支持可视化拖拽编排工作流节点
- **多种节点类型**：支持LLM、API、条件判断、循环、并行等节点
- **实时执行**：通过SSE实现执行状态的实时推送
- **变量系统**：支持跨节点的变量引用和传递
- **错误处理**：支持重试、Try-Catch、失败分支等容错策略
- **状态持久化**：支持工作流执行状态的持久化和恢复
- **调试支持**：支持部分图执行、断点调试

### 1.3 技术栈

**后端**：
- Java 21
- Spring Boot 4.1.0-M2
- Spring AI 2.0.0-M4
- JGraphT 1.5.2（DAG图处理）
- Redis（状态缓存）
- MyBatis-Plus（持久化）

**前端**：
- Vue 3.5
- TypeScript 5.8
- Vue Flow（DAG可视化编辑器）
- Pinia（状态管理）

---

## 二、架构设计

### 2.1 整洁架构分层

工作流引擎将严格遵循AgentHub的整洁架构分层：

```
┌─────────────────────────────────────────────────────────────────────┐
│                        api (接口适配层)                               │
│   WorkflowController · WorkflowDebugController                       │
│   WorkflowRequest/Response DTO                                       │
└─────────────────────────────────────────────────────────────────────┘
                                ↓ 依赖
┌─────────────────────────────────────────────────────────────────────┐
│                    application (应用层)                               │
│   WorkflowUseCase · WorkflowExecutionUseCase                         │
│   WorkflowCommand · WorkflowExecutionCommand                         │
│   Port: WorkflowExecutionPort · WorkflowStatePort                    │
└─────────────────────────────────────────────────────────────────────┘
                                ↓ 依赖
┌─────────────────────────────────────────────────────────────────────┐
│                      domain (领域层)                                  │
│   Workflow · WorkflowNode · WorkflowEdge                             │
│   WorkflowContext · WorkflowResult · NodeResult                      │
│   WorkflowStatus · NodeStatus                                        │
└─────────────────────────────────────────────────────────────────────┘
                                ↓ 依赖
┌─────────────────────────────────────────────────────────────────────┐
│                  infrastructure (基础设施层)                          │
│   workflow/                                                          │
│     ├── engine/          # 工作流引擎核心                             │
│     ├── processor/       # 节点处理器                                 │
│     ├── state/           # 状态管理                                   │
│     ├── variable/        # 变量系统                                   │
│     └── adapter/         # 端口适配器                                 │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 核心组件设计

#### 2.2.1 领域模型（Domain层）

**位置**：`com.agenthub.domain.model.workflow`

**核心模型**：

```java
// 工作流聚合根
public class Workflow {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String workflowCode;
    private String name;
    private String description;
    private WorkflowGraph graph;          // DAG图定义
    private WorkflowStatus status;
    private WorkflowConfig config;        // 工作流配置
    private Instant createdAt;
    private Instant updatedAt;
    
    // 业务行为
    public void publish() { ... }
    public void unpublish() { ... }
    public void validate() { ... }
}

// 工作流图（值对象）
public class WorkflowGraph {
    private List<WorkflowNode> nodes;
    private List<WorkflowEdge> edges;
    
    public DirectedAcyclicGraph<String, DefaultEdge> toDag() { ... }
    public List<String> topologicalSort() { ... }
}

// 工作流节点（实体）
public class WorkflowNode {
    private String id;
    private String name;
    private String description;
    private NodeType type;
    private NodeConfig config;
    private NodePosition position;
}

// 节点类型枚举
public enum NodeType {
    START,          // 开始节点
    END,            // 结束节点
    LLM,            // 大语言模型节点
    API,            // API调用节点
    CONDITION,      // 条件判断节点
    LOOP,           // 循环节点
    PARALLEL,       // 并行节点
    SCRIPT,         // 脚本节点
    RETRIEVAL,      // 知识检索节点
    TOOL,           // 工具调用节点
    VARIABLE_ASSIGN // 变量赋值节点
}

// 工作流边（值对象）
public class WorkflowEdge {
    private String id;
    private String sourceNodeId;
    private String sourceHandle;
    private String targetNodeId;
    private String targetHandle;
}

// 工作流执行上下文
public class WorkflowContext {
    private String workflowId;
    private String executionId;
    private Map<String, Object> userVariables;      // 用户变量
    private Map<String, Object> systemVariables;    // 系统变量
    private Map<String, Object> sessionVariables;   // 会话变量
    private Map<String, NodeResult> nodeResults;    // 节点执行结果
    private List<String> executionOrder;            // 执行顺序
    private long version;                           // 版本号（乐观锁）
}

// 节点执行结果
public class NodeResult {
    private String nodeId;
    private String nodeName;
    private NodeType nodeType;
    private NodeStatus status;
    private Map<String, Object> input;
    private Map<String, Object> output;
    private String errorInfo;
    private Long duration;
    private Instant startTime;
    private Instant endTime;
}
```

#### 2.2.2 应用层（Application层）

**位置**：`com.agenthub.application`

**用例设计**：

```java
// 工作流管理用例
@Component
public class WorkflowUseCase {
    private final WorkflowRepository repository;
    
    public WorkflowOutput create(WorkflowCommand command) { ... }
    public WorkflowOutput update(String id, WorkflowCommand command) { ... }
    public WorkflowOutput publish(String id) { ... }
    public void delete(String id) { ... }
}

// 工作流执行用例
@Component
public class WorkflowExecutionUseCase {
    private final WorkflowExecutionPort executionPort;
    private final WorkflowStatePort statePort;
    
    public String initExecution(ExecutionInitCommand command) { ... }
    public Flux<NodeExecutionEvent> execute(ExecutionCommand command) { ... }
    public void stopExecution(String executionId) { ... }
    public ExecutionResult getExecutionResult(String executionId) { ... }
}
```

**端口设计**：

```java
// 工作流执行端口（输出端口）
public interface WorkflowExecutionPort {
    Flux<NodeExecutionEvent> executeWorkflow(WorkflowGraph graph, WorkflowContext context);
    void stopExecution(String executionId);
}

// 工作流状态端口（输出端口）
public interface WorkflowStatePort {
    void saveContext(String executionId, WorkflowContext context);
    Optional<WorkflowContext> loadContext(String executionId);
    void saveNodeResult(String executionId, NodeResult result);
    List<NodeResult> loadNodeResults(String executionId);
}
```

#### 2.2.3 基础设施层（Infrastructure层）

**位置**：`com.agenthub.infrastructure.workflow`

**包结构**：

```
workflow/
├── engine/                    # 工作流引擎核心
│   ├── WorkflowEngine.java              # 工作流引擎主类
│   ├── WorkflowExecutor.java            # 工作流执行器
│   ├── DagBuilder.java                  # DAG构建器
│   └── ExecutionScheduler.java          # 执行调度器
├── processor/                 # 节点处理器
│   ├── NodeProcessor.java               # 节点处理器接口
│   ├── AbstractNodeProcessor.java       # 抽象节点处理器
│   └── impl/                            # 处理器实现
│       ├── StartNodeProcessor.java
│       ├── EndNodeProcessor.java
│       ├── LlmNodeProcessor.java
│       ├── ApiNodeProcessor.java
│       ├── ConditionNodeProcessor.java
│       ├── LoopNodeProcessor.java
│       ├── ParallelNodeProcessor.java
│       ├── ScriptNodeProcessor.java
│       ├── RetrievalNodeProcessor.java
│       ├── ToolNodeProcessor.java
│       └── VariableAssignNodeProcessor.java
├── state/                     # 状态管理
│   ├── WorkflowStateManager.java        # 状态管理器
│   ├── RedisStateStorage.java           # Redis状态存储
│   └── DatabaseStateStorage.java        # 数据库状态存储
├── variable/                  # 变量系统
│   ├── VariableResolver.java            # 变量解析器
│   ├── VariableEvaluator.java           # 变量求值器
│   └── ExpressionParser.java            # 表达式解析器
├── adapter/                   # 端口适配器
│   ├── WorkflowExecutionAdapter.java    # 执行端口适配器
│   └── WorkflowStateAdapter.java        # 状态端口适配器
└── config/                    # 配置
    └── WorkflowEngineConfig.java        # 引擎配置
```

---

## 三、核心功能实现方案

### 3.1 DAG图构建与验证

**实现类**：`DagBuilder`

**功能**：
1. 从WorkflowGraph构建JGraphT的DirectedAcyclicGraph
2. 验证图的合法性（无环、有开始节点、有结束节点）
3. 计算拓扑排序
4. 识别可并行执行的节点

**实现要点**：

```java
@Component
public class DagBuilder {
    
    /**
     * 构建DAG图
     */
    public DirectedAcyclicGraph<String, DefaultEdge> build(WorkflowGraph graph) {
        validateGraph(graph);
        DirectedAcyclicGraph<String, DefaultEdge> dag = createDag();
        addNodes(dag, graph.getNodes());
        addEdges(dag, graph.getEdges());
        return dag;
    }
    
    /**
     * 验证图的合法性
     */
    private void validateGraph(WorkflowGraph graph) {
        validateNoCycle(graph);
        validateHasStartNode(graph);
        validateHasEndNode(graph);
        validateNodeConnections(graph);
    }
    
    /**
     * 获取拓扑排序
     */
    public List<String> topologicalSort(DirectedAcyclicGraph<String, DefaultEdge> dag) {
        return new TopologicalOrderIterator<>(dag).stream().toList();
    }
    
    /**
     * 获取可并行执行的节点组
     */
    public List<Set<String>> getParallelGroups(DirectedAcyclicGraph<String, DefaultEdge> dag) {
        // 基于入度计算可并行执行的节点组
    }
}
```

### 3.2 节点处理器设计

**设计模式**：策略模式 + 模板方法模式

**处理器接口**：

```java
public interface NodeProcessor {
    /**
     * 处理节点
     */
    NodeResult process(WorkflowNode node, WorkflowContext context);
    
    /**
     * 获取支持的节点类型
     */
    NodeType getSupportedType();
}
```

**抽象处理器**：

```java
public abstract class AbstractNodeProcessor implements NodeProcessor {
    
    @Override
    public final NodeResult process(WorkflowNode node, WorkflowContext context) {
        NodeResult result = initResult(node);
        try {
            preProcess(node, context);
            result = doProcess(node, context);
            postProcess(node, context, result);
        } catch (Exception e) {
            result = handleException(node, context, e);
        }
        return result;
    }
    
    /**
     * 子类实现具体处理逻辑
     */
    protected abstract NodeResult doProcess(WorkflowNode node, WorkflowContext context);
    
    /**
     * 前置处理
     */
    protected void preProcess(WorkflowNode node, WorkflowContext context) {
        // 默认实现：验证参数、检查依赖
    }
    
    /**
     * 后置处理
     */
    protected void postProcess(WorkflowNode node, WorkflowContext context, NodeResult result) {
        // 默认实现：更新上下文、记录结果
    }
    
    /**
     * 异常处理
     */
    protected NodeResult handleException(WorkflowNode node, WorkflowContext context, Exception e) {
        // 应用Try-Catch策略
    }
}
```

**具体处理器示例**：

```java
@Component
public class LlmNodeProcessor extends AbstractNodeProcessor {
    private final AgentChatPort agentChatPort;
    
    @Override
    protected NodeResult doProcess(WorkflowNode node, WorkflowContext context) {
        LlmNodeConfig config = parseConfig(node.getConfig());
        String prompt = resolvePrompt(config.getPrompt(), context);
        Flux<Message> response = agentChatPort.streamMessages(config.getAgentId(), prompt);
        return buildResult(node, response);
    }
    
    @Override
    public NodeType getSupportedType() {
        return NodeType.LLM;
    }
}
```

### 3.3 工作流执行引擎

**核心类**：`WorkflowEngine`

**执行流程**：

```
1. 初始化执行上下文
   ├── 加载工作流定义
   ├── 构建DAG图
   ├── 初始化变量系统
   └── 创建执行上下文

2. 执行工作流
   ├── 获取拓扑排序
   ├── 按顺序执行节点
   │   ├── 检查节点是否可执行
   │   ├── 选择节点处理器
   │   ├── 执行节点
   │   ├── 更新执行状态
   │   └── 发送执行事件
   └── 返回执行结果

3. 处理并行节点
   ├── 识别可并行执行的节点组
   ├── 使用并行流执行
   └── 合并执行结果

4. 处理条件分支
   ├── 计算条件表达式
   ├── 选择执行分支
   └── 跳过非执行分支节点

5. 处理循环节点
   ├── 初始化循环变量
   ├── 迭代执行循环体
   └── 收集循环结果
```

**实现要点**：

```java
@Component
public class WorkflowEngine {
    private final DagBuilder dagBuilder;
    private final Map<NodeType, NodeProcessor> processorMap;
    private final WorkflowStateManager stateManager;
    private final VariableResolver variableResolver;
    
    /**
     * 执行工作流（流式）
     */
    public Flux<NodeExecutionEvent> execute(WorkflowGraph graph, WorkflowContext context) {
        DirectedAcyclicGraph<String, DefaultEdge> dag = dagBuilder.build(graph);
        List<String> executionOrder = dagBuilder.topologicalSort(dag);
        
        return Flux.fromIterable(executionOrder)
            .flatMap(nodeId -> executeNode(dag, nodeId, context))
            .doOnNext(event -> stateManager.saveEvent(event))
            .doOnComplete(() -> stateManager.markCompleted(context.getExecutionId()));
    }
    
    /**
     * 执行单个节点
     */
    private Mono<NodeExecutionEvent> executeNode(
        DirectedAcyclicGraph<String, DefaultEdge> dag, 
        String nodeId, 
        WorkflowContext context
    ) {
        WorkflowNode node = findNode(nodeId);
        NodeProcessor processor = processorMap.get(node.getType());
        
        return Mono.fromCallable(() -> {
            NodeResult result = processor.process(node, context);
            return buildEvent(node, result);
        });
    }
}
```

### 3.4 变量系统

**变量作用域**：

```
${user.xxx}        - 用户输入参数
${sys.xxx}         - 系统参数（query, history_list等）
${session.xxx}     - 会话变量（跨请求持久化）
${nodeId.xxx}      - 节点输出引用
${conversation.xxx} - 会话参数
```

**变量解析器**：

```java
@Component
public class VariableResolver {
    
    /**
     * 解析变量引用
     */
    public Object resolve(String expression, WorkflowContext context) {
        if (!isVariableReference(expression)) {
            return expression;
        }
        
        String variableName = extractVariableName(expression);
        String[] parts = variableName.split("\\.");
        
        return switch (parts[0]) {
            case "user" -> resolveUserVariable(parts[1], context);
            case "sys" -> resolveSystemVariable(parts[1], context);
            case "session" -> resolveSessionVariable(parts[1], context);
            case "conversation" -> resolveConversationVariable(parts[1], context);
            default -> resolveNodeOutput(parts[0], parts[1], context);
        };
    }
    
    /**
     * 解析模板字符串
     */
    public String resolveTemplate(String template, WorkflowContext context) {
        // 使用正则表达式匹配 ${xxx} 并替换
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variable = matcher.group(1);
            Object value = resolve(variable, context);
            matcher.appendReplacement(result, String.valueOf(value));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
}
```

### 3.5 状态管理

**状态存储策略**：
- **Redis**：实时执行状态、临时变量
- **Database**：持久化执行历史、执行结果

**状态管理器**：

```java
@Component
public class WorkflowStateManager {
    private final RedisStateStorage redisStorage;
    private final DatabaseStateStorage dbStorage;
    
    /**
     * 保存执行上下文
     */
    public void saveContext(String executionId, WorkflowContext context) {
        String key = buildContextKey(executionId);
        redisStorage.set(key, context, Duration.ofHours(24));
    }
    
    /**
     * 加载执行上下文
     */
    public Optional<WorkflowContext> loadContext(String executionId) {
        String key = buildContextKey(executionId);
        return redisStorage.get(key, WorkflowContext.class);
    }
    
    /**
     * 保存节点执行结果
     */
    public void saveNodeResult(String executionId, NodeResult result) {
        // 保存到Redis（实时）
        redisStorage.hset(buildResultKey(executionId), result.getNodeId(), result);
        
        // 异步保存到数据库（持久化）
        CompletableFuture.runAsync(() -> 
            dbStorage.saveNodeResult(executionId, result)
        );
    }
}
```

### 3.6 错误处理机制

**重试策略**：

```java
public class RetryStrategy {
    private int maxRetries;
    private Duration delay;
    private double backoffMultiplier;
    
    public NodeResult executeWithRetry(Supplier<NodeResult> action) {
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount <= maxRetries) {
            try {
                return action.get();
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                if (retryCount <= maxRetries) {
                    sleep(calculateDelay(retryCount));
                }
            }
        }
        
        throw new WorkflowExecutionException("Max retries exceeded", lastException);
    }
    
    private Duration calculateDelay(int retryCount) {
        long delayMs = (long) (delay.toMillis() * Math.pow(backoffMultiplier, retryCount - 1));
        return Duration.ofMillis(delayMs);
    }
}
```

**Try-Catch策略**：

```java
public enum TryCatchStrategy {
    DEFAULT_VALUE,      // 返回默认值
    FAIL_BRANCH,        // 走失败分支
    NOOP               // 忽略错误继续执行
}

public class TryCatchHandler {
    
    public NodeResult handle(NodeResult result, TryCatchStrategy strategy, Object defaultValue) {
        if (result.getStatus() == NodeStatus.SUCCESS) {
            return result;
        }
        
        return switch (strategy) {
            case DEFAULT_VALUE -> buildDefaultResult(result, defaultValue);
            case FAIL_BRANCH -> buildFailBranchResult(result);
            case NOOP -> buildNoopResult(result);
        };
    }
}
```

---

## 四、前端实现方案

### 4.1 DAG可视化编辑器

**技术选型**：Vue Flow（基于React Flow的Vue版本）

**核心组件**：

```typescript
// WorkflowEditor.vue
<template>
  <div class="workflow-editor">
    <VueFlow
      v-model:nodes="nodes"
      v-model:edges="edges"
      @nodes-change="onNodesChange"
      @edges-change="onEdgesChange"
      @connect="onConnect"
    >
      <!-- 节点类型 -->
      <template #node-start="nodeProps">
        <StartNode :data="nodeProps.data" />
      </template>
      <template #node-llm="nodeProps">
        <LlmNode :data="nodeProps.data" />
      </template>
      <template #node-condition="nodeProps">
        <ConditionNode :data="nodeProps.data" />
      </template>
      <!-- ... 其他节点类型 -->
    </VueFlow>
    
    <!-- 节点面板 -->
    <NodePanel @drag-node="onDragNode" />
    
    <!-- 属性面板 -->
    <PropertyPanel :selected-node="selectedNode" @update="onUpdateNode" />
  </div>
</template>
```

**节点类型定义**：

```typescript
// types/workflow-node.ts
export interface WorkflowNode {
  id: string
  type: NodeType
  position: { x: number; y: number }
  data: NodeData
}

export interface NodeData {
  label: string
  config: NodeConfig
  status?: NodeStatus
  result?: NodeResult
}

export type NodeType = 
  | 'start'
  | 'end'
  | 'llm'
  | 'api'
  | 'condition'
  | 'loop'
  | 'parallel'
  | 'script'
  | 'retrieval'
  | 'tool'
  | 'variable_assign'
```

### 4.2 工作流执行与调试

**执行控制组件**：

```typescript
// WorkflowExecution.vue
<template>
  <div class="workflow-execution">
    <!-- 执行控制栏 -->
    <div class="execution-controls">
      <el-button @click="startExecution" :disabled="isExecuting">
        开始执行
      </el-button>
      <el-button @click="stopExecution" :disabled="!isExecuting">
        停止执行
      </el-button>
      <el-button @click="debugExecution">
        调试执行
      </el-button>
    </div>
    
    <!-- 执行状态面板 -->
    <div class="execution-status">
      <div v-for="node in nodes" :key="node.id" class="node-status">
        <NodeStatusIndicator :status="node.data.status" />
        <span>{{ node.data.label }}</span>
      </div>
    </div>
    
    <!-- 执行结果面板 -->
    <div class="execution-results">
      <NodeResultPanel
        v-for="result in nodeResults"
        :key="result.nodeId"
        :result="result"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useWorkflowExecution } from '@/composables/useWorkflowExecution'

const { 
  startExecution, 
  stopExecution, 
  isExecuting, 
  nodeResults 
} = useWorkflowExecution()

onUnmounted(() => {
  stopExecution()
})
</script>
```

**SSE事件处理**：

```typescript
// composables/useWorkflowExecution.ts
export function useWorkflowExecution() {
  const nodeResults = ref<Map<string, NodeResult>>(new Map())
  const isExecuting = ref(false)
  let eventSource: EventSource | null = null
  
  const startExecution = async () => {
    const executionId = await initExecution()
    
    eventSource = new EventSource(
      `/api/v1/workflows/${workflowId}/executions/${executionId}/stream`
    )
    
    eventSource.addEventListener('node_start', (event) => {
      const data = JSON.parse(event.data)
      updateNodeStatus(data.nodeId, 'executing')
    })
    
    eventSource.addEventListener('node_complete', (event) => {
      const data = JSON.parse(event.data)
      nodeResults.value.set(data.nodeId, data.result)
      updateNodeStatus(data.nodeId, data.result.status)
    })
    
    eventSource.addEventListener('workflow_complete', () => {
      isExecuting.value = false
      eventSource?.close()
    })
    
    isExecuting.value = true
  }
  
  const stopExecution = () => {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    isExecuting.value = false
  }
  
  return {
    startExecution,
    stopExecution,
    isExecuting,
    nodeResults
  }
}
```

### 4.3 API封装

**工作流API**：

```typescript
// api/workflow-api.ts
export interface Workflow {
  id: string
  tenantId: string
  workspaceId: string
  workflowCode: string
  name: string
  description: string
  graphDefinition: string
  status: string
  createdAt: string
  updatedAt: string
}

export async function listWorkflows(selection: Selection): Promise<Workflow[]> {
  return requestJson<Workflow[]>(`/api/v1/workspaces/${selection.workspaceId}/workflows`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function createWorkflow(
  selection: Selection,
  workflow: CreateWorkflowRequest
): Promise<Workflow> {
  return requestJson<Workflow>(`/api/v1/workspaces/${selection.workspaceId}/workflows`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
    bodyJson: workflow,
  })
}

export async function executeWorkflow(
  selection: Selection,
  workflowId: string,
  input: Record<string, any>
): Promise<string> {
  return requestJson<string>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/execute`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
      bodyJson: { input },
    }
  )
}
```

---

## 五、数据库设计

### 5.1 工作流表

```sql
CREATE TABLE workflow (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    workspace_id VARCHAR(64) NOT NULL,
    workflow_code VARCHAR(128) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description TEXT,
    graph_definition TEXT NOT NULL,
    config TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, workspace_id, workflow_code)
);

CREATE INDEX idx_workflow_tenant ON workflow(tenant_id);
CREATE INDEX idx_workflow_workspace ON workflow(workspace_id);
CREATE INDEX idx_workflow_status ON workflow(status);
```

### 5.2 工作流执行记录表

```sql
CREATE TABLE workflow_execution (
    id VARCHAR(64) PRIMARY KEY,
    workflow_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    execution_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    input TEXT,
    output TEXT,
    error_info TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workflow_id) REFERENCES workflow(id)
);

CREATE INDEX idx_execution_workflow ON workflow_execution(workflow_id);
CREATE INDEX idx_execution_status ON workflow_execution(status);
CREATE INDEX idx_execution_time ON workflow_execution(start_time);
```

### 5.3 节点执行结果表

```sql
CREATE TABLE node_execution_result (
    id VARCHAR(64) PRIMARY KEY,
    execution_id VARCHAR(64) NOT NULL,
    node_id VARCHAR(64) NOT NULL,
    node_name VARCHAR(256),
    node_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    input TEXT,
    output TEXT,
    error_info TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES workflow_execution(id)
);

CREATE INDEX idx_node_result_execution ON node_execution_result(execution_id);
CREATE INDEX idx_node_result_node ON node_execution_result(node_id);
```

---

## 六、实现步骤

### 6.1 第一阶段：领域模型和基础设施（后端）

**任务列表**：

1. **创建领域模型**
   - [ ] Workflow聚合根
   - [ ] WorkflowGraph值对象
   - [ ] WorkflowNode实体
   - [ ] WorkflowEdge值对象
   - [ ] WorkflowContext上下文
   - [ ] NodeResult结果对象
   - [ ] 枚举定义（NodeType、NodeStatus、WorkflowStatus）

2. **创建应用层**
   - [ ] WorkflowUseCase
   - [ ] WorkflowExecutionUseCase
   - [ ] WorkflowCommand
   - [ ] ExecutionCommand
   - [ ] WorkflowOutput
   - [ ] ExecutionOutput
   - [ ] 端口接口（WorkflowExecutionPort、WorkflowStatePort）

3. **创建基础设施层**
   - [ ] DagBuilder（DAG构建器）
   - [ ] WorkflowEngine（工作流引擎）
   - [ ] WorkflowExecutor（执行器）
   - [ ] NodeProcessor接口和抽象类
   - [ ] 基础节点处理器（Start、End）
   - [ ] WorkflowStateManager（状态管理器）
   - [ ] VariableResolver（变量解析器）
   - [ ] 端口适配器

4. **创建API层**
   - [ ] WorkflowController
   - [ ] WorkflowDebugController
   - [ ] DTO定义（Request、Response）
   - [ ] 异常处理

5. **创建数据库表**
   - [ ] workflow表
   - [ ] workflow_execution表
   - [ ] node_execution_result表
   - [ ] MyBatis Mapper

### 6.2 第二阶段：节点处理器实现（后端）

**任务列表**：

1. **LLM节点处理器**
   - [ ] LlmNodeProcessor
   - [ ] 支持流式响应
   - [ ] 支持历史记录
   - [ ] 支持系统提示词

2. **API节点处理器**
   - [ ] ApiNodeProcessor
   - [ ] 支持HTTP请求
   - [ ] 支持参数映射
   - [ ] 支持响应解析

3. **条件判断节点处理器**
   - [ ] ConditionNodeProcessor
   - [ ] 支持表达式求值
   - [ ] 支持多分支

4. **循环节点处理器**
   - [ ] LoopNodeProcessor
   - [ ] 支持迭代执行
   - [ ] 支持循环变量

5. **并行节点处理器**
   - [ ] ParallelNodeProcessor
   - [ ] 支持并行执行
   - [ ] 支持结果合并

6. **其他节点处理器**
   - [ ] ScriptNodeProcessor
   - [ ] RetrievalNodeProcessor
   - [ ] ToolNodeProcessor
   - [ ] VariableAssignNodeProcessor

### 6.3 第三阶段：前端实现

**任务列表**：

1. **DAG编辑器**
   - [ ] 集成Vue Flow
   - [ ] 实现节点拖拽
   - [ ] 实现连线功能
   - [ ] 实现节点配置面板

2. **节点组件**
   - [ ] StartNode组件
   - [ ] EndNode组件
   - [ ] LlmNode组件
   - [ ] ApiNode组件
   - [ ] ConditionNode组件
   - [ ] LoopNode组件
   - [ ] ParallelNode组件
   - [ ] 其他节点组件

3. **执行与调试**
   - [ ] 执行控制组件
   - [ ] 状态监控组件
   - [ ] 结果展示组件
   - [ ] SSE事件处理

4. **API封装**
   - [ ] workflow-api.ts
   - [ ] 类型定义

5. **页面集成**
   - [ ] WorkflowEditorView
   - [ ] WorkflowListView
   - [ ] WorkflowDetailView

### 6.4 第四阶段：测试与优化

**任务列表**：

1. **单元测试**
   - [ ] 领域模型测试
   - [ ] 节点处理器测试
   - [ ] 变量解析器测试
   - [ ] DAG构建器测试

2. **集成测试**
   - [ ] 工作流执行集成测试
   - [ ] API接口测试
   - [ ] 前后端集成测试

3. **性能优化**
   - [ ] 并行执行优化
   - [ ] 状态缓存优化
   - [ ] 前端渲染优化

4. **文档编写**
   - [ ] API文档
   - [ ] 使用指南
   - [ ] 开发指南

---

## 七、技术难点与解决方案

### 7.1 DAG循环检测

**问题**：如何高效检测工作流图中是否存在循环？

**解决方案**：
- 使用JGraphT的`DirectedAcyclicGraph`，在添加边时自动检测循环
- 如果检测到循环，抛出`CycleDetectedException`
- 提供可视化提示，帮助用户定位循环

### 7.2 并行节点执行

**问题**：如何高效执行可并行的节点？

**解决方案**：
- 基于拓扑排序和入度计算可并行执行的节点组
- 使用`Flux.flatMap`实现并行执行
- 使用`ForkJoinPool`控制并发度
- 合并执行结果时使用版本号解决冲突

### 7.3 变量引用解析

**问题**：如何高效解析复杂的变量引用表达式？

**解决方案**：
- 使用正则表达式匹配`${xxx}`模式
- 实现多级变量作用域（user、sys、session、node）
- 支持表达式求值（如`${node1.output.value + 10}`）
- 缓存解析结果提高性能

### 7.4 状态持久化与恢复

**问题**：如何保证工作流执行状态的可靠持久化和恢复？

**解决方案**：
- 使用Redis存储实时状态，设置合理的过期时间
- 使用数据库持久化执行历史
- 实现乐观锁机制，使用版本号解决并发冲突
- 提供工作流恢复接口，支持从断点继续执行

### 7.5 SSE连接管理

**问题**：如何管理大量的SSE连接，避免资源泄漏？

**解决方案**：
- 使用Spring的`SseEmitter`，设置合理的超时时间
- 实现连接池管理，限制最大连接数
- 在工作流执行完成或出错时主动关闭连接
- 前端实现自动重连机制

---

## 八、性能指标

### 8.1 性能目标

- **节点执行延迟**：< 100ms（不含外部调用）
- **并行执行加速比**：接近线性加速（理想情况下）
- **状态持久化延迟**：< 50ms
- **SSE事件推送延迟**：< 100ms
- **前端渲染帧率**：> 30 FPS

### 8.2 容量规划

- **单工作流最大节点数**：100个
- **单节点最大输入参数**：50个
- **单节点最大输出参数**：50个
- **并发执行工作流数**：100个
- **SSE连接数**：1000个

---

## 九、安全考虑

### 9.1 权限控制

- 工作流创建、编辑、删除需要相应权限
- 工作流执行需要执行权限
- 租户隔离：不同租户的工作流相互隔离
- 工作空间隔离：不同工作空间的工作流相互隔离

### 9.2 输入验证

- 验证工作流图的合法性
- 验证节点配置的有效性
- 验证变量引用的安全性
- 防止脚本注入攻击

### 9.3 敏感信息保护

- API密钥等敏感信息加密存储
- 执行日志脱敏处理
- 变量值敏感信息过滤

---

## 十、监控与运维

### 10.1 监控指标

- 工作流执行次数
- 工作流执行成功率
- 工作流执行平均时长
- 节点执行失败率
- 并发执行数
- SSE连接数

### 10.2 日志记录

- 工作流创建、更新、删除日志
- 工作流执行开始、完成日志
- 节点执行开始、完成、失败日志
- 异常日志

### 10.3 告警规则

- 工作流执行失败率超过阈值
- 节点执行超时
- SSE连接数超过阈值
- 系统资源使用率过高

---

## 十一、总结

本方案设计了一个完整的、符合整洁架构原则的工作流执行引擎，包括：

1. **清晰的分层架构**：严格遵循整洁架构，依赖反转，核心业务逻辑独立
2. **强大的功能特性**：支持多种节点类型、并行执行、变量系统、错误处理
3. **高性能设计**：基于DAG的并行执行、状态缓存、流式响应
4. **易用的前端界面**：可视化DAG编辑器、实时执行监控、调试支持
5. **可靠的运维保障**：状态持久化、监控告警、安全控制

通过本方案的实施，AgentHub将具备强大的工作流编排能力，支持复杂的AI应用场景，为用户提供灵活、高效、可靠的AI Agent工作流管理能力。
