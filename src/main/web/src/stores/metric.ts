import { defineStore } from 'pinia';
import { ref } from 'vue';
import { metricApi } from '@/api/metric';
import type { Metric } from '@/types/metric';

/**
 * Metric 状态管理
 */
export const useMetricStore = defineStore('metric', () => {
  const metrics = ref<Metric[]>([]);
  const loading = ref(false);

  /**
   * 创建 Metric
   */
  const createMetric = async (metric: Metric) => {
    const created = await metricApi.create(metric);
    metrics.value.push(created);
    return created;
  };

  /**
   * 加载所有 Metric
   */
  const loadMetrics = async () => {
    loading.value = true;
    try {
      metrics.value = await metricApi.list();
    } finally {
      loading.value = false;
    }
  };

  /**
   * 按 Run ID 加载 Metric
   */
  const loadMetricsByRun = async (runId: string) => {
    loading.value = true;
    try {
      metrics.value = await metricApi.listByRun(runId);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 按 Agent ID 加载 Metric
   */
  const loadMetricsByAgent = async (agentId: string) => {
    loading.value = true;
    try {
      metrics.value = await metricApi.listByAgent(agentId);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 按类型加载 Metric
   */
  const loadMetricsByType = async (metricType: string) => {
    loading.value = true;
    try {
      metrics.value = await metricApi.listByType(metricType);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 删除 Metric
   */
  const deleteMetric = async (id: string) => {
    await metricApi.delete(id);
    metrics.value = metrics.value.filter(m => m.id !== id);
  };

  /**
   * 计算总 Token
   */
  const calculateTotalTokens = () => {
    return metrics.value
      .filter(m => m.metricType === 'TOTAL_TOKENS')
      .reduce((sum, m) => sum + m.metricValue, 0);
  };

  /**
   * 计算平均延迟
   */
  const calculateAvgLatency = () => {
    const latencyMetrics = metrics.value.filter(m => m.metricType === 'LATENCY_NS');
    if (latencyMetrics.length === 0) return 0;
    const sum = latencyMetrics.reduce((s, m) => s + m.metricValue, 0);
    return sum / latencyMetrics.length / 1000000; // 转换为毫秒
  };

  return {
    metrics,
    loading,
    createMetric,
    loadMetrics,
    loadMetricsByRun,
    loadMetricsByAgent,
    loadMetricsByType,
    deleteMetric,
    calculateTotalTokens,
    calculateAvgLatency,
  };
});
