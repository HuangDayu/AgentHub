import request from '@/utils/request';
import type { Metric } from '@/types/metric';

/**
 * Metric API
 */
export const metricApi = {
  /**
   * 创建 Metric
   */
  create: (metric: Metric): Promise<Metric> =>
    request.post('/api/v1/metrics', metric),

  /**
   * 按 Run ID 查询
   */
  listByRun: (runId: string): Promise<Metric[]> =>
    request.get(`/api/v1/metrics/runs/${runId}`),

  /**
   * 按 Agent ID 查询
   */
  listByAgent: (agentId: string): Promise<Metric[]> =>
    request.get(`/api/v1/metrics/agents/${agentId}`),

  /**
   * 按类型查询
   */
  listByType: (metricType: string): Promise<Metric[]> =>
    request.get(`/api/v1/metrics/types/${metricType}`),

  /**
   * 查询所有
   */
  list: (): Promise<Metric[]> =>
    request.get('/api/v1/metrics'),

  /**
   * 删除
   */
  delete: (id: string): Promise<void> =>
    request.delete(`/api/v1/metrics/${id}`),
};
