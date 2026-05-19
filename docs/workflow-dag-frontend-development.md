# 工作流DAG前端开发文档

## 一、概述

### 1.1 目标

实现一个基于Vue Flow的完整工作流DAG可视化编辑器，支持AI Agent工作流的编排、执行、监控和管理。本文档基于参考项目（spring-ai-alibaba）的实现经验，结合AgentHub项目的现有架构，提供详细的开发指南。

### 1.2 技术栈

**核心技术：**
- **Vue 3.5.0** - Composition API + TypeScript
- **TypeScript 5.8.0** - 完整类型支持
- **Pinia 2.3.1** - 状态管理
- **Vue Flow 1.48.2** - DAG可视化编辑核心库
- **Vite 6.0.0** - 构建工具

**Vue Flow套件：**
- `@vue-flow/core` - 核心功能
- `@vue-flow/background` - 背景网格
- `@vue-flow/controls` - 控制组件（缩放、平移）
- `@vue-flow/minimap` - 小地图导航

### 1.3 架构设计

采用分层架构设计：

```
┌─────────────────────────────────────────┐
│           应用层 (Application)            │
│  - 页面视图 (Views)                       │
│  - 节点实现 (Node Implementations)        │
│  - 配置面板 (Config Panels)               │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           组件层 (Components)             │
│  - DAG编辑器 (DagEditor)                  │
│  - 基础节点 (BaseNode)                    │
│  - 执行控制 (ExecutionControl)            │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           状态层 (State)                  │
│  - WorkflowStore (Pinia)                 │
│  - 节点状态管理                           │
│  - 执行状态管理                           │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           服务层 (Services)               │
│  - WorkflowAPI                           │
│  - WorkflowExecutionAPI                  │
│  - SSE事件处理                            │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           类型层 (Types)                  │
│  - Workflow Types                        │
│  - Node Types                            │
│  - DAG Types                             │
└─────────────────────────────────────────┘
```

## 二、核心功能模块

### 2.1 DAG编辑器

#### 2.1.1 主编辑器组件

**文件路径：** `src/components/workflow/DagEditor.vue`

**功能需求：**
1. 集成Vue Flow画布
2. 支持节点拖拽添加
3. 支持节点连线
4. 支持节点选中、移动、删除
5. 支持撤销/重做
6. 支持自动布局
7. 支持小地图导航
8. 支持缩放和平移

**实现要点：**

```vue
<template>
  <div class="dag-editor">
    <VueFlow
      v-model:nodes="nodes"
      v-model:edges="edges"
      :node-types="nodeTypes"
      :default-viewport="{ zoom: 1, x: 0, y: 0 }"
      :min-zoom="0.2"
      :max-zoom="4"
      :snap-to-grid="true"
      :snap-grid="[15, 15]"
      @nodes-change="onNodesChange"
      @edges-change="onEdgesChange"
      @connect="onConnect"
      @node-click="onNodeClick"
      @node-drag-stop="onNodeDragStop"
    >
      <!-- 背景网格 -->
      <Background :variant="BackgroundVariant.Dots" :gap="20" />
      
      <!-- 控制组件 -->
      <Controls />
      
      <!-- 小地图 -->
      <MiniMap />
      
      <!-- 节点选择面板 -->
      <NodeSelectorPanel v-if="showNodeSelector" />
    </VueFlow>
    
    <!-- 工具栏 -->
    <EditorToolbar
      @undo="undo"
      @redo="redo"
      @auto-layout="autoLayout"
      @zoom-fit="zoomToFit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { VueFlow, useVueFlow, Background, BackgroundVariant, Controls, MiniMap } from '@vue-flow/core'
import { useWorkflowStore } from '@/stores/workflow-store'
import { nodeTypes } from './node-types'

const store = useWorkflowStore()
const { onNodesChange, onEdgesChange, onConnect, fitView } = useVueFlow()

// 节点和边数据
const nodes = computed(() => store.nodes)
const edges = computed(() => store.edges)

// 事件处理
const onNodeClick = (event: any) => {
  store.selectNode(event.node.id)
}

const onNodeDragStop = (event: any) => {
  store.updateNodePosition(event.node.id, event.node.position)
}

const onConnect = (connection: any) => {
  store.addEdge(connection.source, connection.target)
}

// 撤销/重做
const undo = () => store.undo()
const redo = () => store.redo()

// 自动布局
const autoLayout = async () => {
  const layoutedNodes = await layoutNodes(nodes.value, edges.value)
  store.setNodes(layoutedNodes)
}

// 适应视图
const zoomToFit = () => fitView({ padding: 0.2 })
</script>
```

#### 2.1.2 节点拖拽添加

**实现方式：**

1. **节点选择面板** - 左侧节点库
2. **拖拽处理** - 使用HTML5 Drag & Drop API
3. **放置处理** - 在画布上计算放置位置

```vue
<!-- NodeSelectorPanel.vue -->
<template>
  <div class="node-selector-panel">
    <div class="panel-header">
      <h3>节点库</h3>
    </div>
    <div class="node-list">
      <div
        v-for="schema in nodeSchemas"
        :key="schema.type"
        class="node-item"
        draggable="true"
        @dragstart="onDragStart($event, schema)"
      >
        <div class="node-icon" :style="{ backgroundColor: schema.bgColor }">
          <component :is="schema.icon" />
        </div>
        <div class="node-info">
          <div class="node-title">{{ schema.title }}</div>
          <div class="node-desc">{{ schema.desc }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nodeSchemas } from '@/constants/node-schemas'

const onDragStart = (event: DragEvent, schema: NodeSchema) => {
  event.dataTransfer!.setData('application/node-type', schema.type)
  event.dataTransfer!.effectAllowed = 'move'
}
</script>
```

