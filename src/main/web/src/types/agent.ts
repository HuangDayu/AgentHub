export interface Agent {
  id: string
  name: string
  description: string
  publishedVersionId?: string
  createdAt: string
  updatedAt: string
}

export interface AgentVersion {
  id: string
  promptTemplate: string
  retrievalStrategyId?: string
  toolStrategyId?: string
  modelStrategyId?: string
  guardrailStrategyId?: string
  published: boolean
  rolloutStrategy?: string
  canaryPercent?: number
  createdAt: string
  updatedAt: string
}
