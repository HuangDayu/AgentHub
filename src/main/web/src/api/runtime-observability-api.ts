import { runtimeConfig } from '@/common/runtime-config'
import type { Alert } from '@/types/alert'
import type { Metric } from '@/types/metric'
import type { Span } from '@/types/span'
import { requestJson } from './http'

export interface OtlpStatistics {
  totalSpans: number
  totalMetrics: number
  totalLogs: number
}

export interface OtlpLog {
  logId?: string
  traceId?: string
  spanId?: string
  serviceName?: string
  severity?: string
  body?: string
  attributes?: string
  timestamp?: number
  createdAt?: string
}

export interface RuntimeObservabilityData {
  spans: Span[]
  metrics: Metric[]
  alerts: Alert[]
  logs: OtlpLog[]
  statistics: OtlpStatistics
}

const emptyStatistics: OtlpStatistics = {
  totalSpans: 0,
  totalMetrics: 0,
  totalLogs: 0,
}

function emptyData(): RuntimeObservabilityData {
  return {
    spans: [],
    metrics: [],
    alerts: [],
    logs: [],
    statistics: { ...emptyStatistics },
  }
}

async function readOrEmpty<T>(request: Promise<T>, fallback: T): Promise<T> {
  try {
    return await request
  } catch {
    return fallback
  }
}

function get<T>(path: string) {
  return requestJson<T>(path, {
    baseUrl: runtimeConfig.runtimeApiBase,
    method: 'GET',
  })
}

export async function loadRuntimeObservability(agentId?: string, runId?: string) {
  if (!agentId && !runId) {
    return emptyData()
  }

  const spans = runId ? get<Span[]>(`/api/v1/spans/runs/${runId}`) : Promise.resolve([])
  const alerts = runId ? get<Alert[]>(`/api/v1/alerts/runs/${runId}`) : Promise.resolve([])
  const metrics = agentId ? get<Metric[]>(`/api/v1/metrics/agents/${agentId}`) : Promise.resolve([])

  const [safeSpans, safeMetrics, safeAlerts, logs, statistics] = await Promise.all([
    readOrEmpty(spans, []),
    readOrEmpty(metrics, []),
    readOrEmpty(alerts, []),
    readOrEmpty(get<OtlpLog[]>('/api/v1/otlp/logs/query?limit=20'), []),
    readOrEmpty(get<OtlpStatistics>('/api/v1/otlp/statistics'), emptyStatistics),
  ])

  return {
    spans: safeSpans,
    metrics: filterMetricsByRun(safeMetrics, runId),
    alerts: safeAlerts,
    logs,
    statistics,
  }
}

function filterMetricsByRun(metrics: Metric[], runId?: string) {
  if (!runId) {
    return metrics
  }
  return metrics.filter((metric) => !metric.runId || metric.runId === runId)
}
