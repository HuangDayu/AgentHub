import { runtimeConfig } from '@/common/runtime-config'
import type { AgentTeam } from '@/types/memory'
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

export async function listTeams(selection: Selection): Promise<AgentTeam[]> {
  return requestJson<AgentTeam[]>(`/api/v1/workspaces/${selection.workspaceId}/teams`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function createTeam(
  selection: Selection,
  teamCode: string,
  name: string,
  description: string,
  coordinationMode: string,
  memberConfig: string
): Promise<AgentTeam> {
  return requestJson<AgentTeam>(`/api/v1/workspaces/${selection.workspaceId}/teams`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, workspaceId: selection.workspaceId, teamCode, name, description, coordinationMode, memberConfig },
  })
}

export async function getTeam(selection: Selection, teamId: string): Promise<AgentTeam> {
  return requestJson<AgentTeam>(`/api/v1/workspaces/${selection.workspaceId}/teams/${teamId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function updateTeam(
  selection: Selection,
  teamId: string,
  name: string,
  description: string,
  coordinationMode: string,
  memberConfig: string
): Promise<AgentTeam> {
  return requestJson<AgentTeam>(`/api/v1/workspaces/${selection.workspaceId}/teams/${teamId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PUT',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, workspaceId: selection.workspaceId, name, description, coordinationMode, memberConfig },
  })
}

export async function activateTeam(selection: Selection, teamId: string): Promise<AgentTeam> {
  return requestJson<AgentTeam>(`/api/v1/workspaces/${selection.workspaceId}/teams/${teamId}/activate`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}

export async function deactivateTeam(selection: Selection, teamId: string): Promise<AgentTeam> {
  return requestJson<AgentTeam>(`/api/v1/workspaces/${selection.workspaceId}/teams/${teamId}/deactivate`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}

export async function deleteTeam(selection: Selection, teamId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/teams/${teamId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: buildHeaders(selection),
  })
}
