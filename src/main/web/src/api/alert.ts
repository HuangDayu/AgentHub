import request from '@/utils/request';
import type { Alert } from '@/types/alert';

/**
 * Alert API
 */
export const alertApi = {
  /**
   * 创建 Alert
   */
  create: (alert: Alert): Promise<Alert> =>
    request.post('/api/v1/alerts', alert),

  /**
   * 获取 Alert 详情
   */
  get: (id: string): Promise<Alert> =>
    request.get(`/api/v1/alerts/${id}`),

  /**
   * 解决 Alert
   */
  resolve: (id: string, resolvedBy: string): Promise<Alert> =>
    request.put(`/api/v1/alerts/${id}/resolve?resolvedBy=${resolvedBy}`),

  /**
   * 按 Run ID 查询
   */
  listByRun: (runId: string): Promise<Alert[]> =>
    request.get(`/api/v1/alerts/runs/${runId}`),

  /**
   * 查询未解决的 Alert
   */
  listUnresolved: (): Promise<Alert[]> =>
    request.get('/api/v1/alerts/unresolved'),

  /**
   * 查询所有
   */
  list: (): Promise<Alert[]> =>
    request.get('/api/v1/alerts'),

  /**
   * 删除
   */
  delete: (id: string): Promise<void> =>
    request.delete(`/api/v1/alerts/${id}`),
};