```typescript
// DagEditor.vue 中的拖放处理
const onDragOver = (event: DragEvent) => {
  event.preventDefault()
  event.dataTransfer!.dropEffect = 'move'
}

const onDrop = (event: DragEvent) => {
  const type = event.dataTransfer!.getData('application/node-type')
  const { left, top } = containerRef.value!.getBoundingClientRect()
  
  // 转换为画布坐标
  const position = project({
    x: event.clientX - left,
    y: event.clientY - top
  })
  
  // 添加节点
  store.addNode(type, position)
}
```

### 2.2 节点组件系统

#### 2.2.1 基础节点组件

**文件路径：** `src/components/workflow/nodes/BaseNode.vue`

**功能需求：**
1. 统一的节点外观
2. 输入/输出端口（Handle）
3. 节点状态显示（成功、失败、运行中）
4. 节点选中效果
5. 支持失败分支

**实现要点：**

```vue
<template>
  <div
    :class="['base-node', nodeClass]"
    :style="{ backgroundColor: schema.bgColor }"
  >
    <!-- 节点头部 -->
    <div class="node-header">
      <div class="node-icon">
        <component :is="schema.icon" />
      </div>
      <div class="node-title">{{ data.label }}</div>
      <div v-if="nodeStatus" class="node-status">
        <StatusIcon :status="nodeStatus" />
      </div>
    </div>
    
    <!-- 节点内容 -->
    <div class="node-content">
      <slot></slot>
    </div>
    
    <!-- 输入端口 -->
    <Handle
      v-for="input in inputs"
      :key="`input-${input.id}`"
      type="target"
      :position="Position.Left"
      :id="input.id"
      class="handle-input"
    />
    
    <!-- 输出端口 -->
    <Handle
      v-for="output in outputs"
      :key="`output-${output.id}`"
      type="source"
      :position="Position.Right"
      :id="output.id"
      class="handle-output"
    />
    
    <!-- 失败分支端口 -->
    <Handle
      v-if="hasFailBranch"
      type="source"
      :position="Position.Bottom"
      id="fail"
      class="handle-fail"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import { useWorkflowStore } from '@/stores/workflow-store'
import { getNodeSchema } from '@/constants/node-schemas'

interface Props {
  id: string
  data: any
  selected?: boolean
  hasFailBranch?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  selected: false,
  hasFailBranch: false
})

const store = useWorkflowStore()
const schema = computed(() => getNodeSchema(props.data.type))
const nodeStatus = computed(() => store.getNodeStatus(props.id))

const nodeClass = computed(() => ({
  'node-selected': props.selected,
  'node-running': nodeStatus.value === 'running',
  'node-success': nodeStatus.value === 'success',
  'node-failed': nodeStatus.value === 'failed'
}))
</script>

<style scoped>
.base-node {
  border-radius: 8px;
  border: 2px solid transparent;
  padding: 12px;
  min-width: 200px;
  transition: all 0.2s;
}

.node-selected {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.node-running {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
</style>
```

#### 2.2.2 具体节点实现

每个节点类型需要实现三个部分：

1. **节点组件** - 视觉呈现
2. **配置面板** - 参数配置
3. **节点Schema** - 元数据定义

**示例：LLM节点**

```vue
<!-- LLMNode.vue -->
<template>
  <BaseNode
    :id="id"
    :data="data"
    :selected="selected"
    :has-fail-branch="true"
  >
    <div class="llm-node-content">
      <div class="model-name">{{ data.nodeParam.modelName }}</div>
      <div class="prompt-preview">{{ promptPreview }}</div>
    </div>
  </BaseNode>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseNode from './BaseNode.vue'

interface Props {
  id: string
  data: any
  selected?: boolean
}

const props = defineProps<Props>()

const promptPreview = computed(() => {
  const prompt = props.data.nodeParam.prompt
  return prompt.length > 50 ? prompt.substring(0, 50) + '...' : prompt
})
</script>
```

```vue
<!-- LLMPanel.vue -->
<template>
  <div class="llm-panel">
    <div class="panel-section">
      <h4>模型配置</h4>
      <ModelSelector v-model="nodeParam.modelName" />
      <ParameterSlider
        label="Temperature"
        v-model="nodeParam.temperature"
        :min="0"
        :max="2"
        :step="0.1"
      />
    </div>
    
    <div class="panel-section">
      <h4>提示词</h4>
      <PromptEditor
        v-model="nodeParam.prompt"
        :variables="availableVariables"
      />
    </div>
    
    <div class="panel-section">
      <h4>重试配置</h4>
      <RetryConfig v-model="nodeParam.retryConfig" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useWorkflowStore } from '@/stores/workflow-store'
import ModelSelector from '@/components/common/ModelSelector.vue'
import PromptEditor from '@/components/workflow/PromptEditor.vue'

const props = defineProps<{ nodeId: string }>()
const store = useWorkflowStore()

const nodeParam = computed(() => store.getNodeParam(props.nodeId))
const availableVariables = computed(() => store.getAvailableVariables(props.nodeId))
</script>
```

```typescript
// node-schemas.ts 中的LLM Schema
export const LLMSchema: NodeSchema = {
  type: 'llm',
  icon: 'RobotOutlined',
  title: 'LLM',
  desc: '调用大语言模型',
  bgColor: '#E8F4FF',
  defaultParams: {
    modelName: 'gpt-3.5-turbo',
    temperature: 0.7,
    maxTokens: 2000,
    prompt: '',
    retryConfig: {
      maxRetries: 3,
      retryDelay: 1000
    }
  },
  inputs: [
    { id: 'default', label: '输入', type: 'any' }
  ],
  outputs: [
    { id: 'text', label: '文本', type: 'string' },
    { id: 'json', label: 'JSON', type: 'object' }
  ]
}
```

#### 2.2.3 特殊节点类型

**条件分支节点（ConditionNode）：**

