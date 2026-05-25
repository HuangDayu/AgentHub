/**
 * Trace 类型定义
 */
export interface Trace {
  id?: string;
  traceId: string;
  runId?: string;

  rootSpanId?: string;
  spanCount?: number;

  startTimeUnixNano: string;
  endTimeUnixNano: string;
  durationNs: number;

  statusCode?: number;
  errorMessage?: string;

  totalTokens?: number;

  createdAt?: string;
}
