<template>
  <div class="otlp-runtime-panel">
    <!-- 统计概览 -->
    <div class="stats-overview">
      <div class="stat-card">
        <div class="stat-icon traces">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalSpans }}</div>
          <div class="stat-label">追踪数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon metrics">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 3v18h18"/>
            <path d="M18 17V9"/>
            <path d="M13 17V5"/>
            <path d="M8 17v-3"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalMetrics }}</div>
          <div class="stat-label">指标数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon logs">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalLogs }}</div>
          <div class="stat-label">日志数</div>
        </div>
      </div>
    </div>

    <!-- 标签页切换 -->
    <div class="tabs">
      <button 
        :class="['tab', { active: activeTab === 'traces' }]"
        @click="activeTab = 'traces'"
      >
        追踪
      </button>
      <button 
        :class="['tab', { active: activeTab === 'metrics' }]"
        @click="activeTab = 'metrics'"
      >
        指标
      </button>
      <button 
        :class="['tab', { active: activeTab === 'logs' }]"
        @click="activeTab = 'logs'"
      >
        日志
      </button>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 追踪列表 -->
      <div v-if="activeTab === 'traces'" class="traces-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="spans.length === 0" class="empty">暂无追踪数据</div>
        <div v-else class="data-list">
          <div v-for="span in spans" :key="span.id" class="data-item trace-item">
            <div class="item-header">
              <span class="operation-name">{{ span.operationName }}</span>
              <span :class="['status-badge', span.status.toLowerCase()]">{{ span.status }}</span>
            </div>
            <div class="item-details">
              <div class="detail-row">
                <span class="label">服务:</span>
                <span class="value">{{ span.serviceName }}</span>
              </div>
              <div class="detail-row">
                <span class="label">Trace ID:</span>
                <span class="value mono">{{ span.traceId }}</span>
              </div>
              <div class="detail-row">
                <span class="label">Span ID:</span>
                <span class="value mono">{{ span.spanId }}</span>
              </div>
              <div class="detail-row">
                <span class="label">耗时:</span>
                <span class="value">{{ formatDuration(span.duration) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 指标列表 -->
      <div v-if="activeTab === 'metrics'" class="metrics-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="metrics.length === 0" class="empty">暂无指标数据</div>
        <div v-else class="data-list">
          <div v-for="metric in metrics" :key="metric.id" class="data-item metric-item">
            <div class="item-header">
              <span class="metric-name">{{ metric.metricName }}</span>
              <span class="metric-type">{{ metric.metricType }}</span>
            </div>
            <div class="item-details">
              <div class="detail-row">
                <span class="label">服务:</span>
                <span class="value">{{ metric.serviceName }}</span>
              </div>
              <div class="detail-row">
                <span class="label">值:</span>
                <span class="value">{{ metric.value }}</span>
              </div>
              <div v-if="metric.unit" class="detail-row">
                <span class="label">单位:</span>
                <span class="value">{{ metric.unit }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 日志列表 -->
      <div v-if="activeTab === 'logs'" class="logs-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="logs.length === 0" class="empty">暂无日志数据</div>
        <div v-else class="data-list">
          <div v-for="log in logs" :key="log.id" class="data-item log-item">
            <div class="item-header">
              <span :class="['severity-badge', log.severity.toLowerCase()]">{{ log.severity }}</span>
              <span class="service-name">{{ log.serviceName }}</span>
            </div>
            <div class="log-body">{{ log.body }}</div>
            <div class="item-details">
              <div v-if="log.traceId" class="detail-row">
                <span class="label">Trace ID:</span>
                <span class="value mono">{{ log.traceId }}</span>
              </div>
              <div class="detail-row">
                <span class="label">时间:</span>
                <span class="value">{{ formatTimestamp(log.timestamp) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 刷新按钮 -->
    <button class="refresh-btn" @click="refreshData" :disabled="loading">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M23 4v6h-6"/>
        <path d="M1 20v-6h6"/>
        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
      </svg>
      刷新
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import {
  querySpans,
  queryMetrics,
  queryLogs,
  getStatistics,
  type OtlpSpan,
  type OtlpMetric,
  type OtlpLog,
  type OtlpStatistics
} from '../api/otlp';

const activeTab = ref<'traces' | 'metrics' | 'logs'>('traces');
const loading = ref(false);
const statistics = ref<OtlpStatistics>({
  totalSpans: 0,
  totalMetrics: 0,
  totalLogs: 0
});
const spans = ref<OtlpSpan[]>([]);
const metrics = ref<OtlpMetric[]>([]);
const logs = ref<OtlpLog[]>([]);

const loadStatistics = async () => {
  try {
    statistics.value = await getStatistics();
  } catch (error) {
    console.error('Failed to load statistics:', error);
  }
};

const loadSpans = async () => {
  try {
    spans.value = await querySpans(50);
  } catch (error) {
    console.error('Failed to load spans:', error);
  }
};

const loadMetrics = async () => {
  try {
    metrics.value = await queryMetrics(50);
  } catch (error) {
    console.error('Failed to load metrics:', error);
  }
};

const loadLogs = async () => {
  try {
    logs.value = await queryLogs(50);
  } catch (error) {
    console.error('Failed to load logs:', error);
  }
};

const refreshData = async () => {
  loading.value = true;
  try {
    await loadStatistics();
    if (activeTab.value === 'traces') {
      await loadSpans();
    } else if (activeTab.value === 'metrics') {
      await loadMetrics();
    } else if (activeTab.value === 'logs') {
      await loadLogs();
    }
  } finally {
    loading.value = false;
  }
};

const formatDuration = (nanoseconds: number): string => {
  if (nanoseconds < 1000) {
    return `${nanoseconds}ns`;
  } else if (nanoseconds < 1000000) {
    return `${(nanoseconds / 1000).toFixed(2)}μs`;
  } else if (nanoseconds < 1000000000) {
    return `${(nanoseconds / 1000000).toFixed(2)}ms`;
  } else {
    return `${(nanoseconds / 1000000000).toFixed(2)}s`;
  }
};

const formatTimestamp = (nanoseconds: number): string => {
  const milliseconds = nanoseconds / 1000000;
  return new Date(milliseconds).toLocaleString('zh-CN');
};

onMounted(() => {
  refreshData();
  // 每30秒自动刷新
  setInterval(refreshData, 30000);
});
</script>

<style scoped>
.otlp-runtime-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  height: 100%;
  overflow: hidden;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.stat-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.stat-icon.traces {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.stat-icon.metrics {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.stat-icon.logs {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.tabs {
  display: flex;
  gap: 8px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 8px;
}

.tab {
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.tab:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.tab.active {
  background: var(--primary-color);
  color: white;
}

.content-area {
  flex: 1;
  overflow-y: auto;
}

.loading,
.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--text-secondary);
}

.data-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.data-item {
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.operation-name,
.metric-name {
  font-weight: 600;
  color: var(--text-primary);
}

.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.ok {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.status-badge.error {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.status-badge.unset {
  background: rgba(107, 114, 128, 0.1);
  color: #6b7280;
}

.metric-type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.severity-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.severity-badge.error,
.severity-badge.fatal {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.severity-badge.warn {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.severity-badge.info {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.severity-badge.debug,
.severity-badge.trace {
  background: rgba(107, 114, 128, 0.1);
  color: #6b7280;
}

.item-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-row {
  display: flex;
  gap: 8px;
  font-size: 13px;
}

.label {
  color: var(--text-secondary);
  min-width: 60px;
}

.value {
  color: var(--text-primary);
}

.value.mono {
  font-family: monospace;
  font-size: 12px;
}

.log-body {
  margin: 8px 0;
  padding: 8px;
  background: var(--bg-primary);
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-all;
}

.refresh-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.refresh-btn:hover:not(:disabled) {
  background: var(--primary-color-dark);
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.refresh-btn svg {
  width: 16px;
  height: 16px;
}
</style>
