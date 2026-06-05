<template>
  <div class="workflow-editor">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <button class="btn btn-icon" @click="goBack" title="返回列表">←</button>
        <div class="workflow-info">
          <input
            v-model="workflowName"
            class="workflow-title-input"
            :placeholder="'未命名工作流'"
            :disabled="readOnly"
            @input="markDirty"
          />
          <span v-if="isDirty" class="dirty-indicator">● 未保存</span>
        </div>
      </div>
      <div class="toolbar-center">
        <button class="btn btn-icon" @click="undo" :disabled="!canUndo" title="撤销 (Ctrl+Z)">↶</button>
        <button class="btn btn-icon" @click="redo" :disabled="!canRedo" title="重做 (Ctrl+Y)">↷</button>
        <div class="divider"></div>
        <button class="btn btn-icon" @click="toggleMiniMap" :class="{ active: showMiniMap }" title="小地图">▤</button>
        <button class="btn btn-icon" @click="autoLayout" title="自动布局">⤢</button>
        <div class="divider"></div>
        <button class="btn btn-secondary" @click="toggleJsonView">{{ showJsonView ? '可视化' : 'JSON' }}</button>
        <button class="btn btn-secondary" @click="validateWorkflow">校验</button>
      </div>
      <div class="toolbar-right">
        <div class="test-btn-group" v-if="workflowId && workflowId !== 'new'">
          <button class="btn btn-secondary" @click="showTestPanel = !showTestPanel" :class="{ active: showTestPanel }">
            测试
          </button>
        </div>
        <button v-if="!readOnly" class="btn btn-primary" @click="handleSave" :disabled="isSaving">
          {{ isSaving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>

    <!-- 主编辑区域 -->
    <div class="editor-body">
      <!-- 左侧节点面板 -->
      <div class="node-panel" :class="{ collapsed: nodePanelCollapsed }">
        <div class="panel-toggle" @click="nodePanelCollapsed = !nodePanelCollapsed">
          {{ nodePanelCollapsed ? '▶' : '◀' }}
        </div>
        <template v-if="!nodePanelCollapsed">
          <div class="panel-header">
            <input v-model="searchQuery" type="text" placeholder="搜索节点..." class="search-input" />
          </div>
          <div class="node-categories">
            <div v-for="(schemas, category) in filteredNodeSchemas" :key="category" class="node-category">
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
        </template>
      </div>

      <!-- 中间画布区域 -->
      <div class="canvas-area" ref="canvasRef">
        <!-- JSON视图 -->
        <div v-if="showJsonView" class="json-view">
          <textarea v-model="jsonContent" class="json-editor" @blur="applyJsonChanges" spellcheck="false"></textarea>
        </div>

        <!-- Vue Flow可视化画布 -->
        <div v-else class="visual-view" ref="vueFlowContainer">
          <VueFlow
            v-model:nodes="flowNodes"
            v-model:edges="flowEdges"
            :default-viewport="{ zoom: 0.8, x: 200, y: 100 }"
            :min-zoom="0.2"
            :max-zoom="3"
            :snap-to-grid="true"
            :snap-grid="[20, 20]"
            fit-view-on-init
            @node-click="onNodeClick"
            @pane-click="onPaneClick"
            @connect="onConnect"
            @nodes-change="onNodesChange"
            @edges-change="onEdgesChange"
            @nodes-delete="onNodesDelete"
            @drop="onDrop"
            @dragover="onDragOver"
            class="vue-flow-instance"
          >
            <Background :gap="20" :size="1" pattern-color="var(--color-border)" />
            <MiniMap v-if="showMiniMap" :node-color="getMiniMapNodeColor" />
            <Controls show-zoom show-fit-view />

            <!-- 自定义节点类型 -->
            <template #node-start="nodeProps">
              <StartNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-end="nodeProps">
              <EndNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-llm="nodeProps">
              <LLMNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-api="nodeProps">
              <APINode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-script="nodeProps">
              <ScriptNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-retrieval="nodeProps">
              <RetrievalNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-condition="nodeProps">
              <ConditionNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-task="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-code="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-tool="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-agent="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-variable="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-notification="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-input="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-output="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-sub-workflow="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-loop="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
            <template #node-parallel="nodeProps">
              <TaskNode v-bind="nodeProps" @edit="openNodeConfig" />
            </template>
          </VueFlow>
        </div>
      </div>

      <!-- 右侧配置面板 -->
      <div class="config-panel" :class="{ collapsed: !showConfigPanel }">
        <template v-if="showConfigPanel && selectedNodeRef">
          <div class="panel-header config-header">
            <h4>{{ getNodeTitle(selectedNodeRef.type) }} 配置</h4>
            <div class="config-header-actions">
              <button class="btn btn-icon btn-sm" @click="deleteSelectedNode" title="删除节点">🗑</button>
              <button class="btn btn-icon btn-sm" @click="closeConfigPanel" title="关闭">×</button>
            </div>
          </div>
          <div class="panel-body">
            <div class="config-name-row">
              <input v-model="configEditName" class="config-name-input" placeholder="节点名称" />
              <button class="btn btn-icon btn-sm" @click="confirmRename" :disabled="!configEditName.trim()" title="确认重命名">✓</button>
            </div>
            <textarea
              v-model="configEditDesc"
              class="config-desc-input"
              placeholder="添加描述..."
              rows="2"
              @input="updateNodeConfigFromPanel"
            ></textarea>
            <component
              :is="getConfigPanelComponent(selectedNodeRef.type)"
              :node="selectedNodeRef"
              @update="handlePanelUpdate"
            />
            <div class="config-actions">
              <button class="btn btn-sm" @click="copySelectedNode">📋 复制节点</button>
              <button class="btn btn-sm btn-danger" @click="deleteSelectedNode">🗑 删除节点</button>
            </div>
          </div>
        </template>
        <template v-else>
          <div class="panel-placeholder">
            <p>选择一个节点以编辑配置</p>
          </div>
        </template>
      </div>
    </div>

    <!-- 测试面板 -->
    <div v-if="showTestPanel" class="test-panel-overlay" @click.self="showTestPanel = false">
      <div class="test-panel">
        <div class="panel-header">
          <h4>工作流测试</h4>
          <button class="btn-close" @click="showTestPanel = false">×</button>
        </div>
        <div class="panel-body">
          <WorkflowTestPanel :workflow-id="workflowId" @close="showTestPanel = false" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'

import { useWorkflowStore } from '@/stores/workflow-store'
import { getNodeSchemasByCategory, getNodeSchema, NODE_SCHEMA_MAP, generateNodeId, generateEdgeId, NODE_TYPE } from '@/constants/node-schemas'
import type { NodeSchema, WorkflowNode, WorkflowEdge, WorkflowGraph } from '@/types/workflow'

// Node components
import StartNode from './dag-nodes/StartNode.vue'
import EndNode from './dag-nodes/EndNode.vue'
import LLMNode from './workflow-nodes/LLMNode.vue'
import APINode from './workflow-nodes/APINode.vue'
import ScriptNode from './workflow-nodes/ScriptNode.vue'
import RetrievalNode from './workflow-nodes/RetrievalNode.vue'
import ConditionNode from './dag-nodes/ConditionNode.vue'
import TaskNode from './dag-nodes/TaskNode.vue'
import WorkflowTestPanel from './WorkflowTestPanel.vue'

// Config panels (lazy)
import LLMPanel from './workflow-panels/LLMPanel.vue'
import APIPanel from './workflow-panels/APIPanel.vue'
import ScriptPanel from './workflow-panels/ScriptPanel.vue'
import ConditionPanel from './workflow-panels/ConditionPanel.vue'
import RetrievalPanel from './workflow-panels/RetrievalPanel.vue'
import GenericConfigPanel from './workflow-panels/GenericConfigPanel.vue'
import StartPanel from './workflow-panels/StartPanel.vue'
import EndPanel from './workflow-panels/EndPanel.vue'
import TaskPanel from './workflow-panels/TaskPanel.vue'

// Props
interface Props {
  workflowId?: string
  workflowName?: string
  initialGraph?: WorkflowGraph | null
  readOnly?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  workflowId: '',
  workflowName: '',
  readOnly: false,
})

// Emits
const emit = defineEmits<{
  save: [graph: WorkflowGraph]
}>()

const router = useRouter()
const workflowStore = useWorkflowStore()

// ==================== VueFlow setup ====================
const { screenToFlowCoordinate } = useVueFlow()

// ==================== State ====================
const showJsonView = ref(false)
const showTestPanel = ref(false)
const searchQuery = ref('')
const canvasRef = ref<HTMLElement>()
const vueFlowContainer = ref<HTMLElement>()
const jsonContent = ref('')
const nodePanelCollapsed = ref(false)
const showMiniMap = ref(true)
const isSaving = ref(false)
const isDirty = ref(false)
const workflowName = ref(props.workflowName || '')

// ==================== Flow nodes/edges (refs for v-model) ====================
const flowNodes = ref<WorkflowNode[]>([])
const flowEdges = ref<WorkflowEdge[]>([])
const selectedNodeRef = ref<WorkflowNode | null>(null)

// ==================== Computed ====================
const nodeSchemasByCategory = computed(() => getNodeSchemasByCategory())

const filteredNodeSchemas = computed(() => {
  if (!searchQuery.value) return nodeSchemasByCategory.value
  const q = searchQuery.value.toLowerCase()
  const result: Record<string, NodeSchema[]> = {}
  for (const [cat, schemas] of Object.entries(nodeSchemasByCategory.value)) {
    const filtered = schemas.filter(s => s.title.toLowerCase().includes(q) || s.desc.toLowerCase().includes(q))
    if (filtered.length > 0) result[cat] = filtered
  }
  return result
})

const showConfigPanel = computed(() => !!selectedNodeRef.value)
const canUndo = computed(() => workflowStore.canUndo)
const canRedo = computed(() => workflowStore.canRedo)

// ==================== Init ====================
onMounted(() => {
  if (props.initialGraph) {
    loadGraph(props.initialGraph)
  }
  window.addEventListener('keydown', handleKeyboard)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyboard)
})

