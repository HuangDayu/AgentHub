import { runtimeConfig } from '@/common/runtime-config'
import type { Workflow } from '@/types/memory'
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

export async function listWorkflows(selection: Selection): Promise<Workflow[]> {
  return requestJson<Workflow[]>(`/api/v1/workspaces/${selection.workspaceId}/workflows`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

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
    bodyJson: { tenantId: selection.tenantId, workspaceId: selection.workspaceId, workflowCode, name, description, graphDefinition },
  })
}

export async function getWorkflow(selection: Selection, workflowId: string): Promise<Workflow> {
  return requestJson<Workflow>(`/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

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
    bodyJson: { tenantId: selection.tenantId, workspaceId: selection.workspaceId, name, description, graphDefinition },
  })
}

export async function publishWorkflow(selection: Selection, workflowId: string): Promise<Workflow> {
  return requestJson<Workflow>(`/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/publish`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}

export async function unpublishWorkflow(selection: Selection, workflowId: string): Promise<Workflow> {
  return requestJson<Workflow>(`/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}/unpublish`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}

export async function deleteWorkflow(selection: Selection, workflowId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/workflows/${workflowId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: buildHeaders(selection),
  })
}
