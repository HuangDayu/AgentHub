import type { SelectionState } from '@/domain/types'

export function scopedHeaders(selection: SelectionState) {
  const headers: Record<string, string> = {}
  if (selection.tenantId) {
    headers['X-Tenant-Id'] = selection.tenantId
  }
  if (selection.workspaceId) {
    headers['X-Workspace-Id'] = selection.workspaceId
  }
  try {
    const token = localStorage.getItem('tenant_console_access_token')
    if (token) headers['Authorization'] = `Bearer ${token}`
  } catch { /* no-op */ }
  return headers
}

export function chooseNextId(currentId: string, items: Array<{ id: string }>) {
  if (items.some((item) => item.id === currentId)) {
    return currentId
  }
  return items[0]?.id ?? ''
}
