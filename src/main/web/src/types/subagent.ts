/** 子智能体与子会话类型定义 */

export interface Subagent {
  id: string
  tenantId: string
  workspaceId: string
  parentAgentId: string
  parentSubagentId?: string
  name: string
  description: string
  systemPrompt: string
  modelConfigId: string
  status: 'ACTIVE' | 'INACTIVE' | 'RUNNING'
  createdAt: string
  updatedAt: string
}

export interface Subsession {
  id: string
  parentSessionId: string
  subagentId: string
  name: string
  status: 'ACTIVE' | 'CLOSED'
  createdAt: string
  updatedAt: string
}
