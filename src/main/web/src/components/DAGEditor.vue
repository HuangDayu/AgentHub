<template>
  <div class="dag-editor">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">DAG 编辑器</span>
      </div>
      <div class="toolbar-right">
        <button class="btn btn-secondary" @click="toggleJsonView">
          {{ showJsonView ? '可视化编辑' : 'JSON编辑' }}
        </button>
        <button class="btn btn-primary" @click="exportGraph">导出</button>
      </div>
    </div>

    <!-- 主编辑区域 -->
    <div class="editor-container">
      <!-- 节点面板 -->
      <div class="node-panel">
        <div class="panel-title">节点类型</div>
        <div class="node-templates">
          <div
            v-for="template in NODE_TEMPLATES"
            :key="template.type"
            class="node-template"
            draggable="true"
            @dragstart="onDragStart($event, template)"
          >
            <span class="node-icon">{{ template.icon }}</span>
            <span class="node-name">{{ template.name }}</span>
          </div>
        </div>
      </div>

      <!-- 画布区域 -->
      <div
        v-if="!showJsonView"
        class="canvas-container"
        @drop="onDrop"
        @dragover.prevent
        @click="onCanvasClick"
      >
        <svg class="edges-layer" :width="canvasWidth" :height="canvasHeight">
          <!-- 渲染边 -->
          <g v-for="edge in graph.edges" :key="edge.id">
            <path
              :d="getEdgePath(edge)"
              class="edge"
              :class="{ 'edge-selected': selectedEdge?.id === edge.id }"
              @click.stop="selectEdge(edge)"
            />
            <text
              v-if="edge.label"
              :x="getEdgeLabelPosition(edge).x"
              :y="getEdgeLabelPosition(edge).y"
              class="edge-label"
            >
              {{ edge.label }}
            </text>
          </g>
        </svg>

        <!-- 渲染节点 -->
        <div
          v-for="node in graph.nodes"
          :key="node.id"
          class="node"
          :class="[
            `node-${node.type}`,
            { 'node-selected': selectedNode?.id === node.id }
          ]"
          :style="{ left: node.position.x + 'px', top: node.position.y + 'px' }"
          @mousedown="onNodeMouseDown($event, node)"
          @click.stop="selectNode(node)"
        >
          <div class="node-header">
            <span class="node-icon">{{ getNodeIcon(node.type) }}</span>
            <span class="node-title">{{ node.name }}</span>
          </div>
          <div v-if="node.description" class="node-description">
            {{ node.description }}
          </div>
          <!-- 连接点 -->
          <div class="connection-point input" @click.stop="startConnection(node, 'input')"></div>
          <div class="connection-point output" @click.stop="startConnection(node, 'output')"></div>
        </div>
      </div>

      <!-- JSON编辑区域 -->
      <div v-else class="json-editor">
        <textarea
          v-model="jsonText"
          class="json-textarea"
          placeholder="请输入DAG图定义JSON"
          @blur="parseJson"
        ></textarea>
        <div v-if="jsonError" class="json-error">{{ jsonError }}</div>
      </div>

      <!-- 属性面板 -->
      <div class="property-panel">
        <div class="panel-title">属性</div>
        <div v-if="selectedNode" class="property-content">
          <div class="property-group">
            <label>节点ID</label>
            <input v-model="selectedNode.id" type="text" class="input" disabled />
          </div>
          <div class="property-group">
            <label>节点名称</label>
            <input v-model="selectedNode.name" type="text" class="input" @input="updateGraph" />
          </div>
          <div class="property-group">
            <label>节点类型</label>
            <select v-model="selectedNode.type" class="select" @change="updateGraph">
              <option v-for="template in NODE_TEMPLATES" :key="template.type" :value="template.type">
                {{ template.name }}
              </option>
            </select>
          </div>
          <div class="property-group">
            <label>描述</label>
            <textarea
              v-model="selectedNode.description"
              class="textarea"
              @input="updateGraph"
            ></textarea>
          </div>
          <div class="property-group">
            <label>配置 (JSON)</label>
            <textarea
              v-model="nodeConfigJson"
              class="textarea"
              @blur="updateNodeConfig"
            ></textarea>
          </div>
          <button class="btn btn-danger btn-sm" @click="deleteNode(selectedNode)">删除节点</button>
        </div>
        <div v-else-if="selectedEdge" class="property-content">
          <div class="property-group">
            <label>边ID</label>
            <input v-model="selectedEdge.id" type="text" class="input" disabled />
          </div>
          <div class="property-group">
            <label>源节点</label>
            <input :value="getNodeName(selectedEdge.source)" type="text" class="input" disabled />
          </div>
          <div class="property-group">
            <label>目标节点</label>
            <input :value="getNodeName(selectedEdge.target)" type="text" class="input" disabled />
          </div>
          <div class="property-group">
            <label>条件</label>
            <input v-model="selectedEdge.condition" type="text" class="input" @input="updateGraph" />
          </div>
          <div class="property-group">
            <label>标签</label>
            <input v-model="selectedEdge.label" type="text" class="input" @input="updateGraph" />
          </div>
          <button class="btn btn-danger btn-sm" @click="deleteEdge(selectedEdge)">删除边</button>
        </div>
        <div v-else class="property-empty">
          请选择节点或边进行编辑
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { DAGGraph, DAGNode, DAGEdge, NodeTemplate, NODE_TEMPLATES, NodeType } from '@/types/dag'

