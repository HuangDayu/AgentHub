/**
 * Span 类型定义
 */
export interface Span {
  id: string;
  spanId: string;
  traceId: string;
  parentSpanId?: string;
  name: string;
  kind: string;

  startTimeUnixNano: string;
  endTimeUnixNano: string;
  latencyNs: number;

  attributes?: Record<string, any>;
  events?: SpanEvent[];

  statusCode?: number;
  statusMessage?: string;

  model?: string;
  inputTokens?: number;
  outputTokens?: number;
  totalTokens?: number;

  runId?: string;
  agentId?: string;

  createdAt: string;
}

/**
 * Span 事件
 */
export interface SpanEvent {
  name: string;
  timeUnixNano: string;
  attributes?: Record<string, any>;
}

/**
 * Span 查询参数
 */
export interface SpanQueryParams {
  traceId?: string;
  runId?: string;
  agentId?: string;
  model?: string;
  statusCode?: number;
}
