<template>
  <div class="metric-dashboard">
    <el-row :gutter="20">
      <!-- 指标卡片 -->
      <el-col :span="6" v-for="card in metricCards" :key="card.type">
        <el-card class="metric-card">
          <div class="card-content">
            <div class="card-title">{{ card.title }}</div>
            <div class="card-value">{{ card.value }}</div>
            <div class="card-unit">{{ card.unit }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 指标列表 -->
    <el-card class="mt-4">
      <template #header>
        <div class="card-header">
          <span>监控指标</span>
          <el-select v-model="selectedType" @change="handleTypeChange">
            <el-option label="全部" value="" />
            <el-option
              v-for="type in metricTypes"
              :key="type"
              :label="type"
              :value="type"
            />
          </el-select>
        </div>
      </template>

      <el-table :data="metrics" v-loading="loading" stripe border>
        <el-table-column prop="metricName" label="指标名称" width="200" />
        <el-table-column prop="metricType" label="类型" width="150" />
        <el-table-column label="值" width="150">
          <template #default="{ row }">
            {{ formatValue(row.metricValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="runId" label="Run ID" width="200" />
        <el-table-column prop="agentId" label="Agent ID" width="200" />
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.timestamp) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { metricApi } from '@/api/metric';
import type { Metric } from '@/types/metric';

const metrics = ref<Metric[]>([]);
const loading = ref(false);
const selectedType = ref('');

const metricTypes = [
  'LATENCY_NS',
  'THROUGHPUT',
  'INPUT_TOKENS',
  'OUTPUT_TOKENS',
  'TOTAL_TOKENS',
  'CPU_USAGE',
  'MEMORY_USAGE',
];

const metricCards = computed(() => {
  return [
    {
      type: 'TOTAL_TOKENS',
      title: '总 Token',
      value: calculateTotal('TOTAL_TOKENS'),
      unit: 'tokens',
    },
    {
      type: 'LATENCY_NS',
      title: '平均延迟',
      value: calculateAverage('LATENCY_NS'),
      unit: 'ms',
    },
    {
      type: 'ERROR_COUNT',
      title: '错误数',
      value: calculateTotal('ERROR_COUNT'),
      unit: '次',
    },
    {
      type: 'THROUGHPUT',
      title: '吞吐量',
      value: calculateAverage('THROUGHPUT'),
      unit: 'req/s',
    },
  ];
});

const loadMetrics = async () => {
  loading.value = true;
  try {
    metrics.value = await metricApi.list();
  } finally {
    loading.value = false;
  }
};

const handleTypeChange = async () => {
  if (!selectedType.value) {
    await loadMetrics();
    return;
  }

  loading.value = true;
  try {
    metrics.value = await metricApi.listByType(selectedType.value);
  } finally {
    loading.value = false;
  }
};

const calculateTotal = (type: string): number => {
  return metrics.value
    .filter(m => m.metricType === type)
    .reduce((sum, m) => sum + m.metricValue, 0);
};

const calculateAverage = (type: string): string => {
  const filtered = metrics.value.filter(m => m.metricType === type);
  if (filtered.length === 0) return '0';

  const sum = filtered.reduce((s, m) => s + m.metricValue, 0);
  const avg = sum / filtered.length;

  if (type === 'LATENCY_NS') {
    return (avg / 1000000).toFixed(2);
  }
  return avg.toFixed(2);
};

const formatValue = (value: number): string => {
  return value.toFixed(2);
};

const formatTime = (time: string): string => {
  return new Date(time).toLocaleString();
};

onMounted(() => {
  loadMetrics();
});
</script>

<style scoped>
.metric-dashboard {
  padding: 20px;
}

.metric-card {
  text-align: center;
}

.card-content {
  padding: 20px;
}

.card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.card-unit {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-4 {
  margin-top: 16px;
}
</style>
