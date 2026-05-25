import request from '@/utils/request';
import type { Span } from '@/types/span';

/**
 * Span API
 */
export const spanApi = {
  /**
   * 获取 Span 详情
   */
  get: (spanId: string): Promise<Span> =>
    request.get(`/api/v1/spans/${spanId}`),

  /**
   * 按 Trace ID 查询 Span 列表
   */
  listByTrace: (traceId: string): Promise<Span[]> =>
    request.get(`/api/v1/spans/traces/${traceId}`),

  /**
   * 按 Run ID 查询 Span 列表
   */
  listByRun: (runId: string): Promise<Span[]> =>
    request.get(`/api/v1/spans/runs/${runId}`),

  /**
   * 查询所有 Span
   */
  list: (): Promise<Span[]> =>
    request.get('/api/v1/spans'),

  /**
   * 删除 Span
   */
  delete: (spanId: string): Promise<void> =>
    request.delete(`/api/v1/spans/${spanId}`),
};
