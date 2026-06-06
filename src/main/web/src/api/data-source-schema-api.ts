import { runtimeConfig } from '@/common/runtime-config'
import type { SelectionState } from '@/domain/types'
import { scopedHeaders } from '@/services/workspace-service'
import { requestJson } from './http'
import type { DataSourceSchema, DataSourceTable } from '@/types/agent-data-source'

const PREFIX = (w: string, id: string) => `/api/v1/workspaces/${w}/agent-data-sources/${id}/schema`

export function getSchema(selection: SelectionState, dataSourceId: string) {
  return requestJson<DataSourceSchema>(PREFIX(selection.workspaceId, dataSourceId), {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
    headers: scopedHeaders(selection),
  })
}

export function replaceSchema(selection: SelectionState, dataSourceId: string, data: Partial<DataSourceSchema>) {
  return requestJson<DataSourceSchema>(PREFIX(selection.workspaceId, dataSourceId), {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'PUT',
    headers: scopedHeaders(selection),
    bodyJson: data,
  })
}

export function introspectSchema(selection: SelectionState, dataSourceId: string) {
  return requestJson<DataSourceSchema>(`${PREFIX(selection.workspaceId, dataSourceId)}/introspect`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
  })
}

export function addTable(selection: SelectionState, dataSourceId: string, table: Partial<DataSourceTable>) {
  return requestJson<DataSourceSchema>(`${PREFIX(selection.workspaceId, dataSourceId)}/tables`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'POST',
    headers: scopedHeaders(selection),
    bodyJson: table,
  })
}

export function deleteTable(selection: SelectionState, dataSourceId: string, tableId: string) {
  return requestJson<void>(`${PREFIX(selection.workspaceId, dataSourceId)}/tables/${tableId}`, {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'DELETE',
    headers: scopedHeaders(selection),
  })
}
