<template>
  <div class="workflow-editor">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <h3 class="workflow-title">{{ workflowName || '未命名工作流' }}</h3>
        <span v-if="isDirty" class="dirty-indicator">●</span>
      </div>
      <div class="toolbar-center">
        <button 
          class="btn btn-icon" 
          @click="undo" 
          :disabled="!canUndo"
          title="撤销 (Ctrl+Z)"
        >
          ↶
        </button>
        <button 
          class="btn btn-icon" 
          @click="redo" 
          :disabled="!canRedo"
          title="重做 (Ctrl+Y)"
        >
          ↷
        </button>
        <div class="divider"></div>
        <button 
          class="btn btn-icon" 
          @click="toggleMiniMap"
          :class="{ active: showMiniMap }"
          title="小地图"
        >
          ▤
        </button>
        <button 
          class="btn btn-icon" 
          @click="autoLayout"
          title="自动布局"
        >
          ⤢
        </button>
        <div class="divider"></div>
        <button 
          class="btn btn-secondary" 
          @click="toggleJsonView"
        >
          {{ showJsonView ? '可视化' : 'JSON' }}
        </button>
      </div>
      <div class="toolbar-right">
        <button 
          class="btn btn-secondary" 
          @click="handleTest"
          :disabled="!canTest"
        >
          测试
        </button>
        <button 
          class="btn btn-primary" 
          @click="handleSave"
          :disabled="!isDirty || isSaving"
        >
          {{ isSaving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>

    <!-- 主编辑区域 -->
    <div class="editor-container">
      <!-- 左侧节点面板 -->
      <div class="node-panel">
        <div class="panel-header">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="搜索节点..." 
            class="search-input"
          />
        </div>
        <div class="node-categories">
          <div 
            v-for="(schemas, category) in filteredNodeSchemas" 
            :key="category"
            class="node-category"
          >
            <div class="category-title">{{ category }}</div>
            <div class="node-list">
              <div
                v-for="schema in schemas"
                :key="schema.type"
                class="node-item"
                draggable="true"
                @dragstart="onDragStart($event, schema)"
                @click="addNodeBySchema(schema)"
              >
                <span class="node-icon">{{ schema.icon }}</span>
                <div class="node-info">
                  <div class="node-name">{{ schema.title }}</div>
                  <div class="node-desc">{{ schema.desc }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间画布区域 -->
      <div class="canvas-area">
        <!-- JSON视图 -->
        <div v-if="showJsonView" class="json-view">
          <textarea 
            v-model="jsonContent" 
            class="json-editor"
            @blur="applyJsonChanges"
          ></textarea>
        </div>
        
        <!-- 可视化视图 -->
        <div v-else class="visual-view" ref="canvasRef">
          <!-- Vue Flow画布 -->
          <VueFlow
            v-model:nodes="flowNodes"
            v-model:edges="flowEdges"
            :default-viewport="{ zoom: 1, x: 0, y: 0 }"
            :min-zoom="0.2"
            :max-zoom="2"
            fit-view-on-init
            :snap-to-grid="true"
            :snap-grid="[20, 20]"
            @node-click="onNodeClick"
            @edge-click="onEdgeClick"
            @connect="onConnect"
            @nodes-change="onNodesChange"
            @edges-change="onEdgesChange"
          >
            <!-- 背景网格 -->
            <Background :gap="20" :size="1" />
            
            <!-- 小地图 -->
            <MiniMap v-if="showMiniMap" />
            
            <!-- 控制面板 -->
            <Controls />
            
            <!-- 自定义节点 -->
            <template #node-start="nodeProps">
              <StartNode v-bind="nodeProps" />
            </template>
            <template #node-end="nodeProps">
              <EndNode v-bind="nodeProps" />
            </template>
            <template #node-llm="nodeProps">
              <LLMNode v-bind="nodeProps" />
            </template>
            <template #node-api="nodeProps">
              <APINode v-bind="nodeProps" />
            </template>
            <template #node-script="nodeProps">
              <ScriptNode v-bind="nodeProps" />
            </template>
            <template #node-retrieval="nodeProps">
              <RetrievalNode v-bind="nodeProps" />
            </template>
            <template #node-condition="nodeProps">
              <ConditionNode v-bind="nodeProps" />
            </template>
            <template #node-task="nodeProps">
              <TaskNode v-bind="nodeProps" />
            </template>
          </VueFlow>
        </div>
      </div>

      <!-- 右侧配置面板 -->
      <div v-if="selectedNode" class="config-panel">
        <div class="panel-header">
          <h4>{{ selectedNode.data.label }} 配置</h4>
          <button class="btn-close" @click="closeConfigPanel">×</button>
        </div>
        <div class="panel-body">
          <!-- 动态加载节点配置面板 -->
          <component 
            :is="getConfigPanelComponent(selectedNode.type)" 
            :node="selectedNode"
            :available-variables="availableVariables"
            @update="updateNodeConfig"
          />
        </div>
      </div>
    </div>

    <!-- 测试面板 -->
    <div v-if="showTestPanel" class="test-panel">
      <div class="panel-header">
        <h4>工作流测试</h4>
        <button class="btn-close" @click="closeTestPanel">×</button>
      </div>
      <div class="panel-body">
        <WorkflowTestPanel 
          :workflow-id="workflowId"
          @close="closeTestPanel"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import { useWorkflowStore } from '@/stores/workflow-store'
import { 
  getNodeSchemasByCategory, 
  getNodeSchema 
} from '@/constants/node-schemas'
import type { NodeSchema, WorkflowNode } from '@/types/workflow-node'

// 组件导入
import StartNode from './workflow-nodes/StartNode.vue'
import EndNode from './workflow-nodes/EndNode.vue'
import LLMNode from './workflow-nodes/LLMNode.vue'
import APINode from './workflow-nodes/APINode.vue'
import ScriptNode from './workflow-nodes/ScriptNode.vue'
import RetrievalNode from './workflow-nodes/RetrievalNode.vue'
import ConditionNode from './workflow-nodes/ConditionNode.vue'
import TaskNode from './workflow-nodes/TaskNode.vue'
import WorkflowTestPanel from './WorkflowTestPanel.vue'

// Props
interface Props {
  workflowId?: string
  workflowName?: string
  initialGraph?: any
  readOnly?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  workflowId: '',
  workflowName: '',
  readOnly: false
})

// Emits
const emit = defineEmits<{
  save: [graph: any]
  test: [input: any]
  nodeSelect: [node: WorkflowNode]
}>()

// Store
const store = useWorkflowStore()
const { addNodes, addEdges, onConnect: handleConnect } = useVueFlow()

// 状态
const showJsonView = ref(false)
const showTestPanel = ref(false)
const searchQuery = ref('')
const canvasRef = ref<HTMLElement>()
const jsonContent = ref('')

// 计算属性
const nodeSchemasByCategory = computed(() => getNodeSchemasByCategory())

const filteredNodeSchemas = computed(() => {
  if (!searchQuery.value) {
    return nodeSchemasByCategory.value
  }
  
  const result: Record<string, NodeSchema[]> = {}
  const query = searchQuery.value.toLowerCase()
  
  for (const [category, schemas] of Object.entries(nodeSchemasByCategory.value)) {
    const filtered = schemas.filter(
      s => s.title.toLowerCase().includes(query) || 
           s.desc.toLowerCase().includes(query)
    )
    if (filtered.length > 0) {
      result[category] = filtered
    }
  }
  
  return result
})

const {
  workflowName,
  isDirty,
  isSaving,
  canUndo,
  canRedo,
  showMiniMap,
  selectedNode,
  availableVariables,
  nodes: flowNodes,
  edges: flowEdges
} = store

const canTest = computed(() => {
  return flowNodes.value.length > 0 && !isDirty.value
})

// 方法
function toggleJsonView() {
  showJsonView.value = !showJsonView.value
  if (showJsonView.value) {
    jsonContent.value = JSON.stringify(store.graph, null, 2)
  }
}

function toggleMiniMap() {
  store.showMiniMap = !store.showMiniMap
}

function applyJsonChanges() {
  try {
    const graph = JSON.parse(jsonContent.value)
    store.setGraph(graph)
  } catch (error) {
    console.error('JSON解析错误:', error)
    // 恢复到有效状态
    jsonContent.value = JSON.stringify(store.graph, null, 2)
  }
}

function onDragStart(event: DragEvent, schema: NodeSchema) {
  event.dataTransfer!.setData('application/node-schema', JSON.stringify(schema))
  event.dataTransfer!.effectAllowed = 'move'
}

function addNodeBySchema(schema: NodeSchema) {
  const position = { x: 200, y: 200 } // 默认位置，可以优化为画布中心
  
  const newNode: WorkflowNode = {
    id: `${schema.type}_${Date.now()}`,
    type: schema.type,
    position,
    data: {
      label: schema.title,
      desc: schema.desc,
      input_params: [...schema.defaultParams.input_params],
      output_params: [...schema.defaultParams.output_params],
      node_param: { ...schema.defaultParams.node_param }
    }
  }
  
  store.addNode(newNode)
}

function onNodeClick(event: any) {
  const node = event.node
  store.selectNode(node)
  emit('nodeSelect', node)
}

function onEdgeClick(event: any) {
  store.selectEdge(event.edge)
}

function onConnect(connection: any) {
  const newEdge = {
    id: `edge_${Date.now()}`,
    source: connection.source,
    target: connection.target,
    sourceHandle: connection.sourceHandle,
    targetHandle: connection.targetHandle
  }
  store.addEdge(newEdge)
}

function onNodesChange(changes: any) {
  // 处理节点位置变化等
  for (const change of changes) {
    if (change.type === 'position' && change.position) {
      store.updateNode(change.id, { position: change.position })
    }
  }
}

function onEdgesChange(changes: any) {
  // 处理边的变化
}

function closeConfigPanel() {
  store.selectNode(null)
}

function updateNodeConfig(updates: any) {
  if (selectedNode.value) {
    store.updateNode(selectedNode.value.id, {
      data: {
        ...selectedNode.value.data,
        ...updates
      }
    })
  }
}

function getConfigPanelComponent(type: string) {
  // 根据节点类型返回对应的配置面板组件
  const panelMap: Record<string, any> = {
    llm: () => import('./workflow-panels/LLMPanel.vue'),
    api: () => import('./workflow-panels/APIPanel.vue'),
    script: () => import('./workflow-panels/ScriptPanel.vue'),
    retrieval: () => import('./workflow-panels/RetrievalPanel.vue'),
    condition: () => import('./workflow-panels/ConditionPanel.vue'),
    task: () => import('./workflow-panels/TaskPanel.vue')
  }
  
  return panelMap[type] || null
}

function handleTest() {
  showTestPanel.value = true
}

function closeTestPanel() {
  showTestPanel.value = false
}

async function handleSave() {
  if (!isDirty.value || isSaving.value) return
  
  store.isSaving = true
  try {
    await emit('save', store.graph)
    store.markAsSaved()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    store.isSaving = false
  }
}

function undo() {
  store.undo()
}

function redo() {
  store.redo()
}

function autoLayout() {
  // TODO: 实现自动布局算法
  console.log('自动布局')
}

// 键盘快捷键
function handleKeyboard(event: KeyboardEvent) {
  if (props.readOnly) return
  
  // Ctrl+S 保存
  if (event.ctrlKey && event.key === 's') {
    event.preventDefault()
    handleSave()
  }
  
  // Ctrl+Z 撤销
  if (event.ctrlKey && event.key === 'z') {
    event.preventDefault()
    undo()
  }
  
  // Ctrl+Y 重做
  if (event.ctrlKey && event.key === 'y') {
    event.preventDefault()
    redo()
  }
  
  // Delete 删除选中节点
  if (event.key === 'Delete' && selectedNode.value) {
    event.preventDefault()
    store.deleteNode(selectedNode.value.id)
  }
}

// 生命周期
onMounted(() => {
  // 初始化工作流数据
  if (props.workflowId) {
    store.setWorkflowInfo(props.workflowId, props.workflowName || '', '')
  }
  
  if (props.initialGraph) {
    store.setGraph(props.initialGraph)
  }
  
  // 设置只读模式
  store.readOnly = props.readOnly
  
  // 监听键盘事件
  window.addEventListener('keydown', handleKeyboard)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyboard)
  store.reset()
})

// 自动保存（防抖）
let saveTimer: number | null = null
watch(() => store.isDirty, (dirty) => {
  if (dirty && !props.readOnly) {
    if (saveTimer) clearTimeout(saveTimer)
    saveTimer = window.setTimeout(() => {
      handleSave()
    }, 5000) // 5秒后自动保存
  }
})
</script>

<style scoped>
.workflow-editor {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #e1e5eb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.workflow-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.dirty-indicator {
  color: #f56c6c;
  font-size: 12px;
}

.toolbar-center,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn {
  padding: 6px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn:hover:not(:disabled) {
  border-color: #409eff;
  color: #409eff;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #409eff;
  border-color: #409eff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #66b1ff;
  border-color: #66b1ff;
}

.btn-secondary {
  background: #f4f4f5;
  border-color: #e9e9eb;
}

.btn-icon {
  padding: 6px 10px;
  font-size: 16px;
}

.btn-icon.active {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.divider {
  width: 1px;
  height: 20px;
  background: #dcdfe6;
  margin: 0 4px;
}

.editor-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.node-panel {
  width: 260px;
  background: white;
  border-right: 1px solid #e1e5eb;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e1e5eb;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.node-categories {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.node-category {
  margin-bottom: 20px;
}

.category-title {
  font-size: 13px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.node-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.node-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  background: #f8f9fa;
  border: 1px solid #e1e5eb;
  border-radius: 6px;
  cursor: grab;
  transition: all 0.2s;
}

.node-item:hover {
  background: #ecf5ff;
  border-color: #409eff;
  transform: translateX(4px);
}

.node-item:active {
  cursor: grabbing;
}

.node-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.node-info {
  flex: 1;
  min-width: 0;
}

.node-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.node-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.canvas-area {
  flex: 1;
  position: relative;
  background: #ffffff;
}

.json-view {
  width: 100%;
  height: 100%;
}

.json-editor {
  width: 100%;
  height: 100%;
  border: none;
  padding: 20px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  line-height: 1.6;
  resize: none;
}

.visual-view {
  width: 100%;
  height: 100%;
}

.config-panel {
  width: 320px;
  background: white;
  border-left: 1px solid #e1e5eb;
  display: flex;
  flex-direction: column;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.btn-close {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  font-size: 18px;
  color: #909399;
  cursor: pointer;
  border-radius: 4px;
}

.btn-close:hover {
  background: #f4f4f5;
  color: #606266;
}

.test-panel {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 400px;
  max-height: 600px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  display: flex;
  flex-direction: column;
}
</style>
