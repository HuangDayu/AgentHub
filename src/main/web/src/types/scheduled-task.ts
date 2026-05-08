export interface ScheduledTask {
  id: string
  tenantId: string
  workspaceId: string
  taskCode: string
  name: string
  description: string
  taskType: string
  cronExpression: string
  executorConfig: string
  prompt: string
  enabled: boolean
  lastExecuteTime?: string
  nextExecuteTime?: string
  status: string
  createdAt: string
  updatedAt: string
}
