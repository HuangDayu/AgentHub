import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'
import type { PermissionStrategy } from '@/types/permission-strategy'

const BASE = (w: string) => `/api/v1/workspaces/${w}/permission-strategies`

export function listPermissionStrategies(selection: SelectionState) {
  return requestJson<PermissionStrategy[]>(BASE(selection.workspaceId), {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
}

export function getPermissionStrategy(selection: SelectionState, id: string) {
  return requestJson<PermissionStrategy>(`${BASE(selection.workspaceId)}/${id}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
}

export function upsertPermissionStrategy(selection: SelectionState, data: Partial<PermissionStrategy>) {
  return requestJson<PermissionStrategy>(BASE(selection.workspaceId), {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PUT',
    headers: scopedHeaders(selection),
    bodyJson: data,
  })
}

export function deletePermissionStrategy(selection: SelectionState, id: string) {
  return requestJson<void>(`${BASE(selection.workspaceId)}/${id}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: scopedHeaders(selection),
  })
}