watch(() => props.initialGraph, (val) => {
  if (val) loadGraph(val)
})

function loadGraph(graph: WorkflowGraph) {
  flowNodes.value = (graph.nodes || []) as WorkflowNode[]
  flowEdges.value = (graph.edges || []) as WorkflowEdge[]
  workflowStore.setGraph(graph)
  isDirty.value = false
}

// ==================== Node/Schema helpers ====================
function getNodeTitle(type: string): string {
  const schema = NODE_SCHEMA_MAP[type]
  return schema?.title || type
}

function getMiniMapNodeColor(node: any): string {
  const schema = NODE_SCHEMA_MAP[node.type]
  return schema?.bgColor || '#ccc'
}

// ==================== Drag & Drop ====================
function onDragStart(event: DragEvent, schema: NodeSchema) {
  event.dataTransfer!.setData('application/node-schema', JSON.stringify(schema))
  event.dataTransfer!.effectAllowed = 'move'
}

function onDragOver(event: DragEvent) {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

function onDrop(event: DragEvent) {
  const schema = parseDroppedSchema(event)
  if (!schema) return
  const position = dropPosition(event)
  if (!position) return
  addNewNodeAt(schema, position, false)
}

function parseDroppedSchema(event: DragEvent): NodeSchema | null {
  const schemaData = event.dataTransfer?.getData('application/node-schema')
  return schemaData ? JSON.parse(schemaData) : null
}

function dropPosition(event: DragEvent): { x: number; y: number } | null {
  const bounds = vueFlowContainer.value?.getBoundingClientRect()
  if (!bounds) return null
  return screenToFlowCoordinate({ x: event.clientX - bounds.left, y: event.clientY - bounds.top })
}

// ==================== Add Node ====================
function addNodeBySchema(schema: NodeSchema) {
  if (isDuplicateTerminalNode(schema)) return
  addNewNodeAt(schema, randomPosition(), true)
}

function isDuplicateTerminalNode(schema: NodeSchema): boolean {
  if (schema.type !== NODE_TYPE.START && schema.type !== NODE_TYPE.END) return false
  return flowNodes.value.some(n => n.type === schema.type)
}

function randomPosition(): { x: number; y: number } {
  return { x: 200 + Math.random() * 200, y: 200 + Math.random() * 200 }
}

function addNewNodeAt(schema: NodeSchema, position: { x: number; y: number }, select: boolean): void {
  const newNode = buildWorkflowNode(schema, position)
  flowNodes.value.push(newNode)
  workflowStore.addNode(newNode)
  if (select) selectedNodeRef.value = newNode
  markDirty()
}

function buildWorkflowNode(schema: NodeSchema, position: { x: number; y: number }): WorkflowNode {
  return { id: generateNodeId(schema.type), type: schema.type, position, data: buildNodeData(schema) }
}

function buildNodeData(schema: NodeSchema) {
  return {
    label: schema.title,
    desc: schema.desc,
    input_params: JSON.parse(JSON.stringify(schema.defaultParams.input_params)),
    output_params: JSON.parse(JSON.stringify(schema.defaultParams.output_params)),
    node_param: JSON.parse(JSON.stringify(schema.defaultParams.node_param)),
  }
}

// ==================== Click handlers ====================
function onNodeClick(event: any) {
  const node = event.node as WorkflowNode
  selectedNodeRef.value = node
  configEditName.value = node.data.label || ''
  configEditDesc.value = node.data.desc || ''
  workflowStore.selectNode(node)
}

function onPaneClick() {
  selectedNodeRef.value = null
  workflowStore.selectNode(null)
}

function hasCycle(nodeId: string, targetId: string, visited: Set<string> = new Set()): boolean {
  if (nodeId === targetId) return true
  if (visited.has(nodeId)) return false
  visited.add(nodeId)

  const outgoing = flowEdges.value.filter(e => e.source === nodeId)
  for (const edge of outgoing) {
    if (hasCycle(edge.target, targetId, visited)) return true
  }
  return false
}

function onConnect(connection: any) {
  if (canConnect(connection)) addConnection(connection)
}

function canConnect(connection: any): boolean {
  if (!connection.source || !connection.target) return false
  if (isSelfConnection(connection)) return false
  if (hasCycle(connection.target, connection.source)) {
    alert('不能形成环路连接')
    return false
  }
  return true
}

function isSelfConnection(connection: any): boolean {
  return connection.source === connection.target
}

function addConnection(connection: any) {
  const newEdge = buildWorkflowEdge(connection)
  flowEdges.value.push(newEdge)
  workflowStore.addEdge(newEdge)
  markDirty()
}

function buildWorkflowEdge(connection: any): WorkflowEdge {
  return {
    id: generateEdgeId(),
    source: connection.source,
    target: connection.target,
    sourceHandle: connection.sourceHandle,
    targetHandle: connection.targetHandle,
  }
}

function onNodesChange(changes: any) {
  for (const change of changes) { if (isPositionChange(change)) applyPositionChange(change) }
}

function isPositionChange(change: any): boolean {
  return change.type === 'position' && Boolean(change.position)
}

function applyPositionChange(change: any) {
  const node = flowNodes.value.find(n => n.id === change.id)
  if (!node) return
  node.position = change.position
  workflowStore.updateNode(change.id, { position: change.position })
  markDirty()
}

function onEdgesChange(changes: any) {
  // VueFlow handles edge deletions automatically
}

function onNodesDelete(nodes: any[]) {
  const allowDelete = filterDeletableNodes(nodes)
  notifyIfHasSystemNode(nodes, allowDelete)
  deleteAllowListedNodes(allowDelete)
  markDirty()
}

function filterDeletableNodes(nodes: any[]): any[] {
  return nodes.filter((n: any) => !NODE_SCHEMA_MAP[n.type]?.isSystem)
}

function notifyIfHasSystemNode(allNodes: any[], allowDelete: any[]) {
  if (allowDelete.length !== allNodes.length) alert('系统节点不允许删除')
}

function deleteAllowListedNodes(allowDelete: any[]) {
  for (const node of allowDelete) {
    workflowStore.deleteNode(node.id)
    if (selectedNodeRef.value?.id === node.id) selectedNodeRef.value = null
  }
}

// ==================== Config Panel ====================
function openNodeConfig(nodeId: string) {
  const node = flowNodes.value.find(n => n.id === nodeId)
  if (node) {
    selectedNodeRef.value = node
    workflowStore.selectNode(node)
  }
}

function closeConfigPanel() {
  selectedNodeRef.value = null
}

const PANEL_MAP: Record<string, any> = {
  start: markRaw(StartPanel),
  end: markRaw(EndPanel),
  llm: markRaw(LLMPanel),
  api: markRaw(APIPanel),
  script: markRaw(ScriptPanel),
  retrieval: markRaw(RetrievalPanel),
  condition: markRaw(ConditionPanel),
  code: markRaw(GenericConfigPanel),
  tool: markRaw(GenericConfigPanel),
  agent: markRaw(GenericConfigPanel),
  variable: markRaw(GenericConfigPanel),
  notification: markRaw(GenericConfigPanel),
  input: markRaw(GenericConfigPanel),
  output: markRaw(GenericConfigPanel),
  'sub-workflow': markRaw(GenericConfigPanel),
  loop: markRaw(GenericConfigPanel),
  parallel: markRaw(GenericConfigPanel),
  task: markRaw(GenericConfigPanel),
}

function getConfigPanelComponent(type: string): any {
  return PANEL_MAP[type] || markRaw(GenericConfigPanel)
}

function handlePanelUpdate(updates: any) {
  if (!selectedNodeRef.value) return
  const nodeId = selectedNodeRef.value.id
  const node = flowNodes.value.find(n => n.id === nodeId)
  if (!node) return

  Object.assign(node.data, updates)
  workflowStore.updateNode(nodeId, { data: node.data })
  markDirty()
}

const configEditName = ref('')
const configEditDesc = ref('')

function configEditDescStr(): string {
  return configEditDesc.value
}

function confirmRename() {
  if (!selectedNodeRef.value || !configEditName.value.trim()) return
  selectedNodeRef.value.data.label = configEditName.value.trim()
  updateNodeConfigFromPanel()
}

function updateNodeConfigFromPanel() {
  if (!selectedNodeRef.value) return
  const nodeId = selectedNodeRef.value.id
  // 同步描述
  if (configEditDesc.value !== undefined) {
    selectedNodeRef.value.data.desc = configEditDesc.value
  }
  workflowStore.updateNode(nodeId, { data: selectedNodeRef.value.data })
  markDirty()
}

function copySelectedNode() {
  const source = selectedNodeRef.value
  if (!source) return
  if (isSystemNode(source.type)) {
    alert('系统节点不允许复制')
    return
  }
  addCopiedNode(source)
}

function isSystemNode(type: string): boolean {
  return Boolean(NODE_SCHEMA_MAP[type]?.isSystem)
}

function addCopiedNode(source: WorkflowNode): void {
  const newNode = cloneNode(source)
  flowNodes.value.push(newNode)
  workflowStore.addNode(newNode)
  selectedNodeRef.value = newNode
  markDirty()
}

function cloneNode(source: WorkflowNode): WorkflowNode {
  return {
    ...deepClone(source),
    id: generateNodeId(source.type),
    position: offsetPosition(source.position),
    selected: true,
    data: copyNodeData(source.data),
  }
}

function deepClone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value))
}

