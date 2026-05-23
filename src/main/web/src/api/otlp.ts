import axios from 'axios';

const API_BASE_URL = '/api/v1/otlp';

export interface OtlpSpan {
  id: string;
  spanId: string;
  traceId: string;
  parentSpanId?: string;
  operationName: string;
  serviceName: string;
  kind: string;
  startTimestamp: number;
  endTimestamp: number;
  duration: number;
  status: string;
  statusDescription?: string;
  attributes?: string;
  events?: string;
  links?: string;
  createdAt: string;
}

export interface OtlpMetric {
  id: string;
  metricName: string;
  description?: string;
  unit?: string;
  metricType: string;
  serviceName: string;
  value: string;
  attributes?: string;
  timestamp: number;
  createdAt: string;
}

export interface OtlpLog {
  id: string;
  logId: string;
  traceId?: string;
  spanId?: string;
  serviceName: string;
  severity: string;
  severityNumber?: number;
  body: string;
  attributes?: string;
  timestamp: number;
  createdAt: string;
}

export interface OtlpStatistics {
  totalSpans: number;
  totalMetrics: number;
  totalLogs: number;
}

/**
 * 查询最近的Span数据
 */
export async function querySpans(limit: number = 100): Promise<OtlpSpan[]> {
  const response = await axios.get(`${API_BASE_URL}/spans`, {
    params: { limit }
  });
  return response.data;
}

/**
 * 根据TraceId查询Span数据
 */
export async function queryTraceById(traceId: string): Promise<OtlpSpan[]> {
  const response = await axios.get(`${API_BASE_URL}/traces/${traceId}`);
  return response.data;
}

/**
 * 查询最近的Metric数据
 */
export async function queryMetrics(limit: number = 100): Promise<OtlpMetric[]> {
  const response = await axios.get(`${API_BASE_URL}/metrics/query`, {
    params: { limit }
  });
  return response.data;
}

/**
 * 查询最近的Log数据
 */
export async function queryLogs(limit: number = 100): Promise<OtlpLog[]> {
  const response = await axios.get(`${API_BASE_URL}/logs/query`, {
    params: { limit }
  });
  return response.data;
}

/**
 * 获取统计信息
 */
export async function getStatistics(): Promise<OtlpStatistics> {
  const response = await axios.get(`${API_BASE_URL}/statistics`);
  return response.data;
}
