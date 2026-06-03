import { runtimeConfig } from '@/common/runtime-config'
import type { Skill, SkillConfig, SkillFile } from '@/types/memory'
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

export async function searchSkills(selection: Selection, keyword: string): Promise<Skill[]> {
  return requestJson<Skill[]>(`/api/v1/workspaces/${selection.workspaceId}/skills/search`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
    query: { keyword },
  })
}

export async function createSkill(
  selection: Selection,
  skillCode: string,
  name: string,
  description: string,
  skillPath: string
): Promise<Skill> {
  return requestJson<Skill>(`/api/v1/workspaces/${selection.workspaceId}/skills`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, skillCode, name, description, skillPath },
  })
}

export async function createSkillFromUrl(
  selection: Selection,
  skillCode: string,
  name: string,
  description: string,
  zipUrl: string
): Promise<Skill> {
  return requestJson<Skill>(`/api/v1/workspaces/${selection.workspaceId}/skills/from-url`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, skillCode, name, description, zipUrl },
  })
}

export async function createSkillFromUpload(
  selection: Selection,
  skillCode: string,
  name: string,
  description: string,
  file: File
): Promise<Skill> {
  const formData = new FormData()
  formData.append('skillCode', skillCode)
  formData.append('name', name)
  formData.append('description', description)
  formData.append('file', file)
  const token = localStorage.getItem('agenthub_access_token')
  const headers: Record<string, string> = {
    'X-Tenant-Id': selection.tenantId,
    'X-Workspace-Id': selection.workspaceId,
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  const url = `${runtimeConfig.agentApiBase}/api/v1/workspaces/${selection.workspaceId}/skills/from-upload`
  const response = await fetch(url, { method: 'POST', headers, body: formData })
  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `上传失败：${response.status}`)
  }
  return response.json()
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
  skillPath: string
): Promise<Skill> {
  return requestJson<Skill>(`/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PUT',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, name, description, skillPath },
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

export async function syncAllSkills(selection: Selection): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/skills/sync-all`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}

export async function getSkillFiles(selection: Selection, skillId: string): Promise<SkillFile[]> {
  return requestJson<SkillFile[]>(`/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}/files`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function getSkillFileContent(selection: Selection, skillId: string, fileId: string): Promise<string> {
  const res = await requestJson<{ content: string }>(`/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}/files/${fileId}/content`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
  return res.content
}

export async function listSkillConfigs(selection: Selection): Promise<SkillConfig[]> {
  return requestJson<SkillConfig[]>(`/api/v1/workspaces/${selection.workspaceId}/skill-configs?tenantId=${selection.tenantId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: buildHeaders(selection),
  })
}

export async function createSkillConfig(
  selection: Selection,
  config: Partial<SkillConfig>
): Promise<SkillConfig> {
  return requestJson<SkillConfig>(`/api/v1/workspaces/${selection.workspaceId}/skill-configs`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, ...config },
  })
}

export async function updateSkillConfig(
  selection: Selection,
  configId: string,
  config: Partial<SkillConfig>
): Promise<SkillConfig> {
  return requestJson<SkillConfig>(`/api/v1/workspaces/${selection.workspaceId}/skill-configs/${configId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PUT',
    headers: buildHeaders(selection),
    bodyJson: { tenantId: selection.tenantId, ...config },
  })
}

export async function deleteSkillConfig(selection: Selection, configId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/skill-configs/${configId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: buildHeaders(selection),
  })
}

export async function syncSkillWithConfig(selection: Selection, configId: string): Promise<void> {
  return requestJson<void>(`/api/v1/workspaces/${selection.workspaceId}/skill-configs/${configId}/sync`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: buildHeaders(selection),
  })
}
