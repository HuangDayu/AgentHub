<template>
  <div class="dag-editor">
    <!-- 节点工具栏 -->
    <div class="node-toolbar">
      <div class="toolbar-title">节点类型</div>
      <div class="node-types">
        <div
          v-for="nodeType in nodeTypes"
          :key="nodeType.type"
          class="node-type-item"
          draggable="true"
          @dragstart="onDragStart($event, nodeType)"
        >
          <div class="node-icon" :style="{ background: nodeType.color }">
            {{ nodeType.icon }}
          </div>
          <span>{{ nodeType.label }}</span>
        </div>
      </div>
    </div>

    <!-- Vue Flow画布 -->
    <div
      class="flow-container"
      @drop="onDrop"
      @dragover="onDragOver"
    >
      <VueFlow
        v-model:nodes="nodes"
        v-model:edges="edges"
        @nodes-change="onNodesChange"
        @edges-change="onEdgesChange"
        @connect="onConnect"
        @node-click="onNodeClick"
        @edge-click="onEdgeClick"
        @pane-click="onPaneClick"
        :default-viewport="{ zoom: 1, x: 0, y: 0 }"
        :min-zoom="0.2"
        :max-zoom="4"
        :snap-to-grid="true"
        :snap-grid="[15, 15]"
        fit-view-on-init
        class="vue-flow-canvas"
        :connection-mode="ConnectionMode.Loose"
        :deleteKeyCode="'Delete'"
        :multi-selectionKeyCode="'Shift'"
      >
        <!-- 背景 -->
        <Background pattern-color="#aaa" :gap="20" />

        <!-- 小地图 -->
        <MiniMap v-if="showMiniMap" />

        <!-- 控制按钮 -->
        <Controls />

        <!-- 自定义节点 -->
        <template #node-start="nodeProps">
          <StartNode :data="nodeProps.data" :selected="nodeProps.selected" />
        </template>
        <template #node-end="nodeProps">
          <EndNode :data="nodeProps.data" :selected="nodeProps.selected" />
        </template>
        <template #node-llm="nodeProps">
          <LLMNode :data="nodeProps.data" :selected="nodeProps.selected" />
        </template>
        <template #node-api="nodeProps">
          <ApiNode :data="nodeProps.data" :selected="nodeProps.selected" />
        </template>
        <template #node-condition="nodeProps">
          <ConditionNode :data="nodeProps.data" :selected="nodeProps.selected" />
        </template>
        <template #node-loop="nodeProps">
          <LoopNode :data="nodeProps.data" :selected="nodeProps.selected" />
        </template>
        <template #node-parallel="nodeProps">
          <ParallelNode :data="nodeProps.data" :selected="nodeProps.selected" />
        </template>
        <template #node-variable="nodeProps">
          <VariableNode :data="nodeProps.data" :selected="nodeProps.selected" />
        </template>
        <template #node-code="nodeProps">
          <CodeNode :data="nodeProps.data" :selected="nodeProps.selected" />
        </template>
        <template #node-tool="nodeProps">
          <ToolNode :data="nodeProps.data" :selected="nodeProps.selected" />
        </template>
      </VueFlow>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { VueFlow, useVueFlow, ConnectionMode } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import type { Node, Edge, Connection, NodeChange, EdgeChange } from '@vue-flow/core'
import { useWorkflowStore } from '@/stores/workflow-store'

// 导入自定义节点组件
import StartNode from './nodes/StartNode.vue'
import EndNode from './nodes/EndNode.vue'
import LLMNode from './nodes/LLMNode.vue'
import ApiNode from './nodes/ApiNode.vue'
import ConditionNode from './nodes/ConditionNode.vue'
import LoopNode from './nodes/LoopNode.vue'
import ParallelNode from './nodes/ParallelNode.vue'
import VariableNode from './nodes/VariableNode.vue'
import CodeNode from './nodes/CodeNode.vue'
import ToolNode from './nodes/ToolNode.vue'

const props = defineProps<{
  modelValue: string
  showMiniMap?: boolean
  readOnly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'node-select': [node: Node]
  'edge-select': [edge: Edge]
}>()