```vue
<template>
  <BaseNode :id="id" :data="data" :selected="selected">
    <div class="condition-node-content">
      <div v-for="branch in branches" :key="branch.id" class="branch-item">
        <div class="branch-label">{{ branch.label }}</div>
        <Handle
          type="source"
          :position="Position.Right"
          :id="branch.id"
          :style="{ top: `${branch.position}px` }"
        />
      </div>
    </div>
  </BaseNode>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import BaseNode from './BaseNode.vue'

const branches = computed(() => {
  const conditions = props.data.nodeParam.conditions
  return conditions.map((cond, index) => ({
    id: `branch-${index}`,
    label: cond.label,
    position: 60 + index * 30
  }))
})
</script>
```

**循环节点（LoopNode）：**

```vue
<template>
  <div class="loop-node">
    <BaseNode :id="id" :data="data" :selected="selected">
      <div class="loop-info">
        <div>迭代变量: {{ data.nodeParam.iteratorVar }}</div>
        <div>循环体节点数: {{ loopBodyNodeCount }}</div>
      </div>
    </BaseNode>
    
    <!-- 循环体区域 -->
    <div class="loop-body" v-if="isExpanded">
      <slot name="loop-body"></slot>
    </div>
  </div>
</template>
```

### 2.3 配置面板系统

#### 2.3.1 面板容器

**文件路径：** `src/components/workflow/ConfigPanelContainer.vue`

```vue
<template>
  <div class="config-panel-container">
    <div class="panel-header">
      <h3>{{ selectedNodeSchema?.title }} 配置</h3>
      <button @click="closePanel">×</button>
    </div>
    
    <div class="panel-content">
      <component
        :is="panelComponent"
        :node-id="selectedNodeId"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useWorkflowStore } from '@/stores/workflow-store'

// 动态加载面板组件
const panelComponents = {
  llm: () => import('./panels/LLMPanel.vue'),
  api: () => import('./panels/ApiPanel.vue'),
  condition: () => import('./panels/ConditionPanel.vue'),
  // ... 其他节点面板
}

const store = useWorkflowStore()
const selectedNodeId = computed(() => store.selectedNodeId)
const selectedNodeSchema = computed(() => store.selectedNodeSchema)

const panelComponent = computed(() => {
  const type = selectedNodeSchema.value?.type
  return panelComponents[type] || null
})

const closePanel = () => store.selectNode(null)
</script>
```

#### 2.3.2 通用配置组件

**变量选择器：**

```vue
<!-- VariableSelector.vue -->
<template>
  <div class="variable-selector">
    <a-tree-select
      v-model:value="selectedValue"
      :tree-data="variableTree"
      :field-names="{ label: 'name', value: 'path', children: 'children' }"
      placeholder="选择变量"
      @change="onChange"
    >
      <template #suffixIcon>
        <VariableIcon />
      </template>
    </a-tree-select>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useWorkflowStore } from '@/stores/workflow-store'

const props = defineProps<{
  nodeId: string
  type?: string // 变量类型过滤
}>()

const store = useWorkflowStore()
const variableTree = computed(() => store.getVariableTree(props.nodeId))
</script>
```

**提示词编辑器：**

```vue
<!-- PromptEditor.vue -->
<template>
  <div class="prompt-editor">
    <div class="editor-toolbar">
      <button @click="insertVariable">插入变量</button>
      <button @click="preview">预览</button>
    </div>
    
    <div class="editor-content">
      <textarea
        v-model="prompt"
        @input="onInput"
        @select="onSelect"
      ></textarea>
      
      <!-- 变量高亮 -->
      <VariableHighlighter :text="prompt" :variables="variables" />
    </div>
    
    <!-- 变量选择弹窗 -->
    <VariableSelectorModal
      v-if="showVariableSelector"
      @select="onVariableSelect"
      @close="showVariableSelector = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useVariableSystem } from '@/utils/variable-system'

const prompt = defineModel<string>()
const { highlightVariables, parseVariables } = useVariableSystem()

const insertVariable = () => {
  showVariableSelector.value = true
}

const onVariableSelect = (variable: Variable) => {
  const varRef = `\${${variable.path}}`
  // 在光标位置插入变量引用
  insertAtCursor(varRef)
}
</script>
```

### 2.4 状态管理

#### 2.4.1 WorkflowStore设计

**文件路径：** `src/stores/workflow-store.ts`

**核心状态：**

```typescript
interface WorkflowState {
  // 工作流元数据
  workflowId: string | null
  workflowName: string
  workflowDescription: string
  
  // 节点和边
  nodes: WorkflowNode[]
  edges: WorkflowEdge[]
  
  // 选择状态
  selectedNodeId: string | null
  selectedEdgeId: string | null
  
  // 历史记录（撤销/重做）
  history: HistoryStep[]
  historyIndex: number
  
  // 执行状态
  executionStatus: ExecutionStatus
  nodeStatusMap: Map<string, NodeStatus>
  
  // 变量树
  variableTree: VariableTreeNode[]
  
  // UI状态
  isDirty: boolean
  isLoading: boolean
}
```

**核心Actions：**

