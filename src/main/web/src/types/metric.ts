/**
 * Metric 类型定义
 */
export interface Metric {
  id?: string;
  metricType: string;
  metricName: string;
  metricValue: number;

  runId?: string;
  agentId?: string;
  traceId?: string;
  spanId?: string;

  labels?: Record<string, any>;

  timestamp?: string;
  createdAt?: string;
}

/**
 * Metric 类型枚举
 */
export enum MetricType {
  LATENCY_NS = 'LATENCY_NS',
  THROUGHPUT = 'THROUGHPUT',
  INPUT_TOKENS = 'INPUT_TOKENS',
  OUTPUT_TOKENS = 'OUTPUT_TOKENS',
  TOTAL_TOKENS = 'TOTAL_TOKENS',
  CPU_USAGE = 'CPU_USAGE',
  MEMORY_USAGE = 'MEMORY_USAGE',
  ERROR_COUNT = 'ERROR_COUNT',
}
