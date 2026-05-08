import type { ScheduledTask } from '../types/scheduled-task'
import { get, post, put, del } from './http'

export async function listScheduledTasks(workspaceId: string): Promise<ScheduledTask[]> {
  return get(`/api/v1/workspaces/${workspaceId}/scheduled-tasks`)
}

export async function createScheduledTask(
  workspaceId: string,
  task: {
    tenantId: string
    workspaceId: string
    taskCode: string
    name: string
    description: string
    taskType: string
    cronExpression: string
    executorConfig: string
    prompt: string
  }
): Promise<ScheduledTask> {
  return post(`/api/v1/workspaces/${workspaceId}/scheduled-tasks`, task)
}

export async function updateScheduledTask(
  workspaceId: string,
  taskId: string,
  task: {
    name: string
    description: string
    cronExpression: string
    executorConfig: string
    prompt: string
  }
): Promise<ScheduledTask> {
  return put(`/api/v1/workspaces/${workspaceId}/scheduled-tasks/${taskId}`, task)
}

export async function enableScheduledTask(workspaceId: string, taskId: string): Promise<ScheduledTask> {
  return post(`/api/v1/workspaces/${workspaceId}/scheduled-tasks/${taskId}/enable`)
}

export async function disableScheduledTask(workspaceId: string, taskId: string): Promise<ScheduledTask> {
  return post(`/api/v1/workspaces/${workspaceId}/scheduled-tasks/${taskId}/disable`)
}

export async function deleteScheduledTask(workspaceId: string, taskId: string): Promise<void> {
  return del(`/api/v1/workspaces/${workspaceId}/scheduled-tasks/${taskId}`)
}

export async function executeScheduledTask(workspaceId: string, taskId: string): Promise<void> {
  return post(`/api/v1/workspaces/${workspaceId}/scheduled-tasks/${taskId}/execute`)
}