function offsetPosition(position: { x: number; y: number }): { x: number; y: number } {
  return { x: position.x + 200, y: position.y + 80 }
}

function copyNodeData(data: any): any {
  return { ...deepClone(data), label: data.label + '_副本' }
}

function deleteSelectedNode() {
  if (!selectedNodeRef.value) return
  const nodeId = selectedNodeRef.value.id
  flowNodes.value = flowNodes.value.filter(n => n.id !== nodeId)
  flowEdges.value = flowEdges.value.filter(e => e.source !== nodeId && e.target !== nodeId)
  workflowStore.deleteNode(nodeId)
  selectedNodeRef.value = null
  markDirty()
}

// ==================== Toolbar actions ====================
function toggleJsonView() {
  showJsonView.value = !showJsonView.value
  if (showJsonView.value) {
    jsonContent.value = JSON.stringify({ nodes: flowNodes.value, edges: flowEdges.value }, null, 2)
  }
}

function applyJsonChanges() {
  try { applyParsedGraph(JSON.parse(jsonContent.value)) } catch (err) { console.error('JSON解析错误:', err) }
}

function applyParsedGraph(graph: { nodes?: any[]; edges?: any[] }) {
  flowNodes.value = graph.nodes || []; flowEdges.value = graph.edges || []
  workflowStore.setGraph(graph)
  markDirty()
}