```typescript
export const useWorkflowStore = defineStore('workflow', () => {
  // 状态
  const state = reactive<WorkflowState>({
    // ... 初始状态
  })
  
  // 节点操作
  const addNode = (type: string, position: Position) => {
    const schema = getNodeSchema(type)
    const node: WorkflowNode = {
      id: generateId(),
      type,
      position,
      data: {
        label: schema.title,
        nodeParam: cloneDeep(schema.defaultParams),
        inputParams: [],
        outputParams: []
      }
    }
    
    state.nodes.push(node)
    addHistoryStep()
    updateVariableTree()
  }
  
  const updateNode = (nodeId: string, updates: Partial<WorkflowNode>) => {
    const index = state.nodes.findIndex(n => n.id === nodeId)
    if (index !== -1) {
      state.nodes[index] = { ...state.nodes[index], ...updates }
      addHistoryStep()
      updateVariableTree()
    }
  }
  
  const deleteNode = (nodeId: string) => {
    // 删除相关边
    state.edges = state.edges.filter(
      e => e.source !== nodeId && e.target !== nodeId
    )
    // 删除节点
    state.nodes = state.nodes.filter(n => n.id !== nodeId)
    addHistoryStep()
    updateVariableTree()
  }
  
  // 边操作
  const addEdge = (source: string, target: string, sourceHandle?: string, targetHandle?: string) => {
    // 验证连接有效性
    if (!validateConnection(source, target)) {
      return false
    }
    
    const edge: WorkflowEdge = {
      id: generateId(),
      source,
      target,
      sourceHandle,
      targetHandle,
      type: 'default'
    }
    
    state.edges.push(edge)
    addHistoryStep()
    return true
  }
  
  // 历史记录
  const addHistoryStep = () => {
    const step: HistoryStep = {
      nodes: cloneDeep(state.nodes),
      edges: cloneDeep(state.edges),
      timestamp: Date.now()
    }
    
    // 删除当前位置之后的历史
    state.history = state.history.slice(0, state.historyIndex + 1)
    state.history.push(step)
    state.historyIndex++
    
    // 限制历史记录数量
    if (state.history.length > 50) {
      state.history.shift()
      state.historyIndex--
    }
    
    state.isDirty = true
  }
  
  const undo = () => {
    if (state.historyIndex > 0) {
      state.historyIndex--
      const step = state.history[state.historyIndex]
      state.nodes = cloneDeep(step.nodes)
      state.edges = cloneDeep(step.edges)
    }
  }
  
  const redo = () => {
    if (state.historyIndex < state.history.length - 1) {
      state.historyIndex++
      const step = state.history[state.historyIndex]
      state.nodes = cloneDeep(step.nodes)
      state.edges = cloneDeep(step.edges)
    }
  }
  
  // 变量树更新
  const updateVariableTree = () => {
    state.variableTree = buildVariableTree(state.nodes, state.edges)
  }
  
  // 执行状态更新
  const updateNodeStatus = (nodeId: string, status: NodeStatus) => {
    state.nodeStatusMap.set(nodeId, status)
  }
  
  return {
    state,
    addNode,
    updateNode,
    deleteNode,
    addEdge,
    undo,
    redo,
    updateNodeStatus,
    // ... 更多actions
  }
})
```

#### 2.4.2 变量系统

**文件路径：** `src/utils/variable-system.ts`

```typescript
// 变量引用格式: ${nodeId.outputKey}

export interface VariableRef {
  nodeId: string
  outputKey: string
  path: string
}

// 解析变量引用
export const parseVariableRef = (ref: string): VariableRef | null => {
  const match = ref.match(/\$\{([^}]+)\}/)
  if (!match) return null
  
  const [nodeId, outputKey] = match[1].split('.')
  return { nodeId, outputKey, path: match[1] }
}

// 构建变量树
export const buildVariableTree = (
  nodes: WorkflowNode[],
  edges: WorkflowEdge[]
): VariableTreeNode[] => {
  const tree: VariableTreeNode[] = []
  
  // 按拓扑顺序遍历节点
  const sortedNodes = topologicalSort(nodes, edges)
  
  for (const node of sortedNodes) {
    const schema = getNodeSchema(node.type)
    const nodeVariables: VariableTreeNode = {
      id: node.id,
      name: node.data.label,
      type: 'node',
      children: []
    }
    
    // 添加输出变量
    for (const output of schema.outputs) {
      nodeVariables.children!.push({
        id: `${node.id}.${output.id}`,
        name: output.label,
        type: output.type,
        path: `${node.id}.${output.id}`
      })
    }
    
    tree.push(nodeVariables)
  }
  
  return tree
}

// 验证变量引用
export const validateVariableRef = (
  ref: VariableRef,
  currentNodeId: string,
  nodes: WorkflowNode[],
  edges: WorkflowEdge[]
): boolean => {
  // 检查节点是否存在
  const node = nodes.find(n => n.id === ref.nodeId)
  if (!node) return false
  
  // 检查是否是前置节点
  const predecessors = getPredecessors(currentNodeId, edges)
  if (!predecessors.includes(ref.nodeId)) {
    return false
  }
  
  // 检查输出键是否存在
  const schema = getNodeSchema(node.type)
  return schema.outputs.some(o => o.id === ref.outputKey)
}

// 高亮变量引用
export const highlightVariables = (
  text: string,
  variables: VariableTreeNode[]
): string => {
  return text.replace(/\$\{([^}]+)\}/g, (match, path) => {
    const variable = findVariable(variables, path)
    if (variable) {
      return `<span class="variable-ref" data-path="${path}">${match}</span>`
    }
    return `<span class="variable-ref invalid">${match}</span>`
  })
}
```

### 2.5 执行与调试

#### 2.5.1 执行控制组件

**文件路径：** `src/components/workflow-execution/ExecutionControl.vue`

