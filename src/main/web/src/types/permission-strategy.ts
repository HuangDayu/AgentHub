// 权限策略类型 - 与后端 PermissionStrategyResponse 对齐

export interface PermissionStrategy {
  id: string
  tenantId: string
  workspaceId: string
  name: string
  description?: string
  allowedRoles: string[]
  allowedOperations: string[]
  protocolBlocklist: string[]
  dangerousSqlBlock: boolean
  requireApprovalFor: string[]
  tablePermissions: Record<string, string[]>
  rateLimitPerMinute: number
  rateLimitPerHour: number
  auditLogEnabled: boolean
  auditLogRetentionDays: number
  piiMaskingOnResult: boolean
  createdAt?: string
  updatedAt?: string
}
