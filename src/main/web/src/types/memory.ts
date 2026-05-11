export interface Memory {
  id: string
  tenantId: string
  workspaceId: string
  agentId: string
  memoryType: string
  content: string
  metadata: string
  importance: number
  expiresAt?: string
  createdAt: string
  updatedAt: string
}

export interface Skill {
  id: string
  tenantId: string
  workspaceId: string
  skillCode: string
  name: string
  description: string
  skillType: string
  definition: string
  parameters: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface Workflow {
  id: string
  tenantId: string
  workspaceId: string
  workflowCode: string
  name: string
  description: string
  graphDefinition: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface AgentTeam {
  id: string
  tenantId: string
  workspaceId: string
  teamCode: string
  name: string
  description: string
  coordinationMode: string
  memberConfig: string
  status: string
  createdAt: string
  updatedAt: string
}