function toggleMiniMap() {
  showMiniMap.value = !showMiniMap.value
}

function undo() {
  workflowStore.undo()
  // Sync with VueFlow
  flowNodes.value = [...workflowStore.nodes]
  flowEdges.value = [...workflowStore.edges]
}

function redo() {
  workflowStore.redo()
  flowNodes.value = [...workflowStore.nodes]
  flowEdges.value = [...workflowStore.edges]
}

function validateWorkflow() {
  const errors = collectWorkflowErrors()
  showValidationResult(errors)
}

function collectWorkflowErrors(): string[] {
  const errors: string[] = []
  checkStartNode(errors)
  checkEndNode(errors)
  checkDisconnectedNodes(errors)
  return errors
}

function checkStartNode(errors: string[]): void {
  if (!flowNodes.value.some(n => n.type === NODE_TYPE.START)) errors.push('缺少开始节点')
}

function checkEndNode(errors: string[]): void {
  if (!flowNodes.value.some(n => n.type === NODE_TYPE.END)) errors.push('缺少结束节点')
}

function checkDisconnectedNodes(errors: string[]): void {
  const connectedIds = collectConnectedNodeIds()
  const orphans = flowNodes.value.filter(n => !connectedIds.has(n.id) && n.type !== NODE_TYPE.START)
  if (orphans.length > 0) errors.push(`${orphans.length} 个节点未连接`)
}

