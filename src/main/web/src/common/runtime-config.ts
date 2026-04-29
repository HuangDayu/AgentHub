function getFallbackBase(): string {
  if (typeof window !== 'undefined' && window.location && window.location.origin) {
    return window.location.origin
  }
  // Default fallback for development
  return 'http://localhost:8080'
}

const fallbackBase = getFallbackBase()

function getEnvBase(envVar: string | undefined): string {
  // If env var is set and non-empty, use it
  if (envVar && envVar.trim()) {
    return envVar.trim()
  }
  return fallbackBase
}

export const runtimeConfig = {
  // Admin APIs
  billingApiBase: getEnvBase(import.meta.env.VITE_BILLING_API_BASE),
  auditApiBase: getEnvBase(import.meta.env.VITE_AUDIT_API_BASE),
  connectorApiBase: getEnvBase(import.meta.env.VITE_CONNECTOR_API_BASE),
  policyApiBase: getEnvBase(import.meta.env.VITE_POLICY_API_BASE),
  toolApiBase: getEnvBase(import.meta.env.VITE_TOOL_API_BASE),
  platformApiBase: getEnvBase(import.meta.env.VITE_PLATFORM_API_BASE),
  tenantApiBase: getEnvBase(import.meta.env.VITE_TENANT_API_BASE),
  // Tenant APIs
  agentApiBase: getEnvBase(import.meta.env.VITE_AGENT_API_BASE),
  knowledgeApiBase: getEnvBase(import.meta.env.VITE_KNOWLEDGE_API_BASE),
  runtimeApiBase: getEnvBase(import.meta.env.VITE_RUNTIME_API_BASE),
  // User APIs
  retrievalApiBase: getEnvBase(import.meta.env.VITE_RETRIEVAL_API_BASE),
  userApiBase: getEnvBase(import.meta.env.VITE_USER_API_BASE),
}
