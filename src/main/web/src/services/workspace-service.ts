import type { SelectionState } from '@/domain/types'

export function scopedHeaders(selection: SelectionState) {
  const headers: Record<string, string> = { ...buildScopeHeaders(selection), ...buildAuthHeader() }
  return headers
}

function buildScopeHeaders(selection: SelectionState): Record<string, string> {
  const headers: Record<string, string> = {}
  if (selection.tenantId) headers['X-Tenant-Id'] = selection.tenantId
  if (selection.workspaceId) headers['X-Workspace-Id'] = selection.workspaceId
  return headers
}

function buildAuthHeader(): Record<string, string> {
  try { const token = localStorage.getItem('agenthub_access_token'); if (token) return { 'Authorization': `Bearer ${token}` } } catch { /* no-op */ }
  return {}
}

export function chooseNextId(currentId: string, items: Array<{ id: string }>) {
  if (items.some((item) => item.id === currentId)) {
    return currentId
  }
  return items[0]?.id ?? ''
}