// Props
const props = defineProps<{
  modelValue?: string // JSON字符串
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

// 状态
const graph = ref<DAGGraph>({ nodes: [], edges: [] })
const selectedNode = ref<DAGNode | null>(null)
const selectedEdge = ref<DAGEdge | null>(null)
const showJsonView = ref(false)
const jsonText = ref('')
const jsonError = ref('')
const canvasWidth = ref(2000)
const canvasHeight = ref(2000)

// 拖拽状态
const isDragging = ref(false)
const dragNode = ref<DAGNode | null>(null)
const dragOffset = ref({ x: 0, y: 0 })

// 连接状态
const isConnecting = ref(false)
const connectionStart = ref<{ node: DAGNode; type: 'input' | 'output' } | null>(null)

// 计算属性
const nodeConfigJson = computed({
  get: () => {
    if (!selectedNode.value?.config) return '{}'
    return JSON.stringify(selectedNode.value.config, null, 2)
  },
  set: (value: string) => {
    if (selectedNode.value) {
      try {
        selectedNode.value.config = JSON.parse(value)
      } catch (e) {
        // 解析错误，不更新
      }
    }
  }
})

// 初始化
onMounted(() => {
  if (props.modelValue) {
    try {
      graph.value = JSON.parse(props.modelValue)
      jsonText.value = props.modelValue
    } catch (e) {
      console.error('Failed to parse graph definition:', e)
    }
  }
})

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    try {
      graph.value = JSON.parse(newValue)
      jsonText.value = newValue
    } catch (e) {
      console.error('Failed to parse graph definition:', e)
    }
  }
})

// 方法
function getNodeIcon(type: NodeType): string {
  const template = NODE_TEMPLATES.find(t => t.type === type)
  return template?.icon || '●'
}

function getNodeName(nodeId: string): string {
  const node = graph.value.nodes.find(n => n.id === nodeId)
  return node?.name || nodeId
}

function generateId(): string {
  return `node-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
}

function generateEdgeId(): string {
  return `edge-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
}

// 拖拽节点模板
function onDragStart(event: DragEvent, template: NodeTemplate) {
  event.dataTransfer?.setData('nodeTemplate', JSON.stringify(template))
}

// 放置节点
function onDrop(event: DragEvent) {
  const templateData = event.dataTransfer?.getData('nodeTemplate')
  if (!templateData) return

  const template: NodeTemplate = JSON.parse(templateData)
  const rect = (event.target as HTMLElement).getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top

  const newNode: DAGNode = {
    id: generateId(),
    type: template.type,
    name: template.name,
    description: template.description,
    config: template.defaultConfig ? { ...template.defaultConfig } : undefined,
    position: { x, y }
  }

  graph.value.nodes.push(newNode)
  selectedNode.value = newNode
  selectedEdge.value = null
  updateGraph()
}

// 选择节点
function selectNode(node: DAGNode) {
  selectedNode.value = node
  selectedEdge.value = null
}

// 选择边
function selectEdge(edge: DAGEdge) {
  selectedEdge.value = edge
  selectedNode.value = null
}

// 点击画布
function onCanvasClick() {
  selectedNode.value = null
  selectedEdge.value = null
}