```vue
<template>
  <div class="execution-control">
    <div class="control-buttons">
      <a-button
        type="primary"
        :loading="isExecuting"
        @click="startExecution"
      >
        {{ isExecuting ? '执行中' : '开始执行' }}
      </a-button>
      
      <a-button
        v-if="isExecuting"
        danger
        @click="stopExecution"
      >
        停止
      </a-button>
      
      <a-button @click="debugExecution">
        调试执行
      </a-button>
    </div>
    
    <div class="execution-info">
      <div class="status">
        状态: {{ executionStatus }}
      </div>
      <div class="progress">
        进度: {{ completedNodes }}/{{ totalNodes }}
      </div>
      <div class="time">
        耗时: {{ executionTime }}ms
      </div>
    </div>
    
    <!-- 调试控制 -->
    <div v-if="isDebugMode" class="debug-controls">
      <a-button @click="stepOver">单步跳过</a-button>
      <a-button @click="stepInto">单步进入</a-button>
      <a-button @click="continueExecution">继续</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useWorkflowStore } from '@/stores/workflow-store'
import { workflowExecutionApi } from '@/api/workflow-execution-api'

const store = useWorkflowStore()
const isExecuting = ref(false)
const isDebugMode = ref(false)

const startExecution = async () => {
  isExecuting.value = true
  
  try {
    // 启动执行
    const result = await workflowExecutionApi.startExecution({
      workflowId: store.workflowId,
      nodes: store.nodes,
      edges: store.edges
    })
    
    // 监听SSE事件
    subscribeToEvents(result.executionId)
  } catch (error) {
    console.error('执行失败:', error)
    isExecuting.value = false
  }
}

const subscribeToEvents = (executionId: string) => {
  const eventSource = workflowExecutionApi.subscribeToEvents(executionId)
  
  eventSource.on('node-start', (event) => {
    store.updateNodeStatus(event.nodeId, 'running')
  })
  
  eventSource.on('node-complete', (event) => {
    store.updateNodeStatus(event.nodeId, 'success')
    store.setNodeOutput(event.nodeId, event.output)
  })
  
  eventSource.on('node-error', (event) => {
    store.updateNodeStatus(event.nodeId, 'failed')
    store.setNodeError(event.nodeId, event.error)
  })
  
  eventSource.on('execution-complete', () => {
    isExecuting.value = false
    eventSource.close()
  })
}

const debugExecution = async () => {
  isDebugMode.value = true
  await workflowExecutionApi.startDebugExecution({
    workflowId: store.workflowId,
    nodes: store.nodes,
    edges: store.edges,
    breakpoints: store.breakpoints
  })
}

const stepOver = async () => {
  await workflowExecutionApi.stepOver(store.executionId)
}

const continueExecution = async () => {
  await workflowExecutionApi.continueExecution(store.executionId)
}
</script>
```

#### 2.5.2 结果展示组件

**文件路径：** `src/components/workflow-execution/ExecutionResultPanel.vue`

```vue
<template>
  <div class="execution-result-panel">
    <div class="panel-header">
      <h3>执行结果</h3>
      <a-button @click="exportResults">导出</a-button>
    </div>
    
    <div class="result-tabs">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="summary" tab="概览">
          <ExecutionSummary :result="executionResult" />
        </a-tab-pane>
        
        <a-tab-pane key="nodes" tab="节点结果">
          <NodeResultsList :nodes="nodeResults" />
        </a-tab-pane>
        
        <a-tab-pane key="variables" tab="变量">
          <VariableInspector :variables="finalVariables" />
        </a-tab-pane>
        
        <a-tab-pane key="logs" tab="日志">
          <ExecutionLogs :logs="executionLogs" />
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useWorkflowStore } from '@/stores/workflow-store'

const store = useWorkflowStore()
const activeTab = ref('summary')

const executionResult = computed(() => store.executionResult)
const nodeResults = computed(() => store.nodeResults)
const finalVariables = computed(() => store.finalVariables)
const executionLogs = computed(() => store.executionLogs)

const exportResults = () => {
  const data = JSON.stringify(executionResult.value, null, 2)
  downloadFile(data, 'workflow-result.json')
}
</script>
```

### 2.6 API封装

#### 2.6.1 工作流API

**文件路径：** `src/api/workflow-api.ts`

```typescript
import { http } from './http'
import type {
  Workflow,
  WorkflowCreateRequest,
  WorkflowUpdateRequest,
  WorkflowListResponse
} from '@/types/workflow'

export const workflowApi = {
  // 列表查询
  list: async (params?: {
    page?: number
    pageSize?: number
    name?: string
  }): Promise<WorkflowListResponse> => {
    return http.get('/api/workflows', { params })
  },
  
  // 获取详情
  get: async (id: string): Promise<Workflow> => {
    return http.get(`/api/workflows/${id}`)
  },
  
  // 创建
  create: async (data: WorkflowCreateRequest): Promise<Workflow> => {
    return http.post('/api/workflows', data)
  },
  
  // 更新
  update: async (id: string, data: WorkflowUpdateRequest): Promise<Workflow> => {
    return http.put(`/api/workflows/${id}`, data)
  },
  
  // 删除
  delete: async (id: string): Promise<void> => {
    return http.delete(`/api/workflows/${id}`)
  },
  
  // 复制
  copy: async (id: string, name: string): Promise<Workflow> => {
    return http.post(`/api/workflows/${id}/copy`, { name })
  },
  
  // 验证
  validate: async (workflow: Workflow): Promise<ValidationResult> => {
    return http.post('/api/workflows/validate', workflow)
  },
  
  // 导出
  export: async (id: string): Promise<Blob> => {
    return http.get(`/api/workflows/${id}/export`, {
      responseType: 'blob'
    })
  },
  
  // 导入
  import: async (file: File): Promise<Workflow> => {
    const formData = new FormData()
    formData.append('file', file)
    return http.post('/api/workflows/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
```

#### 2.6.2 执行API

**文件路径：** `src/api/workflow-execution-api.ts`