const workflowStore = useWorkflowStore()
const { 
  onConnect: handleConnect, 
  addNodes, 
  addEdges,
  removeNodes,
  removeEdges,
  fitView,
  project
} = useVueFlow()

// 节点类型定义
const nodeTypes = [
  { type: 'start', label: '开始', icon: '▶', color: '#28a745' },
  { type: 'end', label: '结束', icon: '⏹', color: '#dc3545' },
  { type: 'llm', label: 'LLM', icon: '🤖', color: '#007bff' },
  { type: 'api', label: 'API', icon: '🌐', color: '#17a2b8' },
  { type: 'condition', label: '条件', icon: '❓', color: '#ffc107' },
  { type: 'loop', label: '循环', icon: '🔄', color: '#6610f2' },
  { type: 'parallel', label: '并行', icon: '⚡', color: '#fd7e14' },
  { type: 'variable', label: '变量', icon: '📝', color: '#20c997' },
  { type: 'code', label: '代码', icon: '💻', color: '#6f42c1' },
  { type: 'tool', label: '工具', icon: '🔧', color: '#e83e8c' }
]

// 节点和边数据
const nodes = ref<Node[]>([])
const edges = ref<Edge[]>([])

// 解析初始JSON
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    try {
      const graph = JSON.parse(newVal)
      nodes.value = graph.nodes || []
      edges.value = graph.edges || []
    } catch (e) {
      console.error('Failed to parse graph definition', e)
    }
  }
}, { immediate: true })

// 监听变化并更新JSON
watch([nodes, edges], () => {
  const graph = {
    nodes: nodes.value,
    edges: edges.value
  }
  emit('update:modelValue', JSON.stringify(graph, null, 2))
}, { deep: true })

// 拖拽开始
function onDragStart(event: DragEvent, nodeType: any) {
  if (event.dataTransfer) {
    event.dataTransfer.setData('application/vueflow', nodeType.type)
    event.dataTransfer.effectAllowed = 'move'
  }
}

