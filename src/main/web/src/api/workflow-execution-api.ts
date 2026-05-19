/**
 * 增强的工作流API
 * 包含执行、调试、版本管理等功能
 */

import { runtimeConfig } from '@/common/runtime-config'
import type { 
  Workflow, 
  WorkflowGraph,
  WorkflowExecution,
  NodeResult
} from '@/types/workflow-node'
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

// ==================== 基础CRUD ====================

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

// ==================== 发布管理 ====================

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

// ==================== 版本管理 ====================

/**
 * 工作流版本
 */
export interface WorkflowVersion {
  versionId: string
  versionNumber: string
  description: string
  graphDefinition: string
  createdAt: string
  createdBy: string
  isPublished: boolean
}

/**
 * 获取工作流版本列表
 */
export async function listWorkflowVersions(
  selection: Selection, 
  workflowId: string
): Promise<WorkflowVersion[]> {
  return requestJson<WorkflowVersion[]>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/versions`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: buildHeaders(selection),
    }
  )
}

/**
 * 创建新版本
 */
export async function createWorkflowVersion(
  selection: Selection,
  workflowId: string,
  description: string
): Promise<WorkflowVersion> {
  return requestJson<WorkflowVersion>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/versions`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
      bodyJson: { description }
    }
  )
}

/**
 * 获取特定版本
 */
export async function getWorkflowVersion(
  selection: Selection,
  workflowId: string,
  versionId: string
): Promise<WorkflowVersion> {
  return requestJson<WorkflowVersion>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/versions/${versionId}`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'GET',
      headers: buildHeaders(selection),
    }
  )
}

/**
 * 回滚到指定版本
 */
export async function rollbackWorkflowVersion(
  selection: Selection,
  workflowId: string,
  versionId: string
): Promise<Workflow> {
  return requestJson<Workflow>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/versions/${versionId}/rollback`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
    }
  )
}

// ==================== 执行管理 ====================

/**
 * 执行请求
 */
export interface ExecutionRequest {
  workflowId: string
  input: Record<string, any>
  debug?: boolean
}

/**
 * 启动工作流执行
 */
export async function startExecution(
  selection: Selection,
  request: ExecutionRequest
): Promise<WorkflowExecution> {
  return requestJson<WorkflowExecution>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${request.workflowId}/execute`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
      bodyJson: {
        input: request.input,
        debug: request.debug || false
      }
    }
  )
}

/**
 * 获取执行状态
 */
export async function getExecutionStatus(
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
      bodyJson: { limit }
    }
  )
}

// ==================== 调试功能 ====================

/**
 * 调试会话
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
 * 获取调试会话状态
 */
export async function getDebugSession(
  selection: Selection,
  workflowId: string,
  sessionId: string
): Promise<DebugSession> {
  return requestJson<DebugSession>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/debug/${sessionId}`,
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

// ==================== 单节点测试 ====================

/**
 * 测试单个节点
 */
export async function testSingleNode(
  selection: Selection,
  workflowId: string,
  nodeId: string,
  input: Record<string, any>
): Promise<NodeResult> {
  return requestJson<NodeResult>(
    `/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/nodes/${nodeId}/test`,
    {
      baseUrl: runtimeConfig.agentApiBase,
      method: 'POST',
      headers: buildHeaders(selection),
      bodyJson: { input }
    }
  )
}
