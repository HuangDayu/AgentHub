/**
 * 工作流API封装
 * 包含工作流的CRUD、执行、调试等操作
 */

import { runtimeConfig } from '@/common/runtime-config'
import type { 
  Workflow, 
  WorkflowGraph,
  WorkflowExecution,
  NodeResult,
  SSEEvent 
} from '@/types/workflow'
import { requestJson } from './http'

export interface Selection {
  tenantId: string
  workspaceId: string
}

function buildHeaders(selection: Selection) {
  return {
    'X-Tenant-Id': selection.tenantId,
    'X-Workspace-Id': selection.workspaceId,
  }
}

// ==================== 工作流CRUD ====================

/**
 * 列出工作流
 */
export async function listWorkflows(selection: Selection): Promise<Workflow[]> {
  return requestJson<Workflow[]>(`/api/v1/workspaces/${selection.workspaceId}/workflows`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

/**
 * 创建工作流
 */
export async function createWorkflow(
  selection: Selection,
  workflowCode: string,
  name: string,
  description: string,
  graphDefinition: string
): Promise<Workflow> {
  return requestJson<Workflow>(`/api/v1/workspaces/${selection.workspaceId}/workflows`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
    bodyJson: { 
      tenantId: selection.tenantId, 
      workspaceId: selection.workspaceId, 
      workflowCode, 
      name, 
      description, 
      graphDefinition 
    },
  })
}

/**
 * 获取工作流详情
 */
export async function getWorkflow(selection: Selection, workflowId: string): Promise<Workflow> {
  return requestJson<Workflow>(`/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

/**
 * 更新工作流
 */
export async function updateWorkflow(
  selection: Selection,
  workflowId: string,
  name: string,
  description: string,
  graphDefinition: string
): Promise<Workflow> {
  return requestJson<Workflow>(`/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PUT',
    headers: buildHeaders(selection),
    bodyJson: { 
      tenantId: selection.tenantId, 
      workspaceId: selection.workspaceId, 
      name, 
      description, 
      graphDefinition 
    },
  })
}

/**
 * 删除工作流
 */
export async function deleteWorkflow(selection: Selection, workflowId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: buildHeaders(selection),
  })
}

/**
 * 发布工作流
 */
export async function publishWorkflow(selection: Selection, workflowId: string): Promise<Workflow> {
  return requestJson<Workflow>(`/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/publish`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}

/**
 * 取消发布工作流
 */
export async function unpublishWorkflow(selection: Selection, workflowId: string): Promise<Workflow> {
  return requestJson<Workflow>(`/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/unpublish`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}

// ==================== 工作流执行 ====================

/**
 * 执行工作流
 */
export async function executeWorkflow(
  selection: Selection,
  workflowId: string,
  input: Record<string, any>
): Promise<WorkflowExecution> {
  return requestJson<WorkflowExecution>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/execute`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
      bodyJson: { input }
    }
  )
}

/**
 * 获取执行结果
 */
export async function getExecutionResult(
  selection: Selection,
  workflowId: string,
  taskId: string
): Promise<WorkflowExecution> {
  return requestJson<WorkflowExecution>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/executions/${taskId}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: buildHeaders(selection),
    }
  )
}

/**
 * 停止执行
 */
export async function stopExecution(
  selection: Selection,
  workflowId: string,
  taskId: string
): Promise<void> {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/executions/${taskId}/stop`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
    }
  )
}

/**
 * 获取执行历史
 */
export async function getExecutionHistory(
  selection: Selection,
  workflowId: string,
  limit: number = 20
): Promise<WorkflowExecution[]> {
  return requestJson<WorkflowExecution[]>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/executions`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: buildHeaders(selection),
      query: { limit }
    }
  )
}

// ==================== SSE事件流 ====================

/**
 * 创建SSE连接，监听工作流执行事件
 */
export function createWorkflowEventStream(
  selection: Selection,
  workflowId: string,
  taskId: string,
  onEvent: (event: SSEEvent) => void,
  onError: (error: Error) => void
): EventSource {
  const url = `${runtimeConfig.agentApiBase}/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/executions/${taskId}/events`
  
  const eventSource = new EventSource(url, {
    withCredentials: true
  })

  eventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      onEvent(data)
    } catch (e) {
      console.error('Failed to parse SSE event:', e)
    }
  }

  eventSource.onerror = (error) => {
    onError(new Error('SSE connection error'))
    eventSource.close()
  }

  return eventSource
}

// ==================== 调试功能 ====================

/**
 * 调试会话接口
 */
export interface DebugSession {
  sessionId: string
  workflowId: string
  status: 'active' | 'completed' | 'error'
  currentNodeId?: string
  variables: Record<string, any>
  breakpoints: string[]
}

/**
 * 创建调试会话
 */
export async function createDebugSession(
  selection: Selection,
  workflowId: string,
  input: Record<string, any>
): Promise<DebugSession> {
  return requestJson<DebugSession>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/debug`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
      bodyJson: { input }
    }
  )
}

/**
 * 单步执行
 */
export async function stepDebug(
  selection: Selection,
  workflowId: string,
  sessionId: string
): Promise<NodeResult> {
  return requestJson<NodeResult>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/debug/${sessionId}/step`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
    }
  )
}

/**
 * 继续执行
 */
export async function continueDebug(
  selection: Selection,
  workflowId: string,
  sessionId: string
): Promise<WorkflowExecution> {
  return requestJson<WorkflowExecution>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/debug/${sessionId}/continue`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
    }
  )
}

/**
 * 设置断点
 */
export async function setBreakpoints(
  selection: Selection,
  workflowId: string,
  sessionId: string,
  nodeIds: string[]
): Promise<DebugSession> {
  return requestJson<DebugSession>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/debug/${sessionId}/breakpoints`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
      bodyJson: { nodeIds }
    }
  )
}

/**
 * 获取当前变量
 */
export async function getDebugVariables(
  selection: Selection,
  workflowId: string,
  sessionId: string
): Promise<Record<string, any>> {
  return requestJson<Record<string, any>>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/debug/${sessionId}/variables`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: buildHeaders(selection),
    }
  )
}

/**
 * 结束调试会话
 */
export async function endDebugSession(
  selection: Selection,
  workflowId: string,
  sessionId: string
): Promise<void> {
  return requestJson<void>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/debug/${sessionId}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'DELETE',
      headers: buildHeaders(selection),
    }
  )
}

// ==================== 验证功能 ====================

/**
 * 验证结果接口
 */
export interface ValidationResult {
  valid: boolean
  errors: ValidationError[]
  warnings: ValidationWarning[]
}

export interface ValidationError {
  nodeId?: string
  edgeId?: string
  message: string
  code: string
}

export interface ValidationWarning {
  nodeId?: string
  edgeId?: string
  message: string
  code: string
}

/**
 * 验证工作流图
 */
export async function validateWorkflow(
  selection: Selection,
  graph: WorkflowGraph
): Promise<ValidationResult> {
  return requestJson<ValidationResult>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/validate`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
      bodyJson: { graph }
    }
  )
}
