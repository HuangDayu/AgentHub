// ── Admin Domain Types ─────────────────────────────────

export interface PlatformStats {
  activeTenants: number
  activeWorkspaces: number
  totalKnowledgeBases: number
  totalAgents: number
  totalBillingCents: number
  totalAuditEvents: number
  currency: string
}

export interface TenantInfo {
  id: string
  name: string
  code: string
}

export interface Usage {
  tenantId: string
  totalTokens: number
  currentCostCents: number
  currency: string
}

export interface Invoice {
  id: string
  tenantId: string
  periodStart: string
  periodEnd: string
  amountCents: number
  status: string
  currency: string
}

export interface Budget {
  tenantId: string
  limitCents: number
  currency: string
}

export interface AuditEvent {
  eventId: string
  occurredAt: string
  actorId: string
  action: string
  resourceType: string
  resourceId: string
  outcome: string
}

export interface AuditExportJob {
  jobId: string
  status: string
  createdAt: string
  format: string
  requestedBy: string
  downloadUrl: string | null
}

export interface Connector {
  id: string
  name: string
  type: string
  enabled: boolean
  config: Record<string, string>
  createdAt: string
  updatedAt: string
  lastSyncedAt: string | null
}

export interface SyncJob {
  id: string
  connectorId: string
  status: string
  requestedAt: string
  finishedAt: string | null
}

export interface Policy {
  id: string
  name: string
  description: string
  rules: Record<string, unknown>
  createdAt: string
  updatedAt: string
}

export interface Tool {
  id: string
  name: string
  description: string
  enabled: boolean
  parameters: Record<string, unknown>
  createdAt: string
  updatedAt: string
}

export interface ToolInvocationResult {
  toolId: string
  output: unknown
  invokedAt: string
}

// ── Tenant Management ──────────────────────────────────

export interface Tenant {
  id: string
  tenantCode: string
  name: string
  planCode: string
  region: string
  createdAt: string
  updatedAt: string
}

export interface Workspace {
  id: string
  tenantId: string
  name: string
  createdAt: string
}

export interface Member {
  id: string
  workspaceId: string
  userId: string
  roleCode: string
  scopeType: string
  createdAt: string
}

export interface SelectionState {
  tenantId: string
  workspaceId: string
}

// ── Knowledge Base ─────────────────────────────────────

export interface KnowledgeBase {
  id: string
  name: string
  description: string
  indexVersions: string[]
  activeIndexVersion: string
  vectorStoreConfigId?: string
  embeddingModelConfigId?: string
  chatModelConfigId?: string
  createdAt: string
  updatedAt: string
}

export interface Document {
  docId: string
  kbId: string
  fileName: string
  contentType: string
  size: number
  status: string
  createdAt: string
}

// ── Vector Store Config ─────────────────────────────────

export interface VectorStoreConfig {
  id: string
  name: string
  type: string
  host: string
  port: number
  apiKey?: string
  collectionName: string
  extraParams?: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

// ── Model Config ────────────────────────────────────────

export interface ModelConfig {
  id: string
  name: string
  type: string
  supplier: string
  apiKey?: string
  baseUrl?: string
  model: string
  enabled: boolean
  createdAt: string
  updatedAt: string
  createdBy?: string
}

// ── Agent ──────────────────────────────────────────────

export interface Agent {
  id: string
  name: string
  description: string
  publishedVersionId: string | null
  chatModelConfigId?: string
  embeddingModelConfigId?: string
  vectorStoreConfigId?: string
  createdAt: string
}

export interface AgentVersion {
  id: string
  agentId: string
  configuration: string
  published: boolean
  createdAt: string
}

// ── Retrieval ──────────────────────────────────────────

export interface RetrievalChunk {
  docId: string
  chunkIndex: number
  content: string
  score: number
}

export interface RetrievalResponse {
  chunks: RetrievalChunk[]
}

// ── Runtime Chat (tenant) ─────────────────────────────

export interface ChatSession {
  sessionId: string
  agentId: string
  name?: string
  createdAt: string
}

// ── Stream Message Types ───────────────────────────────

export type MessageRole = 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL'
export type MessageType = 'ASSISTANT' | 'USER' | 'TOOL' | 'SKILL' | 'SYSTEM'

export interface ToolCall {
  id: string
  type: 'function'
  name: string
  arguments: string
}

export interface ToolResponse {
  id: string
  name: string
  responseData: string
}

export interface StreamMessage {
  messageType: MessageType
  text?: string
  toolCalls?: ToolCall[]
  responses?: ToolResponse[]
  metadata?: {
    role?: string
    finishReason?: string
    [key: string]: unknown
  }
}

export interface ChatMessage {
  messageId: string
  sessionId: string
  role: MessageRole
  content: string
  createdAt: string
  // 扩展字段用于流式消息
  messageType?: MessageType
  toolCalls?: ToolCall[]
  toolResponses?: ToolResponse[]
  isExpanded?: boolean // 用于工具调用展开/折叠
}

// ── User Console Types ─────────────────────────────────

export interface UserInfo {
  id: string
  username: string
  email?: string
  displayName?: string
  createdAt?: string
}

export interface UserAgent {
  id: string
  name: string
  description?: string
}

export interface UserSession {
  id: string
  agentId: string
  agentName: string
  title: string
  createdAt: string
  updatedAt: string
}

export interface UserMessage {
  id: string
  sessionId: string
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt: string
}

export interface Notification {
  id: string
  title: string
  content: string
  channel?: string
  status: 'unread' | 'read'
  createdAt: string
}

export interface UserKnowledgeBase {
  id: string
  name: string
  description?: string
}

export interface RetrievalResult {
  id: string
  content: string
  score: number
  source?: string
  documentName?: string
  metadata?: Record<string, unknown>
}

export interface UserRetrievalResponse {
  results: RetrievalResult[]
  query: string
  totalResults: number
}

// ── Strategy Types ──────────────────────────────────────

export interface RetrievalStrategy {
  id: string
  name: string
  description?: string
  retrievalType?: string
  topK: number
  similarityThreshold: number
  rerankEnabled: boolean
  rerankModel?: string
  vectorWeight: number
  keywordWeight: number
  createdAt: string
  updatedAt: string
}

export interface ModelStrategy {
  id: string
  name: string
  description?: string
  chatModelConfigId?: string
  embeddingModelConfigId?: string
  temperature: number
  maxTokens: number
  topP: number
  frequencyPenalty: number
  presencePenalty: number
  createdAt: string
  updatedAt: string
}

export interface ToolStrategy {
  id: string
  name: string
  description?: string
  maxConcurrentCalls: number
  timeoutSeconds: number
  retryCount: number
  fallbackEnabled: boolean
  createdAt: string
  updatedAt: string
}

export interface GuardrailStrategy {
  id: string
  name: string
  description?: string
  inputValidationEnabled: boolean
  outputValidationEnabled: boolean
  piiDetectionEnabled: boolean
  piiMaskingEnabled: boolean
  promptInjectionDetection: boolean
  maxInputLength: number
  maxOutputLength: number
  createdAt: string
  updatedAt: string
}

export type StrategyType = 'retrieval' | 'model' | 'tool' | 'guardrail'
