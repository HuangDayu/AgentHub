import { runtimeConfig } from '@/common/runtime-config'
import type { Skill } from '@/types/memory'
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

export async function listSkills(selection: Selection): Promise<Skill[]> {
  return requestJson<Skill[]>(`/api/v1/workspaces/${selection.workspaceId}/skills`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function createSkill(
  selection: Selection,
  skillCode: string,
  name: string,
  description: string,
  skillType: string,
  definition: string,
  parameters: string
): Promise<Skill> {
  return requestJson<Skill>(`/api/v1/workspaces/${selection.workspaceId}/skills`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, workspaceId: selection.workspaceId, skillCode, name, description, skillType, definition, parameters },
  })
}

export async function getSkill(selection: Selection, skillId: string): Promise<Skill> {
  return requestJson<Skill>(`/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function updateSkill(
  selection: Selection,
  skillId: string,
  name: string,
  description: string,
  definition: string,
  parameters: string
): Promise<Skill> {
  return requestJson<Skill>(`/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PUT',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, workspaceId: selection.workspaceId, name, description, definition, parameters },
  })
}

export async function enableSkill(selection: Selection, skillId: string): Promise<Skill> {
  return requestJson<Skill>(`/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}/enable`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}

export async function disableSkill(selection: Selection, skillId: string): Promise<Skill> {
  return requestJson<Skill>(`/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}/disable`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}

export async function deleteSkill(selection: Selection, skillId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: buildHeaders(selection),
  })
}
