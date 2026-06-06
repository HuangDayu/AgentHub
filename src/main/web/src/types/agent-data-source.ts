// 数据源领域类型 - 与后端 AgentDataSourceResponse/DataSourceSchemaResponse 等对齐

export interface AgentDataSource {
  id: string
  tenantId: string
  workspaceId: string
  name: string
  description?: string
  protocol: string
  endpointUri: string
  propertiesJson?: string
  enabled: boolean
  status: string
  lastErrorMessage?: string
  lastCheckedAt?: string
  permissionPolicyId?: string
  schemaId?: string
  createdAt?: string
  updatedAt?: string
}

export interface AgentDataSourceField {
  name: string
  label: string
  type: string
  required: boolean
  description?: string
  defaultValue?: string
}

export interface AgentDataSourceDescriptor {
  protocol: string
  displayName: string
  description: string
  category: string
  fields: AgentDataSourceField[]
  syntaxHint?: string
  exampleUri?: string
}

export interface AgentDataSourceTestResult {
  success: boolean
  message: string
  elapsedMs: number
  testedAt: string
}

export interface AgentDataSourceInvokeResult {
  success: boolean
  result?: unknown
  errorMessage?: string
  elapsedMs: number
  executedAt: string
}

export interface DataSourceColumn {
  id: string
  name: string
  type: string
  nullable: boolean
  isPrimary: boolean
  defaultValue?: string
  description?: string
  isPii: boolean
  piiType?: string
  columnOrder: number
}

export interface DataSourceTable {
  id: string
  name: string
  displayName?: string
  description?: string
  allowedOperations: string[]
  sampleDataJson?: string
  tableOrder: number
  columns: DataSourceColumn[]
  relationships: TableRelationship[]
}

export interface TableRelationship {
  id: string
  sourceTableId: string
  targetTableId: string
  name: string
  type: string
  sourceColumn: string
  targetColumn: string
  description?: string
}

export interface DataSourceSchema {
  id: string
  dataSourceId: string
  displayName?: string
  description?: string
  introspected: boolean
  lastIntrospectedAt?: string
  tables: DataSourceTable[]
  createdAt?: string
  updatedAt?: string
}