// 节点拖拽
function onNodeMouseDown(event: MouseEvent, node: DAGNode) {
  isDragging.value = true
  dragNode.value = node
  dragOffset.value = {
    x: event.clientX - node.position.x,
    y: event.clientY - node.position.y
  }

  document.addEventListener('mousemove', onNodeMouseMove)
  document.addEventListener('mouseup', onNodeMouseUp)
}

function onNodeMouseMove(event: MouseEvent) {
  if (!isDragging.value || !dragNode.value) return

  dragNode.value.position.x = event.clientX - dragOffset.value.x
  dragNode.value.position.y = event.clientY - dragOffset.value.y
}

function onNodeMouseUp() {
  isDragging.value = false
  dragNode.value = null
  document.removeEventListener('mousemove', onNodeMouseMove)
  document.removeEventListener('mouseup', onNodeMouseUp)
  updateGraph()
}

// 连接节点
function startConnection(node: DAGNode, type: 'input' | 'output') {
  if (!isConnecting.value) {
    isConnecting.value = true
    connectionStart.value = { node, type }
  } else if (connectionStart.value) {
    // 完成连接
    const startNode = connectionStart.value.node
    const startType = connectionStart.value.type

    // 确保连接方向正确（output -> input）
    let source: string, target: string
    if (startType === 'output' && type === 'input') {
      source = startNode.id
      target = node.id
    } else if (startType === 'input' && type === 'output') {
      source = node.id
      target = startNode.id
    } else {
      // 无效连接
      isConnecting.value = false
      connectionStart.value = null
      return
    }

    // 检查是否已存在相同的边
    const exists = graph.value.edges.some(e => e.source === source && e.target === target)
    if (!exists && source !== target) {
      const newEdge: DAGEdge = {
        id: generateEdgeId(),
        source,
        target
      }
      graph.value.edges.push(newEdge)
      updateGraph()
    }

    isConnecting.value = false
    connectionStart.value = null
  }
}

// 获取边的路径
function getEdgePath(edge: DAGEdge): string {
  const sourceNode = graph.value.nodes.find(n => n.id === edge.source)
  const targetNode = graph.value.nodes.find(n => n.id === edge.target)

  if (!sourceNode || !targetNode) return ''

  const x1 = sourceNode.position.x + 120 // 节点宽度的一半
  const y1 = sourceNode.position.y + 40 // 节点高度的一半
  const x2 = targetNode.position.x + 120
  const y2 = targetNode.position.y + 40

  // 使用贝塞尔曲线
  const cx = (x1 + x2) / 2
  return `M ${x1} ${y1} C ${cx} ${y1}, ${cx} ${y2}, ${x2} ${y2}`
}

// 获取边标签位置
function getEdgeLabelPosition(edge: DAGEdge): { x: number; y: number } {
  const sourceNode = graph.value.nodes.find(n => n.id === edge.source)
  const targetNode = graph.value.nodes.find(n => n.id === edge.target)

  if (!sourceNode || !targetNode) return { x: 0, y: 0 }

  return {
    x: (sourceNode.position.x + targetNode.position.x) / 2 + 120,
    y: (sourceNode.position.y + targetNode.position.y) / 2 + 40
  }
}

// 删除节点
function deleteNode(node: DAGNode) {
  const index = graph.value.nodes.findIndex(n => n.id === node.id)
  if (index !== -1) {
    graph.value.nodes.splice(index, 1)
    // 删除相关的边
    graph.value.edges = graph.value.edges.filter(e => e.source !== node.id && e.target !== node.id)
    selectedNode.value = null
    updateGraph()
  }
}

// 删除边
function deleteEdge(edge: DAGEdge) {
  const index = graph.value.edges.findIndex(e => e.id === edge.id)
  if (index !== -1) {
    graph.value.edges.splice(index, 1)
    selectedEdge.value = null
    updateGraph()
  }
}

// 更新节点配置
function updateNodeConfig() {
  try {
    if (selectedNode.value) {
      selectedNode.value.config = JSON.parse(nodeConfigJson.value)
      updateGraph()
    }
  } catch (e) {
    // 解析错误
  }
}

// 更新图
function updateGraph() {
  const json = JSON.stringify(graph.value, null, 2)
  jsonText.value = json
  emit('update:modelValue', json)
}

// 切换JSON视图
function toggleJsonView() {
  showJsonView.value = !showJsonView.value
  if (showJsonView.value) {
    jsonText.value = JSON.stringify(graph.value, null, 2)
  }
}