function collectConnectedNodeIds(): Set<string> {
  const ids = new Set<string>()
  flowEdges.value.forEach(e => { ids.add(e.source); ids.add(e.target) })
  return ids
}

function showValidationResult(errors: string[]): void {
  if (errors.length === 0) { alert('✅ 工作流校验通过'); return }
  alert('⚠️ 校验发现问题:\n' + errors.join('\n'))
}

interface LevelAssignment {
  levels: Map<string, number>
  visited: Set<string>
  edges: WorkflowEdge[]
}

function autoLayout() {
  const { startNode, endNode, otherNodes } = categorizeNodes(flowNodes.value)
  const y = positionStartNode(startNode)
  const levels = computeNodeLevels(startNode, otherNodes, flowEdges.value)
  const groups = groupNodesByLevel(levels, startNode, endNode)
  applyLevelPositions(groups, y)
  applyEndPosition(endNode, levels)
  persistLayout()
}

function categorizeNodes(nodes: WorkflowNode[]): { startNode: WorkflowNode | undefined; endNode: WorkflowNode | undefined; otherNodes: WorkflowNode[] } {
  return {
    startNode: nodes.find(n => n.type === NODE_TYPE.START),
    endNode: nodes.find(n => n.type === NODE_TYPE.END),
    otherNodes: nodes.filter(n => n.type !== NODE_TYPE.START && n.type !== NODE_TYPE.END),
  }
}

