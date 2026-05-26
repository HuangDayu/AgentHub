# 工作流管理功能使用指南

## 📖 概述

本项目的工作流管理功能参考了 spring-ai-alibaba 项目的实现，提供了完整的可视化工作流编辑、执行和调试能力。

## 🎯 核心功能

### 1. 可视化编辑器
- **拖拽式节点编辑**：从左侧节点面板拖拽节点到画布
- **节点连接**：通过拖拽连接节点之间的输入输出
- **实时预览**：所见即所得的编辑体验
- **自动布局**：一键优化节点布局

### 2. 丰富的节点类型
- **基础节点**：开始、结束、任务
- **AI节点**：大模型(LLM)、知识检索
- **逻辑节点**：条件判断、循环、并行、迭代器
- **集成节点**：API调用、脚本执行
- **数据节点**：变量处理、输入输出

### 3. 变量引用系统
- **跨节点变量引用**：使用 `${nodeId.outputKey}` 格式引用上游节点输出
- **变量验证**：实时验证变量引用的有效性
- **变量提示**：智能提示可用的变量列表

### 4. 工作流执行
- **实时执行**：一键执行工作流
- **进度监控**：实时查看每个节点的执行状态
- **结果查看**：查看每个节点的输入输出
- **错误处理**：清晰的错误信息展示

### 5. 调试功能
- **单步调试**：逐步执行工作流
- **断点设置**：在指定节点暂停执行
- **变量查看**：实时查看执行上下文中的变量

### 6. 版本管理
- **版本保存**：保存工作流的不同版本
- **版本回滚**：回滚到历史版本
- **版本对比**：对比不同版本的差异

## 🚀 快速开始

### 1. 创建工作流

```typescript
import { useWorkflowStore } from '@/stores/workflow-store'

const store = useWorkflowStore()

// 设置工作流基本信息
store.setWorkflowInfo('workflow-001', '我的工作流', '这是一个示例工作流')

// 添加节点
import { getNodeSchema } from '@/constants/node-schemas'

const llmSchema = getNodeSchema('llm')
const llmNode = {
  id: 'llm_1',
  type: 'llm',
  position: { x: 200, y: 200 },
  data: {
    label: '大模型调用',
    ...llmSchema.defaultParams
  }
}

store.addNode(llmNode)
```

### 2. 使用编辑器组件

```vue
<template>
  <WorkflowEditor
    :workflow-id="workflowId"
    :workflow-name="workflowName"
    :initial-graph="graphData"
    @save="handleSave"
    @test="handleTest"
  />
</template>

<script setup>
import WorkflowEditor from '@/components/WorkflowEditor.vue'

function handleSave(graph) {
  // 保存工作流到后端
  console.log('保存工作流:', graph)
}

function handleTest(input) {
  // 执行工作流测试
  console.log('测试输入:', input)
}
</script>
```

### 3. 执行工作流

```typescript
import { startExecution, getExecutionStatus } from '@/api/workflow-execution-api'

// 启动执行
const result = await startExecution(selection, {
  workflowId: 'workflow-001',
  input: {
    query: '你好'
  },
  debug: true
})

// 轮询执行状态
const status = await getExecutionStatus(
  selection,
  'workflow-001',
  result.task_id
)
```

## 📚 节点类型详解

### LLM节点（大模型节点）

**用途**：调用大语言模型生成内容

**配置项**：
- `model_id`：模型ID（如：gpt-4, qwen-max）
- `temperature`：温度参数（0-2）
- `max_tokens`：最大token数
- `sys_prompt`：系统提示词（支持变量引用）
- `user_prompt`：用户提示词（支持变量引用）
- `output_key`：输出变量名

**示例**：
```json
{
  "type": "llm",
  "data": {
    "label": "生成回复",
    "node_param": {
      "model_id": "qwen-max",
      "model_config": {
        "temperature": 0.7,
        "max_tokens": 2000
      },
      "sys_prompt": "你是一个友好的助手",
      "user_prompt": "${Start.query}",
      "output_key": "response"
    }
  }
}
```

### API节点

**用途**：调用外部API接口

**配置项**：
- `method`：HTTP方法（GET/POST/PUT/DELETE）
- `url`：API地址（支持变量引用）
- `headers`：请求头
- `body`：请求体（支持变量引用）
- `timeout`：超时时间
- `output_key`：输出变量名

**示例**：
```json
{
  "type": "api",
  "data": {
    "label": "调用天气API",
    "node_param": {
      "method": "GET",
      "url": "https://api.weather.com/v1/current?city=${Start.city}",
      "headers": {
        "Authorization": "Bearer xxx"
      },
      "output_key": "weather_data"
    }
  }
}
```

### Script节点

**用途**：执行自定义脚本代码

**配置项**：
- `language`：脚本语言（javascript/python）
- `code`：脚本代码
- `output_key`：输出变量名

**示例**：
```json
{
  "type": "script",
  "data": {
    "label": "数据处理",
    "node_param": {
      "language": "javascript",
      "code": "const data = ${API_1.response};\nreturn { processed: data.map(x => x * 2) };",
      "output_key": "processed_data"
    }
  }
}
```

### Condition节点

**用途**：条件分支判断

**配置项**：
- `conditions`：条件列表
  - `expression`：条件表达式
  - `label`：分支标签
