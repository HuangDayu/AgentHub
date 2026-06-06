import { runtimeConfig } from '@/common/runtime-config'
import { requestJson } from './http'
import type { AgentDataSourceDescriptor } from '@/types/agent-data-source'

export function listAgentDataSourceComponents() {
  return requestJson<AgentDataSourceDescriptor[]>('/api/v1/agent-data-source-components', {
    baseUrl: runtimeConfig.agentApiBase,
    method: 'GET',
  })
}