// 解析JSON
function parseJson() {
  try {
    graph.value = JSON.parse(jsonText.value)
    jsonError.value = ''
    emit('update:modelValue', jsonText.value)
  } catch (e: any) {
    jsonError.value = `JSON解析错误: ${e.message}`
  }
}

// 导出图
function exportGraph() {
  const json = JSON.stringify(graph.value, null, 2)
  const blob = new Blob([json], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'dag-graph.json'
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.dag-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-stripe);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: var(--bg-card-solid);
  border-bottom: 1px solid var(--color-border);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.toolbar-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-heading);
}

.toolbar-right {
  display: flex;
  gap: 10px;
}

.editor-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.node-panel {
  width: 200px;
  background: var(--bg-card-solid);
  border-right: 1px solid var(--color-border);
  padding: 16px;
  overflow-y: auto;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.node-templates {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.node-template {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  background: var(--bg-stripe);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  cursor: grab;
  transition: all 0.2s;
}

.node-template:hover {
  background: var(--bg-stripe);
  border-color: var(--color-primary);
}

.node-template:active {
  cursor: grabbing;
}

.node-icon {
  font-size: 18px;
}

.node-name {
  font-size: 13px;
  color: var(--color-text);
}

.canvas-container {
  flex: 1;
  position: relative;
  overflow: auto;
  background: var(--bg-card-solid);
  background-image:
    linear-gradient(rgba(0, 0, 0, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
}

.edges-layer {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
}

.edge {
  fill: none;
  stroke: var(--color-text-muted);
  stroke-width: 2;
  pointer-events: stroke;
  cursor: pointer;
}

.edge:hover {
  stroke: var(--color-primary);
  stroke-width: 3;
}

.edge-selected {
  stroke: var(--color-primary);
  stroke-width: 3;
}

.edge-label {
  font-size: 12px;
  fill: var(--color-text);
  text-anchor: middle;
}

.node {
  position: absolute;
  width: 240px;
  min-height: 80px;
  background: var(--bg-card-solid);
  border: 2px solid var(--color-border);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: move;
  transition: box-shadow 0.2s;
}

.node:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.node-selected {
  border-color: var(--color-primary);
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
}

.node-start {
  border-color: var(--color-success);
}

.node-end {
  border-color: var(--color-error);
}

.node-task {
  border-color: var(--color-primary);
}

.node-condition {
  border-color: var(--color-warning);
}

.node-parallel {
  border-color: var(--color-purple);
}

.node-loop {
  border-color: var(--color-success);
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: var(--bg-stripe);
  border-bottom: 1px solid var(--color-border);
  border-radius: 6px 6px 0 0;
}

.node-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-heading);
}

.node-description {
  padding: 8px 12px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.connection-point {
  position: absolute;
  width: 12px;
  height: 12px;
  background: var(--color-primary);
  border: 2px solid white;
  border-radius: 50%;
  cursor: crosshair;
  transition: transform 0.2s;
}

.connection-point:hover {
  transform: scale(1.3);
}

.connection-point.input {
  left: -6px;
  top: 50%;
  transform: translateY(-50%);
}

.connection-point.output {
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
}

.connection-point.input:hover,
.connection-point.output:hover {
  transform: translateY(-50%) scale(1.3);
}

.property-panel {
  width: 280px;
  background: var(--bg-card-solid);
  border-left: 1px solid var(--color-border);
  padding: 16px;
  overflow-y: auto;
}

.property-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.property-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.property-group label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
}

.property-empty {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 13px;
  padding: 20px;
}

.json-editor {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: var(--bg-card-solid);
}

.json-textarea {
  flex: 1;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  resize: none;
}

.json-error {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 4px;
  color: #c00;
  font-size: 12px;
}

/* 按钮样式 */
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: var(--color-primary); color: var(--color-text-inverse);
}

.btn-primary:hover {
  background: var(--color-primary-dark);
}

.btn-secondary {
  background: var(--bg-stripe);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.btn-secondary:hover {
  background: var(--bg-stripe);
}

.btn-danger {
  background: var(--color-error); color: var(--color-text-inverse);
}

.btn-danger:hover {
  background: var(--color-error-dark);
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

/* 输入框样式 */
.input,
.select,
.textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 13px;
  transition: border-color 0.2s;
}

.input:focus,
.select:focus,
.textarea:focus {
  outline: none;
  border-color: var(--color-primary);
}

.textarea {
  min-height: 80px;
  resize: vertical;
}
</style>