function positionStartNode(node: WorkflowNode | undefined): number {
  if (!node) return 150
  node.position = { x: 200, y: 150 }
  return 270
}

function computeNodeLevels(start: WorkflowNode | undefined, others: WorkflowNode[], edges: WorkflowEdge[]): Map<string, number> {
  const ctx: LevelAssignment = { levels: new Map(), visited: new Set(), edges }
  if (!start) {
    others.forEach((n, i) => ctx.levels.set(n.id, i))
    return ctx.levels
  }
  assignLevel(ctx, start.id, 0)
  return ctx.levels
}

function assignLevel(ctx: LevelAssignment, nodeId: string, level: number): void {
  if (ctx.visited.has(nodeId)) return
  ctx.visited.add(nodeId)
  const current = ctx.levels.get(nodeId) ?? level
  ctx.levels.set(nodeId, Math.max(current, level))
  for (const edge of ctx.edges) {
    if (edge.source === nodeId) assignLevel(ctx, edge.target, level + 1)
  }
}

function groupNodesByLevel(levels: Map<string, number>, start: WorkflowNode | undefined, end: WorkflowNode | undefined): Map<number, WorkflowNode[]> {
  const groups = new Map<number, WorkflowNode[]>()
  for (const [nodeId, level] of levels) {
    const node = flowNodes.value.find(n => n.id === nodeId)
    if (!node || node === start || node === end) continue
    addToBucket(groups, level, node)
  }
  return groups
}