```typescript
import { http } from './http'
import type {
  ExecutionRequest,
  ExecutionResult,
  DebugRequest
} from '@/types/workflow'

export const workflowExecutionApi = {
  // 启动执行
  startExecution: async (request: ExecutionRequest): Promise<{
    executionId: string
  }> => {
    return http.post('/api/workflows/execute', request)
  },
  
  // 停止执行
  stopExecution: async (executionId: string): Promise<void> => {
    return http.post(`/api/executions/${executionId}/stop`)
  },
  
  // 获取执行结果
  getExecutionResult: async (executionId: string): Promise<ExecutionResult> => {
    return http.get(`/api/executions/${executionId}/result`)
  },
  
  // 订阅执行事件（SSE）
  subscribeToEvents: (executionId: string): EventSource => {
    const url = `${http.baseURL}/api/executions/${executionId}/events`
    return new EventSource(url)
  },
  
  // 调试执行
  startDebugExecution: async (request: DebugRequest): Promise<{
    executionId: string
  }> => {
    return http.post('/api/workflows/debug', request)
  },
  
  // 单步执行
  stepOver: async (executionId: string): Promise<void> => {
    return http.post(`/api/executions/${executionId}/step-over`)
  },
  
  stepInto: async (executionId: string): Promise<void> => {
    return http.post(`/api/executions/${executionId}/step-into`)
  },
  
  // 继续执行
  continueExecution: async (executionId: string): Promise<void> => {
    return http.post(`/api/executions/${executionId}/continue`)
  },
  
  // 设置断点
  setBreakpoints: async (
    executionId: string,
    nodeIds: string[]
  ): Promise<void> => {
    return http.post(`/api/executions/${executionId}/breakpoints`, {
      nodeIds
    })
  }
}
```

## 三、页面集成

### 3.1 工作流编辑器页面

**文件路径：** `src/views/workflow/WorkflowEditorView.vue`

```vue
<template>
  <div class="workflow-editor-view">
    <!-- 顶部工具栏 -->
    <div class="editor-header">
      <div class="header-left">
        <a-button @click="goBack">返回</a-button>
        <a-input
          v-model:value="workflowName"
          placeholder="工作流名称"
          @blur="saveWorkflow"
        />
      </div>
      
      <div class="header-center">
        <a-button-group>
          <a-button @click="undo" :disabled="!canUndo">撤销</a-button>
          <a-button @click="redo" :disabled="!canRedo">重做</a-button>
        </a-button-group>
        
        <a-button @click="autoLayout">自动布局</a-button>
        <a-button @click="validateWorkflow">验证</a-button>
      </div>
      
      <div class="header-right">
        <a-button @click="saveWorkflow" :loading="isSaving">保存</a-button>
        <a-button type="primary" @click="executeWorkflow">执行</a-button>
      </div>
    </div>
    
    <!-- 主编辑区域 -->
    <div class="editor-main">
      <!-- 左侧节点库 -->
      <div class="editor-sidebar">
        <NodeSelectorPanel />
      </div>
      
      <!-- 中间画布 -->
      <div class="editor-canvas">
        <DagEditor ref="dagEditorRef" />
      </div>
      
      <!-- 右侧配置面板 -->
      <div class="editor-config" v-if="selectedNodeId">
        <ConfigPanelContainer :node-id="selectedNodeId" />
      </div>
    </div>
    
    <!-- 底部执行面板 -->
    <div class="editor-footer" v-if="showExecutionPanel">
      <ExecutionResultPanel />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useWorkflowStore } from '@/stores/workflow-store'
import { workflowApi } from '@/api/workflow-api'
import DagEditor from '@/components/workflow/DagEditor.vue'
import NodeSelectorPanel from '@/components/workflow/NodeSelectorPanel.vue'
import ConfigPanelContainer from '@/components/workflow/ConfigPanelContainer.vue'
import ExecutionResultPanel from '@/components/workflow-execution/ExecutionResultPanel.vue'

const route = useRoute()
const router = useRouter()
const store = useWorkflowStore()

const dagEditorRef = ref()
const isSaving = ref(false)
const showExecutionPanel = ref(false)

const workflowName = computed({
  get: () => store.workflowName,
  set: (value) => store.setWorkflowName(value)
})

const selectedNodeId = computed(() => store.selectedNodeId)
const canUndo = computed(() => store.canUndo)
const canRedo = computed(() => store.canRedo)

// 加载工作流
onMounted(async () => {
  const workflowId = route.params.id as string
  
  if (workflowId && workflowId !== 'new') {
    try {
      const workflow = await workflowApi.get(workflowId)
      store.loadWorkflow(workflow)
    } catch (error) {
      message.error('加载工作流失败')
      router.push('/workflows')
    }
  } else {
    store.createNewWorkflow()
  }
})

// 保存工作流
const saveWorkflow = async () => {
  isSaving.value = true
  
  try {
    const workflow = store.toWorkflow()
    
    if (workflow.id) {
      await workflowApi.update(workflow.id, workflow)
    } else {
      const created = await workflowApi.create(workflow)
      store.setWorkflowId(created.id)
    }
    
    message.success('保存成功')
    store.markAsSaved()
  } catch (error) {
    message.error('保存失败')
  } finally {
    isSaving.value = false
  }
}

// 执行工作流
const executeWorkflow = () => {
  showExecutionPanel.value = true
  store.startExecution()
}

// 自动布局
const autoLayout = () => {
  dagEditorRef.value?.autoLayout()
}

// 验证工作流
const validateWorkflow = async () => {
  const workflow = store.toWorkflow()
  const result = await workflowApi.validate(workflow)
  
  if (result.valid) {
    message.success('验证通过')
  } else {
    message.error('验证失败')
    store.setValidationErrors(result.errors)
  }
}

// 撤销/重做
const undo = () => store.undo()
const redo = () => store.redo()

// 返回
const goBack = () => {
  if (store.isDirty) {
    // 提示保存
  } else {
    router.push('/workflows')
  }
}
</script>

<style scoped>
.workflow-editor-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.editor-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.editor-sidebar {
  width: 280px;
  background: #fafafa;
  border-right: 1px solid #e8e8e8;
}

.editor-canvas {
  flex: 1;
  position: relative;
}

.editor-config {
  width: 360px;
  background: #fff;
  border-left: 1px solid #e8e8e8;
  overflow-y: auto;
}

.editor-footer {
  height: 300px;
  background: #fff;
  border-top: 1px solid #e8e8e8;
}
</style>
```