- `default_branch`：默认分支

**示例**：
```json
{
  "type": "condition",
  "data": {
    "label": "判断类型",
    "node_param": {
      "conditions": [
        {
          "expression": "${Start.type} === 'text'",
          "label": "文本类型"
        },
        {
          "expression": "${Start.type} === 'image'",
          "label": "图片类型"
        }
      ],
      "default_branch": "default"
    }
  }
}
```

## 🔗 变量引用系统

### 基本语法

使用 `${nodeId.outputKey}` 格式引用上游节点的输出变量。

### 示例

假设有以下节点：
- Start节点（ID: `start_1`）输出：`query`
- LLM节点（ID: `llm_1`）输出：`response`

在后续节点中引用：
```json
{
  "user_prompt": "用户问题：${start_1.query}\n\nAI回答：${llm_1.response}"
}
```

### 变量验证

系统会自动验证变量引用的有效性：
- 检查引用的节点是否存在
- 检查引用的输出键是否存在
- 检查是否存在循环依赖

## 🎨 最佳实践

### 1. 节点命名规范
- 使用清晰、有意义的节点名称
- 例如：`生成摘要`、`调用翻译API`、`数据清洗`

### 2. 变量命名规范
- 使用小写字母和下划线
- 例如：`user_query`、`api_response`、`processed_data`

### 3. 工作流设计原则
- 保持工作流简洁，避免过度复杂
- 合理使用条件分支和并行节点
- 添加适当的错误处理节点

### 4. 性能优化
- 避免不必要的API调用
- 使用并行节点提高执行效率
- 合理设置缓存策略

## 🔧 高级功能

### 1. 自动保存

编辑器支持自动保存功能，默认5秒防抖保存：

```typescript
// 在WorkflowEditor组件中已实现
watch(() => store.isDirty, (dirty) => {
  if (dirty) {
    setTimeout(() => handleSave(), 5000)
  }
})
```

### 2. 快捷键

- `Ctrl+S`：保存工作流
- `Ctrl+Z`：撤销
- `Ctrl+Y`：重做
- `Delete`：删除选中节点

### 3. 历史记录

支持撤销/重做功能，最多保存50步历史记录：

```typescript
// 撤销
store.undo()

// 重做
store.redo()
```

### 4. 调试模式

启用调试模式可以逐步执行工作流：

```typescript
const debugSession = await createDebugSession(selection, workflowId, input)

// 单步执行
const nodeResult = await stepDebug(selection, workflowId, debugSession.sessionId)

// 继续执行
const result = await continueDebug(selection, workflowId, debugSession.sessionId)
```

## 📝 API参考

### 工作流API

```typescript
// 创建工作流
createWorkflow(selection, workflowCode, name, description, graphDefinition)

// 更新工作流
updateWorkflow(selection, workflowId, name, description, graphDefinition)

// 发布工作流
publishWorkflow(selection, workflowId)

// 获取版本列表
listWorkflowVersions(selection, workflowId)

// 回滚版本
rollbackWorkflowVersion(selection, workflowId, versionId)
```

### 执行API

```typescript
// 启动执行
startExecution(selection, { workflowId, input, debug })

// 获取执行状态
getExecutionStatus(selection, workflowId, taskId)

// 停止执行
stopExecution(selection, workflowId, taskId)

// 获取执行历史
getExecutionHistory(selection, workflowId, limit)
```

### 调试API

```typescript
// 创建调试会话
createDebugSession(selection, workflowId, input)

// 单步执行
stepDebug(selection, workflowId, sessionId)

// 设置断点
setBreakpoints(selection, workflowId, sessionId, nodeIds)
```

## 🐛 常见问题

### Q: 变量引用无效？
A: 检查以下几点：
1. 节点ID是否正确
2. 输出键是否存在
3. 是否存在循环依赖

### Q: 工作流执行失败？
A: 查看执行结果中的错误信息：
```typescript
const result = await getExecutionStatus(selection, workflowId, taskId)
console.log(result.error)
```

### Q: 如何调试单个节点？
A: 使用单节点测试功能：
```typescript
const result = await testSingleNode(
  selection,
  workflowId,
  nodeId,
  testInput
)
```

## 📦 文件结构

```
src/main/web/src/
├── types/
│   └── workflow-node.ts          # 工作流类型定义
├── constants/
│   └── node-schemas.ts           # 节点Schema定义
├── utils/
│   └── variable-system.ts        # 变量引用系统
├── stores/
│   └── workflow-store.ts         # 工作流状态管理
├── api/
│   └── workflow-execution-api.ts # 工作流API
└── components/
    ├── WorkflowEditor.vue        # 工作流编辑器
    ├── WorkflowTestPanel.vue     # 测试面板
    ├── workflow-nodes/           # 节点组件
    │   ├── LLMNode.vue
    │   ├── APINode.vue
    │   └── ...
    └── workflow-panels/          # 配置面板
        ├── LLMPanel.vue
        ├── APIPanel.vue
        └── ...
```

## 🎯 下一步计划

1. ✅ 完善所有节点类型的组件和配置面板
2. ✅ 实现工作流模板功能
3. ✅ 添加工作流导入导出功能
4. ✅ 优化自动布局算法
5. ✅ 添加工作流分享功能

## 📄 许可证

本项目采用 MIT 许可证。
