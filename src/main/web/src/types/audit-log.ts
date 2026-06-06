// 审计日志类型 - 与后端 AuditEventResponse 对齐

export interface AuditEvent {
  id: string
  tenantId: string
  workspaceId: string
  actorId: string
  actorType: string
  agentId?: string
  sessionId?: string
  resourceType: string
  resourceId: string
  resourceName?: string
  action: string
  status: string
  request?: unknown
  response?: unknown
  errorMessage?: string
  metadata?: Record<string, unknown>
  elapsedMs?: number
  createdAt?: string
}

export interface AuditLogPage {
  items: AuditEvent[]
  total: number
  page: number
  size: number
}

export interface AuditLogQuery {
  workspaceId?: string
  resourceType?: string
  resourceId?: string
  actorId?: string
  action?: string
  status?: string
  from?: string
  to?: string
  page?: number
  size?: number
}