### 3.2 工作流列表页面

**文件路径：** `src/views/workflow/WorkflowListView.vue`

```vue
<template>
  <div class="workflow-list-view">
    <div class="page-header">
      <h2>工作流管理</h2>
      <a-button type="primary" @click="createWorkflow">
        新建工作流
      </a-button>
    </div>
    
    <div class="search-bar">
      <a-input-search
        v-model:value="searchText"
        placeholder="搜索工作流"
        @search="loadWorkflows"
      />
    </div>
    
    <div class="workflow-list">
      <a-table
        :columns="columns"
        :data-source="workflows"
        :loading="loading"
        :pagination="pagination"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a @click="editWorkflow(record.id)">{{ record.name }}</a>
          </template>
          
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.status }}
            </a-tag>
          </template>
          
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="editWorkflow(record.id)">
                编辑
              </a-button>
              <a-button size="small" @click="copyWorkflow(record)">
                复制
              </a-button>
              <a-button size="small" @click="exportWorkflow(record.id)">
                导出
              </a-button>
              <a-popconfirm
                title="确定删除？"
                @confirm="deleteWorkflow(record.id)"
              >
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { workflowApi } from '@/api/workflow-api'

const router = useRouter()
const workflows = ref([])
const loading = ref(false)
const searchText = ref('')
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { key: 'name', title: '名称', dataIndex: 'name' },
  { key: 'description', title: '描述', dataIndex: 'description' },
  { key: 'status', title: '状态', dataIndex: 'status' },
  { key: 'updatedAt', title: '更新时间', dataIndex: 'updatedAt' },
  { key: 'action', title: '操作' }
]

onMounted(() => {
  loadWorkflows()
})

const loadWorkflows = async () => {
  loading.value = true
  
  try {
    const result = await workflowApi.list({
      page: pagination.value.current,
      pageSize: pagination.value.pageSize,
      name: searchText.value
    })
    
    workflows.value = result.items
    pagination.value.total = result.total
  } catch (error) {
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

const createWorkflow = () => {
  router.push('/workflows/new/edit')
}

const editWorkflow = (id: string) => {
  router.push(`/workflows/${id}/edit`)
}

const copyWorkflow = async (workflow: any) => {
  try {
    const newName = `${workflow.name} (副本)`
    await workflowApi.copy(workflow.id, newName)
    message.success('复制成功')
    loadWorkflows()
  } catch (error) {
    message.error('复制失败')
  }
}

const exportWorkflow = async (id: string) => {
  try {
    const blob = await workflowApi.export(id)
    downloadFile(blob, 'workflow.json')
  } catch (error) {
    message.error('导出失败')
  }
}

const deleteWorkflow = async (id: string) => {
  try {
    await workflowApi.delete(id)
    message.success('删除成功')
    loadWorkflows()
  } catch (error) {
    message.error('删除失败')
  }
}

const onTableChange = (pag: any) => {
  pagination.value.current = pag.current
  loadWorkflows()
}
</script>
```

### 3.3 路由配置

**文件路径：** `src/router/index.ts`

