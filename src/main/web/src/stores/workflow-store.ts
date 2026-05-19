/**
 * 工作流状态管理Store
 * 使用Pinia管理工作流的全局状态
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { 
  WorkflowNode, 
  WorkflowEdge, 
  WorkflowGraph,
  VarTreeItem,
  WorkflowExecution,
  NodeResult
} from '@/types/workflow-node'
import { buildVariableTree } from '@/utils/variable-system'

export const useWorkflowStore = defineStore('workflow', () => {
  // ==================== 状态定义 ====================
  
  /**
   * 当前工作流ID
   */
  const workflowId = ref<string>('')
  
  /**
   * 工作流名称
   */
  const workflowName = ref<string>('')
  
  /**
   * 工作流描述
   */
  const workflowDesc = ref<string>('')
  
  /**
   * 节点列表
   */
  const nodes = ref<WorkflowNode[]>([])
  
  /**
   * 边列表
   */
  const edges = ref<WorkflowEdge[]>([])
  
  /**
   * 当前选中的节点
   */
  const selectedNode = ref<WorkflowNode | null>(null)
  
  /**
   * 当前选中的边
   */
  const selectedEdge = ref<WorkflowEdge | null>(null)
  
  /**
   * 是否显示测试面板
   */
  const showTestPanel = ref<boolean>(false)
  
  /**
   * 是否显示配置面板
   */
  const showConfigPanel = ref<boolean>(false)
  
  /**
   * 是否显示小地图
   */
  const showMiniMap = ref<boolean>(true)
  
  /**
   * 是否只读模式
   */
  const readOnly = ref<boolean>(false)
  
  /**
   * 是否正在保存
   */
  const isSaving = ref<boolean>(false)
  
  /**
   * 是否已修改
   */
  const isDirty = ref<boolean>(false)
  
  /**
   * 当前执行结果
   */
  const execution = ref<WorkflowExecution | null>(null)
  
  /**
   * 历史记录（撤销栈）
   */
  const historyStack = ref<WorkflowGraph[]>([])
  
  /**
   * 未来记录（重做栈）
   */
  const futureStack = ref<WorkflowGraph[]>([])
  
  /**
   * 最大历史记录数
   */
  const MAX_HISTORY = 50
  
  // ==================== 计算属性 ====================
  
  /**
   * 当前工作流图数据
   */
  const graph = computed<WorkflowGraph>(() => ({
    nodes: nodes.value,
    edges: edges.value
  }))
  
  /**
   * 当前选中节点的可用变量树
   */
  const availableVariables = computed<VarTreeItem[]>(() => {
    if (!selectedNode.value) return []
    return buildVariableTree(selectedNode.value.id, nodes.value, edges.value)
  })
  
  /**
   * 节点数量
   */
  const nodeCount = computed(() => nodes.value.length)
  
  /**
   * 边数量
   */
  const edgeCount = computed(() => edges.value.length)
  
  /**
   * 是否可以撤销
   */
  const canUndo = computed(() => historyStack.value.length > 0)
  
  /**
   * 是否可以重做
   */
  const canRedo = computed(() => futureStack.value.length > 0)
  
  // ==================== 方法 ====================
  
  /**
   * 设置工作流基本信息
   */
  function setWorkflowInfo(id: string, name: string, desc: string) {
    workflowId.value = id
    workflowName.value = name
    workflowDesc.value = desc
  }
  
  /**
   * 设置图数据
   */
  function setGraph(newGraph: WorkflowGraph) {
    nodes.value = newGraph.nodes
    edges.value = newGraph.edges
    isDirty.value = true
  }
  
  /**
   * 添加节点
   */
  function addNode(node: WorkflowNode) {
    saveToHistory()
    nodes.value.push(node)
    isDirty.value = true
  }
  
  /**
   * 更新节点
   */
  function updateNode(nodeId: string, updates: Partial<WorkflowNode>) {
    saveToHistory()
    const index = nodes.value.findIndex(n => n.id === nodeId)
    if (index !== -1) {
      nodes.value[index] = { ...nodes.value[index], ...updates }
      isDirty.value = true
      
      // 如果更新的是当前选中的节点，同步更新selectedNode
      if (selectedNode.value?.id === nodeId) {
        selectedNode.value = nodes.value[index]
      }
    }
  }
  
  /**
   * 删除节点
   */
  function deleteNode(nodeId: string) {
    saveToHistory()
    
    // 删除节点
    nodes.value = nodes.value.filter(n => n.id !== nodeId)
    
    // 删除相关的边
    edges.value = edges.value.filter(
      e => e.source !== nodeId && e.target !== nodeId
    )
    
    // 如果删除的是选中的节点，清空选中
    if (selectedNode.value?.id === nodeId) {
      selectedNode.value = null
    }
    
    isDirty.value = true
  }
  
  /**
   * 添加边
   */
  function addEdge(edge: WorkflowEdge) {
    saveToHistory()
    edges.value.push(edge)
    isDirty.value = true
  }
  
  /**
   * 更新边
   */
  function updateEdge(edgeId: string, updates: Partial<WorkflowEdge>) {
    saveToHistory()
    const index = edges.value.findIndex(e => e.id === edgeId)
    if (index !== -1) {
      edges.value[index] = { ...edges.value[index], ...updates }
      isDirty.value = true
    }
  }
  
  /**
   * 删除边
   */
  function deleteEdge(edgeId: string) {
    saveToHistory()
    edges.value = edges.value.filter(e => e.id !== edgeId)
    
    if (selectedEdge.value?.id === edgeId) {
      selectedEdge.value = null
    }
    
    isDirty.value = true
  }
  
  /**
   * 选中节点
   */
  function selectNode(node: WorkflowNode | null) {
    selectedNode.value = node
    selectedEdge.value = null
    showConfigPanel.value = node !== null
  }
  
  /**
   * 选中边
   */
  function selectEdge(edge: WorkflowEdge | null) {
    selectedEdge.value = edge
    selectedNode.value = null
  }
  
  /**
   * 保存到历史记录
   */
  function saveToHistory() {
    // 限制历史记录数量
    if (historyStack.value.length >= MAX_HISTORY) {
      historyStack.value.shift()
    }
    
    // 保存当前状态
    historyStack.value.push({
      nodes: JSON.parse(JSON.stringify(nodes.value)),
      edges: JSON.parse(JSON.stringify(edges.value))
    })
    
    // 清空重做栈
    futureStack.value = []
  }
  
  /**
   * 撤销
   */
  function undo() {
    if (!canUndo.value) return
    
    // 保存当前状态到重做栈
    futureStack.value.push({
      nodes: JSON.parse(JSON.stringify(nodes.value)),
      edges: JSON.parse(JSON.stringify(edges.value))
    })
    
    // 恢复上一个状态
    const prevState = historyStack.value.pop()!
    nodes.value = prevState.nodes
    edges.value = prevState.edges
    isDirty.value = true
  }
  
  /**
   * 重做
   */
  function redo() {
    if (!canRedo.value) return
    
    // 保存当前状态到撤销栈
    historyStack.value.push({
      nodes: JSON.parse(JSON.stringify(nodes.value)),
      edges: JSON.parse(JSON.stringify(edges.value))
    })
    
    // 恢复下一个状态
    const nextState = futureStack.value.pop()!
    nodes.value = nextState.nodes
    edges.value = nextState.edges
    isDirty.value = true
  }
  
  /**
   * 清空历史记录
   */
  function clearHistory() {
    historyStack.value = []
    futureStack.value = []
  }
  
  /**
   * 标记为已保存
   */
  function markAsSaved() {
    isDirty.value = false
    isSaving.value = false
  }
  
  /**
   * 设置执行结果
   */
  function setExecution(result: WorkflowExecution | null) {
    execution.value = result
  }
  
  /**
   * 更新节点执行结果
   */
  function updateNodeResult(nodeId: string, result: NodeResult) {
    if (!execution.value) return
    
    const index = execution.value.node_results.findIndex(r => r.node_id === nodeId)
    if (index !== -1) {
      execution.value.node_results[index] = result
    } else {
      execution.value.node_results.push(result)
    }
  }
  
  /**
   * 重置Store
   */
  function reset() {
    workflowId.value = ''
    workflowName.value = ''
    workflowDesc.value = ''
    nodes.value = []
    edges.value = []
    selectedNode.value = null
    selectedEdge.value = null
    showTestPanel.value = false
    showConfigPanel.value = false
    execution.value = null
    historyStack.value = []
    futureStack.value = []
    isDirty.value = false
    isSaving.value = false
  }
  
  return {
    // 状态
    workflowId,
    workflowName,
    workflowDesc,
    nodes,
    edges,
    selectedNode,
    selectedEdge,
    showTestPanel,
    showConfigPanel,
    showMiniMap,
    readOnly,
    isSaving,
    isDirty,
    execution,
    historyStack,
    futureStack,
    
    // 计算属性
    graph,
    availableVariables,
    nodeCount,
    edgeCount,
    canUndo,
    canRedo,
    
    // 方法
    setWorkflowInfo,
    setGraph,
    addNode,
    updateNode,
    deleteNode,
    addEdge,
    updateEdge,
    deleteEdge,
    selectNode,
    selectEdge,
    saveToHistory,
    undo,
    redo,
    clearHistory,
    markAsSaved,
    setExecution,
    updateNodeResult,
    reset
  }
})
