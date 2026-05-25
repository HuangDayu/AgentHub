import { defineStore } from 'pinia';
import { ref } from 'vue';
import { alertApi } from '@/api/alert';
import type { Alert } from '@/types/alert';

/**
 * Alert 状态管理
 */
export const useAlertStore = defineStore('alert', () => {
  const alerts = ref<Alert[]>([]);
  const loading = ref(false);

  /**
   * 创建 Alert
   */
  const createAlert = async (alert: Alert) => {
    const created = await alertApi.create(alert);
    alerts.value.push(created);
    return created;
  };

  /**
   * 加载所有 Alert
   */
  const loadAlerts = async () => {
    loading.value = true;
    try {
      alerts.value = await alertApi.list();
    } finally {
      loading.value = false;
    }
  };

  /**
   * 按 Run ID 加载 Alert
   */
  const loadAlertsByRun = async (runId: string) => {
    loading.value = true;
    try {
      alerts.value = await alertApi.listByRun(runId);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 加载未解决的 Alert
   */
  const loadUnresolvedAlerts = async () => {
    loading.value = true;
    try {
      alerts.value = await alertApi.listUnresolved();
    } finally {
      loading.value = false;
    }
  };

  /**
   * 解决 Alert
   */
  const resolveAlert = async (id: string, resolvedBy: string) => {
    const resolved = await alertApi.resolve(id, resolvedBy);
    const index = alerts.value.findIndex(a => a.id === id);
    if (index !== -1) {
      alerts.value[index] = resolved;
    }
    return resolved;
  };

  /**
   * 删除 Alert
   */
  const deleteAlert = async (id: string) => {
    await alertApi.delete(id);
    alerts.value = alerts.value.filter(a => a.id !== id);
  };

  /**
   * 获取未解决 Alert 数量
   */
  const getUnresolvedCount = () => {
    return alerts.value.filter(a => !a.resolved).length;
  };

  /**
   * 按级别分组
   */
  const groupByLevel = () => {
    const groups: Record<string, Alert[]> = {};
    alerts.value.forEach(alert => {
      if (!groups[alert.alertLevel]) {
        groups[alert.alertLevel] = [];
      }
      groups[alert.alertLevel].push(alert);
    });
    return groups;
  };

  return {
    alerts,
    loading,
    createAlert,
    loadAlerts,
    loadAlertsByRun,
    loadUnresolvedAlerts,
    resolveAlert,
    deleteAlert,
    getUnresolvedCount,
    groupByLevel,
  };
});
