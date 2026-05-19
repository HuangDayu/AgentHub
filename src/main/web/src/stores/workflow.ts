/**
 * 工作流状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { 
  Workflow, 
  WorkflowGraph, 
  WorkflowNode,
  WorkflowEdge,
  WorkflowExecution,
  NodeResult
} from '@/types/workflow'
import {
  listWorkflows,
  getWorkflow,
  createWorkflow,
  updateWorkflow,
  deleteWorkflow,
  executeWorkflow,
  getExecutionResult,
  createWorkflowEventStream
} from '@/api/workflow-api'

export const useWorkflowStore = defineStore('workflow', () => {
  // 状态
  const workflows = ref<Workflow[]>([])
  const currentWorkflow = ref<Workflow | null>(null)
  const currentGraph = ref<WorkflowGraph>({ nodes: [], edges: [] })
  const currentExecution = ref<WorkflowExecution | null>(null)
  const selectedNodeId = ref<string | null>(null)
  const isDirty = ref(false)
  const isLoading = ref(false)
  
  // SSE连接
  let eventSource: EventSource | null = null

  // 计算属性
  const selectedNode = computed(() => {
    if (!selectedNodeId.value) return null
    return currentGraph.value.nodes.find(n => n.id === selectedNodeId.value)
  })

  const nodeMap = computed(() => {
    const map = new Map<string, WorkflowNode>()
    currentGraph.value.nodes.forEach(node => {
      map.set(node.id, node)
    })
    return map
  })

  const edgeMap = computed(() => {
    const map = new Map<string, WorkflowEdge>()
    currentGraph.value.edges.forEach(edge => {
      map.set(edge.id, edge)
    })
    return map
  })

  // 方法
  async function loadWorkflows(selection: { tenantId: string; workspaceId: string }) {
    isLoading.value = true
    try {
      workflows.value = await listWorkflows(selection)
    } finally {
      isLoading.value = false
    }
  }

  async function loadWorkflow(selection: { tenantId: string; workspaceId: string }, workflowId: string) {
    isLoading.value = true
    try {
      currentWorkflow.value = await getWorkflow(selection, workflowId)
      currentGraph.value = JSON.parse(currentWorkflow.value.graphDefinition)
      isDirty.value = false
    } finally {
      isLoading.value = false
    }
  }

  async function saveWorkflow(selection: { tenantId: string; workspaceId: string }) {
    if (!currentWorkflow.value) return
    
    const graphDefinition = JSON.stringify(currentGraph.value)
    
    if (currentWorkflow.value.id) {
      currentWorkflow.value = await updateWorkflow(
        selection,
        currentWorkflow.value.id,
        currentWorkflow.value.name,
        currentWorkflow.value.description,
        graphDefinition
      )
    } else {
      const newWorkflow = await createWorkflow(
        selection,
        `workflow_${Date.now()}`,
        currentWorkflow.value.name,
        currentWorkflow.value.description,
        graphDefinition
      )
      currentWorkflow.value = newWorkflow
    }
    
    isDirty.value = false
  }

  async function removeWorkflow(selection: { tenantId: string; workspaceId: string }, workflowId: string) {
    await deleteWorkflow(selection, workflowId)
    workflows.value = workflows.value.filter(w => w.id !== workflowId)
  }

  function addNode(node: WorkflowNode) {
    currentGraph.value.nodes.push(node)
    isDirty.value = true
  }

  function updateNode(nodeId: string, updates: Partial<WorkflowNode>) {
    const index = currentGraph.value.nodes.findIndex(n => n.id === nodeId)
    if (index >= 0) {
      currentGraph.value.nodes[index] = { ...currentGraph.value.nodes[index], ...updates }
      isDirty.value = true
    }
  }

  function removeNode(nodeId: string) {
    currentGraph.value.nodes = currentGraph.value.nodes.filter(n => n.id !== nodeId)
    currentGraph.value.edges = currentGraph.value.edges.filter(
      e => e.source !== nodeId && e.target !== nodeId
    )
    isDirty.value = true
  }

  function addEdge(edge: WorkflowEdge) {
    currentGraph.value.edges.push(edge)
    isDirty.value = true
  }

  function removeEdge(edgeId: string) {
    currentGraph.value.edges = currentGraph.value.edges.filter(e => e.id !== edgeId)
    isDirty.value = true
  }

  function selectNode(nodeId: string | null) {
    selectedNodeId.value = nodeId
  }

  function updateNodeStatus(nodeId: string, status: string) {
    const node = currentGraph.value.nodes.find(n => n.id === nodeId)
    if (node) {
      node.status = status as any
    }
  }

  async function execute(
    selection: { tenantId: string; workspaceId: string },
    input: Record<string, any> = {}
  ) {
    if (!currentWorkflow.value?.id) return
    
    currentExecution.value = await executeWorkflow(selection, currentWorkflow.value.id, input)
    
    // 建立SSE连接
    eventSource = createWorkflowEventStream(
      selection,
      currentWorkflow.value.id,
      currentExecution.value.task_id,
      handleSSEEvent,
      handleSSEError
    )
  }

  function handleSSEEvent(event: any) {
    if (event.type === 'node_start') {
      updateNodeStatus(event.data.node_id, 'running')
    } else if (event.type === 'node_complete') {
      updateNodeStatus(event.data.node_id, event.data.status)
    } else if (event.type === 'workflow_complete') {
      currentExecution.value = event.data
      closeEventSource()
    }
  }

  function handleSSEError(error: Error) {
    console.error('SSE error:', error)
    closeEventSource()
  }

  function closeEventSource() {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
  }

  function reset() {
    currentWorkflow.value = null
    currentGraph.value = { nodes: [], edges: [] }
    currentExecution.value = null
    selectedNodeId.value = null
    isDirty.value = false
    closeEventSource()
  }

  return {
    // 状态
    workflows,
    currentWorkflow,
    currentGraph,
    currentExecution,
    selectedNodeId,
    isDirty,
    isLoading,
    
    // 计算属性
    selectedNode,
    nodeMap,
    edgeMap,
    
    // 方法
    loadWorkflows,
    loadWorkflow,
    saveWorkflow,
    removeWorkflow,
    addNode,
    updateNode,
    removeNode,
    addEdge,
    removeEdge,
    selectNode,
    updateNodeStatus,
    execute,
    reset
  }
})