// 拖拽经过
function onDragOver(event: DragEvent) {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

// 放置节点
function onDrop(event: DragEvent) {
  if (props.readOnly) return
  
  const type = event.dataTransfer?.getData('application/vueflow')
  if (!type) return

  // 获取画布坐标
  const bounds = (event.target as HTMLElement).getBoundingClientRect()
  const position = project({
    x: event.clientX - bounds.left,
    y: event.clientY - bounds.top
  })

  // 创建新节点
  const newNode: Node = {
    id: `${type}-${Date.now()}`,
    type,
    position,
    data: {
      label: nodeTypes.find(n => n.type === type)?.label || type,
      config: {}
    }
  }

  addNodes([newNode])
  
  // 保存到历史记录
  workflowStore.saveToHistory()
}

// 节点变化处理
function onNodesChange(changes: NodeChange[]) {
  if (props.readOnly) return
  
  // 处理节点删除
  const removedNodes = changes.filter(c => c.type === 'remove')
  if (removedNodes.length > 0) {
    workflowStore.saveToHistory()
  }
}

// 边变化处理
function onEdgesChange(changes: EdgeChange[]) {
  if (props.readOnly) return
  
  // 处理边删除
  const removedEdges = changes.filter(c => c.type === 'remove')
  if (removedEdges.length > 0) {
    workflowStore.saveToHistory()
  }
}

// 连接处理
handleConnect((params: Connection) => {
  if (props.readOnly) return
  
  // 验证连接
  if (!isValidConnection(params)) {
    return
  }

  // 添加新边
  const newEdge: Edge = {
    id: `e${params.source}-${params.target}`,
    source: params.source,
    target: params.target,
    sourceHandle: params.sourceHandle,
    targetHandle: params.targetHandle
  }
  
  addEdges([newEdge])
  workflowStore.saveToHistory()
})

// 验证连接是否有效
function isValidConnection(connection: Connection): boolean {
  // 不能连接到自己
  if (connection.source === connection.target) {
    return false
  }

  // 检查是否已存在相同的连接
  const existingEdge = edges.value.find(
    e => e.source === connection.source && e.target === connection.target
  )
  if (existingEdge) {
    return false
  }

  // 检查是否会形成循环（可选，根据业务需求）
  // 这里简单起见，允许形成循环
  
  return true
}

// 节点点击
function onNodeClick(event: any) {
  const node = event.node
  workflowStore.selectNode(node)
  emit('node-select', node)
}

// 边点击
function onEdgeClick(event: any) {
  const edge = event.edge
  workflowStore.selectEdge(edge)
  emit('edge-select', edge)
}

// 画布点击
function onPaneClick() {
  workflowStore.selectNode(null)
  workflowStore.selectEdge(null)
}

// 自动布局
function autoLayout() {
  // 简单的自动布局算法
  const nodeWidth = 150
  const nodeHeight = 80
  const horizontalGap = 50
  const verticalGap = 50
  
  // 按拓扑排序
  const sortedNodes = topologicalSort(nodes.value, edges.value)
  
  // 计算每层节点
  const layers: Node[][] = []
  const nodeLayers = new Map<string, number>()
  
  sortedNodes.forEach(node => {
    const inEdges = edges.value.filter(e => e.target === node.id)
    if (inEdges.length === 0) {
      nodeLayers.set(node.id, 0)
      if (!layers[0]) layers[0] = []
      layers[0].push(node)
    } else {
      const maxLayer = Math.max(
        ...inEdges.map(e => nodeLayers.get(e.source) || 0)
      )
      const layer = maxLayer + 1
      nodeLayers.set(node.id, layer)
      if (!layers[layer]) layers[layer] = []
      layers[layer].push(node)
    }
  })

  // 设置节点位置
  layers.forEach((layerNodes, layerIndex) => {
    layerNodes.forEach((node, nodeIndex) => {
      node.position = {
        x: layerIndex * (nodeWidth + horizontalGap) + 50,
        y: nodeIndex * (nodeHeight + verticalGap) + 50
      }
    })
  })

  // 适应视图
  fitView()
  workflowStore.saveToHistory()
}

// 拓扑排序
function topologicalSort(nodes: Node[], edges: Edge[]): Node[] {
  const result: Node[] = []
  const visited = new Set<string>()
  const visiting = new Set<string>()

  function visit(node: Node) {
    if (visited.has(node.id)) return
    if (visiting.has(node.id)) return // 检测到循环

    visiting.add(node.id)
    
    const outEdges = edges.filter(e => e.source === node.id)
    outEdges.forEach(edge => {
      const targetNode = nodes.find(n => n.id === edge.target)
      if (targetNode) visit(targetNode)
    })

    visiting.delete(node.id)
    visited.add(node.id)
    result.unshift(node)
  }

  nodes.forEach(node => visit(node))
  return result
}

// 删除选中的节点
function deleteSelectedNodes() {
  const selectedNodes = nodes.value.filter(n => n.selected)
  if (selectedNodes.length > 0) {
    removeNodes(selectedNodes.map(n => n.id))
    workflowStore.saveToHistory()
  }
}

// 删除选中的边
function deleteSelectedEdges() {
  const selectedEdges = edges.value.filter(e => e.selected)
  if (selectedEdges.length > 0) {
    removeEdges(selectedEdges.map(e => e.id))
    workflowStore.saveToHistory()
  }
}

// 暴露方法给父组件
defineExpose({
  autoLayout,
  deleteSelectedNodes,
  deleteSelectedEdges,
  fitView
})
</script>

<style scoped>
.dag-editor {
  display: flex;
  height: 100%;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.node-toolbar {
  width: 150px;
  background: #f8f9fa;
  border-right: 1px solid #ddd;
  padding: 1rem;
  overflow-y: auto;
}

.toolbar-title {
  font-weight: bold;
  margin-bottom: 1rem;
  color: #333;
}

.node-types {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.node-type-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: grab;
  transition: all 0.2s;
}

.node-type-item:hover {
  border-color: #007bff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.node-type-item:active {
  cursor: grabbing;
}

.node-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  color: white;
  font-size: 12px;
}

.flow-container {
  flex: 1;
  position: relative;
}

.vue-flow-canvas {
  width: 100%;
  height: 100%;
}
</style>