function addToBucket(groups: Map<number, WorkflowNode[]>, level: number, node: WorkflowNode): void {
  const bucket = groups.get(level) ?? []
  bucket.push(node)
  groups.set(level, bucket)
}

function applyLevelPositions(groups: Map<number, WorkflowNode[]>, startY: number): void {
  for (const [level, nodes] of groups) {
    const xOffset = 200 + level * 280
    nodes.forEach((node, i) => { node.position = { x: xOffset, y: startY + i * 120 } })
  }
}

function applyEndPosition(end: WorkflowNode | undefined, levels: Map<string, number>): void {
  if (!end) return
  const maxLevel = Math.max(...levels.values(), 0)
  end.position = { x: 200 + (maxLevel + 1) * 280, y: 150 }
}

function persistLayout(): void {
  workflowStore.setGraph({ nodes: flowNodes.value, edges: flowEdges.value })
  markDirty()
}

// ==================== Save ====================
async function handleSave() {
  if (isSaving.value) return
  isSaving.value = true
  emit('save', buildWorkflowGraph())
  setTimeout(resetSavingState, 500)
}

function buildWorkflowGraph(): WorkflowGraph {
  return { nodes: flowNodes.value, edges: flowEdges.value }
}

function resetSavingState() {
  isSaving.value = false
  isDirty.value = false
}

function markDirty() {
  isDirty.value = true
}

// ==================== Keyboard ====================
const KEYBOARD_ACTIONS: Record<string, () => void> = {
  's': handleSave,
  'z': undo,
  'y': redo,
}

function handleKeyboard(event: KeyboardEvent) {
  if (props.readOnly) return
  if (!isCtrlOrMeta(event)) return
  const action = KEYBOARD_ACTIONS[event.key]
  if (action) {
    event.preventDefault()
    action()
  }
}

function isCtrlOrMeta(event: KeyboardEvent): boolean {
  return event.ctrlKey || event.metaKey
}

function goBack() {
  router.push('/agenthub/dag-workflows')
}
</script>

<style scoped>
/* ==================== Layout ==================== */
.workflow-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-stripe);
  overflow: hidden;
}

/* ==================== Toolbar ==================== */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: var(--bg-card-solid);
  border-bottom: 1px solid var(--color-border);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  z-index: 10;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.workflow-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.workflow-title-input {
  font-size: 16px;
  font-weight: 600;
  border: none;
  background: transparent;
  color: var(--color-heading);
  outline: none;
  min-width: 150px;
  padding: 4px 8px;
  border-radius: 4px;
}
.workflow-title-input:focus {
  background: var(--bg-stripe);
}
.workflow-title-input:disabled {
  color: #999;
}

.dirty-indicator {
  color: var(--color-warning);
  font-size: 11px;
}

.toolbar-center,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* ==================== Buttons ==================== */
.btn {
  padding: 6px 14px;
  border: 1px solid var(--color-border-strong);
  border-radius: 6px;
  background: var(--bg-card-solid);
  color: var(--color-text);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.btn:hover:not(:disabled) { border-color: var(--color-primary); color: var(--color-primary); }
.btn:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-primary { background: var(--color-primary); border-color: var(--color-primary); color: var(--color-text-inverse); }
.btn-primary:hover:not(:disabled) { background: var(--color-primary-light); border-color: var(--color-primary-light); color: var(--color-text-inverse); }
.btn-secondary { background: var(--bg-elevated); border-color: var(--color-border-strong); color: var(--color-text); }
.btn-secondary:hover:not(:disabled) { border-color: var(--color-primary); color: var(--color-primary); }
.btn-icon { padding: 6px 10px; font-size: 16px; line-height: 1; }
.btn-icon.active { background: var(--color-info-subtle); border-color: var(--color-primary); color: var(--color-primary); }
.btn-sm { font-size: 12px; padding: 4px 8px; }
.divider { width: 1px; height: 20px; background: var(--color-border); margin: 0 2px; }
.test-btn-group .btn.active { background: var(--color-info-subtle); border-color: var(--color-primary); color: var(--color-primary); }

/* ==================== Editor Body ==================== */
.editor-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ==================== Left Node Panel ==================== */
.node-panel {
  width: 240px;
  background: var(--bg-card-solid);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  position: relative;
  transition: width 0.2s;
  flex-shrink: 0;
}
.node-panel.collapsed {
  width: 32px;
}

.panel-toggle {
  position: absolute;
  right: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 48px;
  background: var(--bg-card-solid);
  border: 1px solid var(--color-border);
  border-radius: 0 4px 4px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 5;
  font-size: 10px;
  color: #999;
}
.panel-toggle:hover {
  background: var(--bg-stripe);
}

.panel-header {
  padding: 12px;
  border-bottom: 1px solid var(--bg-stripe);
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border-strong);
  border-radius: 6px;
  font-size: 13px;
  outline: none;
  box-sizing: border-box;
}
.search-input:focus {
  border-color: var(--color-primary);
}

