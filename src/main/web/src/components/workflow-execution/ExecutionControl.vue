<template>
  <div class="execution-control">
    <div class="control-header">
      <h3>执行控制</h3>
      <button class="btn-close" @click="$emit('close')">×</button>
    </div>
    
    <div class="control-body">
      <!-- 执行状态 -->
      <div class="status-section">
        <div class="status-header">
          <span class="status-label">执行状态</span>
          <span class="status-value" :class="execution?.status">
            {{ statusText }}
          </span>
        </div>
        
        <div v-if="execution" class="execution-info">
          <div class="info-row">
            <span class="info-label">任务ID:</span>
            <span class="info-value">{{ execution.task_id }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">开始时间:</span>
            <span class="info-value">{{ formatTime(execution.start_time) }}</span>
          </div>
          <div v-if="execution.duration" class="info-row">
            <span class="info-label">执行时长:</span>
            <span class="info-value">{{ formatDuration(execution.duration) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 进度条 -->
      <div v-if="isRunning" class="progress-section">
        <div class="progress-bar">
          <div 
            class="progress-fill" 
            :style="{ width: `${progress}%` }"
          ></div>
        </div>
        <div class="progress-text">
          {{ completedNodes }} / {{ totalNodes }} 节点已完成
        </div>
      </div>
      
      <!-- 节点执行结果 -->
      <div v-if="execution?.node_results?.length" class="results-section">
        <div class="section-title">节点执行结果</div>
        <div class="results-list">
          <div 
            v-for="result in execution.node_results"
            :key="result.node_id"
            class="result-item"
          >
            <NodeStatusIndicator
              :status="result.status"
              :node-id="result.node_id"
            />
            <div class="result-info">
              <span class="result-node">{{ result.node_id }}</span>
              <span class="result-duration" v-if="result.duration">
                {{ formatDuration(result.duration) }}
              </span>
            </div>
            <button 
              class="btn-detail"
              @click="showNodeDetail(result)"
            >
              详情
            </button>
          </div>
        </div>
      </div>
      
      <!-- 错误信息 -->
      <div v-if="execution?.error" class="error-section">
        <div class="error-title">执行错误</div>
        <div class="error-message">{{ execution.error }}</div>
      </div>
    </div>
    
    <div class="control-footer">
      <button 
        v-if="isRunning"
        class="btn btn-danger"
        @click="$emit('stop')"
      >
        停止执行
      </button>
      <button 
        v-else-if="isCompleted"
        class="btn btn-primary"
        @click="handleRerun"
      >
        重新执行
      </button>
    </div>
    
    <!-- 节点详情对话框 -->
    <ModalDialog
      v-if="selectedResult"
      :title="`节点执行详情 - ${selectedResult.node_id}`"
      @close="selectedResult = null"
    >
      <ExecutionResultPanel :result="selectedResult" />
    </ModalDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import NodeStatusIndicator from './NodeStatusIndicator.vue'
import ExecutionResultPanel from './ExecutionResultPanel.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import type { WorkflowExecution, NodeResult } from '@/types/workflow'

const props = defineProps<{
  workflowId: string
  execution: WorkflowExecution | null
}>()

const emit = defineEmits<{
  'close': []
  'stop': []
  'rerun': []
}>()

const selectedResult = ref<NodeResult | null>(null)

// 计算属性
const statusText = computed(() => {
  if (!props.execution) return '未开始'
  const texts: Record<string, string> = {
    pending: '等待中',
    running: '执行中',
    success: '成功',
    failed: '失败',
    timeout: '超时',
    skipped: '跳过'
  }
  return texts[props.execution.status] || props.execution.status
})

const isRunning = computed(() => {
  return props.execution?.status === 'running'
})

const isCompleted = computed(() => {
  return props.execution?.status === 'success' || 
         props.execution?.status === 'failed'
})

const totalNodes = computed(() => {
  return props.execution?.node_results?.length || 0
})

const completedNodes = computed(() => {
  if (!props.execution?.node_results) return 0
  return props.execution.node_results.filter(
    r => r.status === 'success' || r.status === 'failed' || r.status === 'skipped'
  ).length
})

const progress = computed(() => {
  if (totalNodes.value === 0) return 0
  return (completedNodes.value / totalNodes.value) * 100
})

// 方法
function formatTime(time: string): string {
  return new Date(time).toLocaleString('zh-CN')
}

function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`
  if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`
  return `${(ms / 60000).toFixed(2)}min`
}

function showNodeDetail(result: NodeResult) {
  selectedResult.value = result
}

function handleRerun() {
  emit('rerun')
}
</script>

<style scoped>
.execution-control {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 400px;
  max-height: 600px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 1000;
}

.control-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e1e5eb;
}

.control-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.btn-close {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  font-size: 18px;
  color: #909399;
  cursor: pointer;
  border-radius: 4px;
}

.btn-close:hover {
  background: #f4f4f5;
  color: #606266;
}

.control-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.status-section {
  margin-bottom: 16px;
}

.status-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.status-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.status-value {
  font-size: 14px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 4px;
}

.status-value.running {
  background: #ecf5ff;
  color: #409eff;
}

.status-value.success {
  background: #f0f9eb;
  color: #67c23a;
}

.status-value.failed {
  background: #fef0f0;
  color: #f56c6c;
}

.execution-info {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  color: #909399;
}

.info-value {
  color: #606266;
  font-family: 'Monaco', 'Menlo', monospace;
}

.progress-section {
  margin-bottom: 16px;
}

.progress-bar {
  height: 8px;
  background: #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: #409eff;
  transition: width 0.3s;
}

.progress-text {
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.results-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 12px;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.result-node {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.result-duration {
  font-size: 11px;
  color: #909399;
}

.btn-detail {
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.btn-detail:hover {
  background: #f5f7fa;
}

.error-section {
  padding: 12px;
  background: #fef0f0;
  border-radius: 4px;
}

.error-title {
  font-size: 13px;
  font-weight: 500;
  color: #f56c6c;
  margin-bottom: 8px;
}

.error-message {
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
}

.control-footer {
  padding: 16px;
  border-top: 1px solid #e1e5eb;
  display: flex;
  justify-content: flex-end;
}

.btn {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.btn-danger {
  background: #f56c6c;
  color: white;
  border-color: #f56c6c;
}
</style>