```typescript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // ... 其他路由
  
  {
    path: '/workflows',
    children: [
      {
        path: '',
        name: 'WorkflowList',
        component: () => import('@/views/workflow/WorkflowListView.vue')
      },
      {
        path: 'new/edit',
        name: 'WorkflowCreate',
        component: () => import('@/views/workflow/WorkflowEditorView.vue')
      },
      {
        path: ':id/edit',
        name: 'WorkflowEdit',
        component: () => import('@/views/workflow/WorkflowEditorView.vue')
      },
      {
        path: ':id/execute',
        name: 'WorkflowExecute',
        component: () => import('@/views/workflow/WorkflowExecuteView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

## 四、开发任务清单

### 4.1 第一阶段：核心编辑器

**优先级：P0**

1. **DAG编辑器基础**
   - [x] 集成Vue Flow
   - [ ] 实现节点拖拽添加
   - [ ] 实现节点连线
   - [ ] 实现节点选中
   - [ ] 实现节点移动
   - [ ] 实现节点删除

2. **基础节点组件**
   - [x] BaseNode组件
   - [x] StartNode组件
   - [x] EndNode组件
   - [ ] 完善节点状态显示
   - [ ] 完善节点交互效果

3. **工具栏功能**
   - [ ] 撤销/重做
   - [ ] 自动布局
   - [ ] 缩放控制
   - [ ] 小地图导航

### 4.2 第二阶段：节点系统

**优先级：P0**

1. **核心节点实现**
   - [x] LLMNode组件
   - [x] ApiNode组件
   - [x] ConditionNode组件
   - [x] LoopNode组件
   - [x] ParallelNode组件
   - [ ] ScriptNode组件
   - [ ] RetrievalNode组件
   - [ ] ToolNode组件
   - [ ] VariableNode组件

2. **节点配置面板**
   - [x] LLMPanel组件
   - [ ] ApiPanel组件
   - [ ] ConditionPanel组件
   - [ ] LoopPanel组件
   - [ ] ParallelPanel组件
   - [ ] ScriptPanel组件
   - [ ] RetrievalPanel组件
   - [ ] ToolPanel组件
   - [ ] VariablePanel组件

3. **通用配置组件**
   - [ ] VariableSelector组件
   - [ ] PromptEditor组件
   - [ ] ModelSelector组件
   - [ ] ParameterSlider组件
   - [ ] RetryConfig组件

### 4.3 第三阶段：状态管理

**优先级：P1**

1. **WorkflowStore完善**
   - [x] 节点CRUD操作
   - [x] 边CRUD操作
   - [ ] 撤销/重做功能
   - [ ] 变量树构建
   - [ ] 执行状态管理
   - [ ] 验证状态管理

2. **变量系统**
   - [x] 变量引用解析
   - [x] 变量树构建
   - [ ] 变量验证
   - [ ] 变量高亮
   - [ ] 变量提示

### 4.4 第四阶段：执行与调试

**优先级：P1**

1. **执行控制**
   - [x] ExecutionControl组件
   - [ ] 开始/停止执行
   - [ ] SSE事件处理
   - [ ] 执行状态更新

2. **调试功能**
   - [ ] 断点设置
   - [ ] 单步执行
   - [ ] 变量查看
   - [ ] 调用栈查看

3. **结果展示**
   - [x] ExecutionResultPanel组件
   - [ ] 节点结果展示
   - [ ] 变量查看器
   - [ ] 执行日志
   - [ ] 结果导出

### 4.5 第五阶段：页面集成

**优先级：P0**

1. **编辑器页面**
   - [x] WorkflowEditorView基础结构
   - [ ] 完整的布局
   - [ ] 工具栏集成
   - [ ] 保存功能
   - [ ] 验证功能

2. **列表页面**
   - [x] WorkflowListView基础结构
   - [ ] 搜索功能
   - [ ] 分页功能
   - [ ] 操作功能

3. **路由配置**
   - [ ] 编辑器路由
   - [ ] 执行路由
   - [ ] 菜单集成

### 4.6 第六阶段：高级功能

**优先级：P2**

1. **模板系统**
   - [ ] 模板管理
   - [ ] 从模板创建
   - [ ] 保存为模板

2. **导入导出**
   - [ ] 工作流导出
   - [ ] 工作流导入
   - [ ] 格式转换

3. **版本管理**
   - [ ] 版本历史
   - [ ] 版本对比
   - [ ] 版本回滚

4. **分享功能**
   - [ ] 工作流分享
   - [ ] 权限管理
   - [ ] 协作编辑

## 五、开发规范

### 5.1 命名规范

**文件命名：**
- 组件文件：PascalCase（如 `LLMNode.vue`）
- 工具文件：kebab-case（如 `variable-system.ts`）
- Store文件：kebab-case（如 `workflow-store.ts`）

**组件命名：**
- 节点组件：`XxxNode`（如 `LLMNode`）
- 面板组件：`XxxPanel`（如 `LLMPanel`）
- 页面组件：`XxxView`（如 `WorkflowEditorView`）

### 5.2 代码规范

**TypeScript：**
- 所有组件使用 `<script setup lang="ts">`
- 完整的类型定义，避免使用 `any`
- 使用接口定义Props和Emits

**Vue 3：**
- 优先使用Composition API
- 使用 `computed` 处理派生状态
- 使用 `watch` 处理副作用
- 使用 `provide/inject` 传递跨层依赖

**样式：**
- 使用 `scoped` 样式
- 遵循BEM命名规范
- 使用CSS变量管理主题色

### 5.3 组件设计原则

**单一职责：**
- 每个组件只负责一个功能
- 复杂组件拆分为多个子组件

**可复用性：**
- 通用组件放在 `components/common`
- 业务组件放在 `components/workflow`

**可配置性：**
- 通过Props配置组件行为
- 提供合理的默认值

### 5.4 性能优化

**渲染优化：**
- 使用 `v-show` 替代 `v-if`（频繁切换）
- 使用 `v-memo` 缓存静态内容
- 大列表使用虚拟滚动

**状态优化：**
- 避免深层响应式对象
- 使用 `shallowRef` 处理大对象
- 合理使用 `computed` 缓存

**交互优化：**
- 防抖/节流处理频繁操作
- 异步加载大组件
- 懒加载非关键功能

## 六、测试策略

### 6.1 单元测试

**测试工具：**
- Vitest - 测试框架
- Vue Test Utils - 组件测试工具

**测试范围：**
- 工具函数（variable-system.ts）
- Store actions
- 组件渲染和交互

### 6.2 集成测试

**测试场景：**
- 节点添加、编辑、删除流程
- 工作流保存和加载
- 执行流程

### 6.3 E2E测试

**测试工具：**
- Playwright

**测试场景：**
- 完整的工作流创建流程
- 工作流执行流程
- 用户交互流程

## 七、部署与发布

### 7.1 构建配置

**Vite配置：**
```typescript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-flow': ['@vue-flow/core', '@vue-flow/background', '@vue-flow/controls'],
          'ant-design': ['ant-design-vue']
        }
      }
    }
  }
})
```

### 7.2 环境配置

**开发环境：**
- API地址：`http://localhost:8080`
- 开发工具：Vue DevTools

**生产环境：**
- API地址：从环境变量读取
- 性能监控：开启

## 八、附录

### 8.1 参考资源

**Vue Flow文档：**
- 官方文档：https://vueflow.dev/
- 示例：https://vueflow.dev/examples/

**参考项目：**
- spring-ai-alibaba：`E:\Code\vibe\spring-ai-alibaba\spring-ai-alibaba-admin`
- React Flow实现：`frontend/packages/spark-flow`

### 8.2 常见问题

**Q: Vue Flow和React Flow的区别？**
A: API风格不同，但核心概念一致。Vue Flow使用Composition API，React Flow使用Hooks。

**Q: 如何实现自定义节点？**
A: 创建Vue组件，使用 `Handle` 组件定义端口，在 `nodeTypes` 中注册。

**Q: 如何处理大量节点？**
A: 使用虚拟渲染、节点分组、懒加载等技术优化性能。

### 8.3 更新日志

**v1.0.0 (计划中)**
- 完整的DAG编辑器
- 所有节点类型支持
- 执行和调试功能
- 完整的API集成

---

**文档版本：** 1.0.0  
**最后更新：** 2026-05-19  
**维护者：** AgentHub开发团队