.node-categories {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.node-category {
  margin-bottom: 16px;
}

.category-title {
  font-size: 11px;
  font-weight: 600;
  color: #999;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.node-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.node-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 10px;
  background: var(--bg-elevated);
  border: 1px solid var(--bg-stripe);
  border-radius: 6px;
  cursor: grab;
  transition: all 0.15s;
}
.node-item:hover {
  background: var(--color-info-subtle);
  border-color: var(--color-primary-subtle);
  transform: translateX(3px);
}
.node-item:active {
  cursor: grabbing;
}

.node-icon {
  font-size: 18px;
  flex-shrink: 0;
  line-height: 1.2;
}

.node-info {
  flex: 1;
  min-width: 0;
}

.node-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
}

.node-desc {
  font-size: 11px;
  color: #999;
  line-height: 1.3;
  margin-top: 2px;
}

/* ==================== Canvas Area ==================== */
.canvas-area {
  flex: 1;
  position: relative;
  background: var(--bg-elevated);
  overflow: hidden;
}

.visual-view {
  width: 100%;
  height: 100%;
}

.vue-flow-instance {
  width: 100%;
  height: 100%;
}

.json-view {
  width: 100%;
  height: 100%;
  padding: 0;
}

.json-editor {
  width: 100%;
  height: 100%;
  border: none;
  padding: 20px;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  resize: none;
  outline: none;
  background: var(--color-text);
  color: var(--color-border-strong);
}

/* ==================== Config Panel ==================== */
.config-panel {
  width: 320px;
  background: var(--bg-card-solid);
  border-left: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.2s;
}
.config-panel.collapsed {
  width: 0;
  overflow: hidden;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-header h4 {
  margin: 0;
  font-size: 14px;
  color: var(--color-text);
  flex: 1;
}

.config-header-actions {
  display: flex;
  gap: 4px;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.panel-placeholder {
  display: flex;
  height: 100%;
  align-items: center;
  justify-content: center;
  color: #bbb;
  font-size: 13px;
  padding: 20px;
  text-align: center;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #666;
  margin-bottom: 6px;
}

.form-group input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--color-border-strong);
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}
.form-group input:focus {
  outline: none;
  border-color: var(--color-primary);
}

/* ==================== Config Panel Updates ==================== */
.config-name-row {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
}

.config-name-input {
  flex: 1;
  padding: 8px 10px;
  border: 1px solid var(--color-border-strong);
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  outline: none;
}
.config-name-input:focus {
  border-color: var(--color-primary);
}

.config-desc-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--color-border-strong);
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  resize: vertical;
  outline: none;
  box-sizing: border-box;
  margin-bottom: 12px;
  font-family: inherit;
}
.config-desc-input:focus {
  border-color: var(--color-primary);
}

.config-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--bg-stripe);
}

.btn-danger {
  color: var(--color-error);
  border-color: var(--color-error-light);
}
.btn-danger:hover:not(:disabled) {
  background: var(--color-error-subtle);
  border-color: var(--color-error);
  color: var(--color-error);
}

/* ==================== Test Panel Overlay ==================== */
.test-panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
  align-items: stretch;
}

.test-panel {
  width: 480px;
  background: var(--bg-card-solid);
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 12px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
}

.test-panel .panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--bg-stripe);
}

.test-panel .panel-header h4 {
  margin: 0;
  font-size: 15px;
}

.btn-close {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-close:hover {
  background: var(--bg-stripe);
}

.test-panel .panel-body {
  padding: 20px;
  overflow-y: auto;
}
</style>
