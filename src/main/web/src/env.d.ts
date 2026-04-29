declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

interface ImportMetaEnv {
  readonly VITE_BILLING_API_BASE?: string
  readonly VITE_AUDIT_API_BASE?: string
  readonly VITE_CONNECTOR_API_BASE?: string
  readonly VITE_POLICY_API_BASE?: string
  readonly VITE_TOOL_API_BASE?: string
  readonly VITE_PLATFORM_API_BASE?: string
  readonly VITE_TENANT_API_BASE?: string
  readonly VITE_AGENT_API_BASE?: string
  readonly VITE_KNOWLEDGE_API_BASE?: string
  readonly VITE_RETRIEVAL_API_BASE?: string
  readonly VITE_USER_API_BASE?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
