<template>
  <div class="alert-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>告警列表</span>
          <el-button type="primary" @click="loadUnresolved">
            仅显示未解决
          </el-button>
        </div>
      </template>

      <el-table :data="alerts" v-loading="loading" stripe border>
        <el-table-column prop="alertLevel" label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.alertLevel)">
              {{ row.alertLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertType" label="类型" width="120" />
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="message" label="消息" />
        <el-table-column prop="runId" label="Run ID" width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.resolved ? 'success' : 'danger'">
              {{ row.resolved ? '已解决' : '未解决' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
              v-if="!row.resolved"
              type="success"
              size="small"
              @click="handleResolve(row)"
            >
              解决
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Alert 详情对话框 -->
    <el-dialog v-model="detailVisible" title="告警详情" width="60%">
      <el-descriptions v-if="selectedAlert" :column="2" border>
        <el-descriptions-item label="告警级别">
          <el-tag :type="getLevelType(selectedAlert.alertLevel)">
            {{ selectedAlert.alertLevel }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警类型">
          {{ selectedAlert.alertType }}
        </el-descriptions-item>
        <el-descriptions-item label="标题">
          {{ selectedAlert.title }}
        </el-descriptions-item>
        <el-descriptions-item label="消息">
          {{ selectedAlert.message }}
        </el-descriptions-item>
        <el-descriptions-item label="Run ID">
          {{ selectedAlert.runId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="Agent ID">
          {{ selectedAlert.agentId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="selectedAlert.resolved ? 'success' : 'danger'">
            {{ selectedAlert.resolved ? '已解决' : '未解决' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="解决人">
          {{ selectedAlert.resolvedBy || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatTime(selectedAlert.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="解决时间">
          {{ formatTime(selectedAlert.resolvedAt) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { alertApi } from '@/api/alert';
import type { Alert } from '@/types/alert';

const alerts = ref<Alert[]>([]);
const loading = ref(false);
const detailVisible = ref(false);
const selectedAlert = ref<Alert | null>(null);

const loadAlerts = async () => {
  loading.value = true;
  try {
    alerts.value = await alertApi.list();
  } finally {
    loading.value = false;
  }
};

const loadUnresolved = async () => {
  loading.value = true;
  try {
    alerts.value = await alertApi.listUnresolved();
  } finally {
    loading.value = false;
  }
};

const handleResolve = async (alert: Alert) => {
  try {
    await ElMessageBox.confirm('确认解决此告警？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });

    await alertApi.resolve(alert.id!, 'current-user');
    ElMessage.success('告警已解决');
    await loadAlerts();
  } catch (error) {
    // 用户取消
  }
};

const handleViewDetail = (alert: Alert) => {
  selectedAlert.value = alert;
  detailVisible.value = true;
};

const getLevelType = (level: string): string => {
  const types: Record<string, string> = {
    INFO: 'info',
    WARNING: 'warning',
    ERROR: 'danger',
    CRITICAL: 'danger',
  };
  return types[level] || 'info';
};

const formatTime = (time: string): string => {
  if (!time) return '-';
  return new Date(time).toLocaleString();
};

onMounted(() => {
  loadAlerts();
});
</script>

<style scoped>
.alert-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
