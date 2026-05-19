<template>
  <div class="workflow-execution-view">
    <div class="page-header">
      <button class="btn btn-back" @click="goBack">
        ← 返回
      </button>
      <h1>工作流执行: {{ workflowName }}</h1>
    </div>
    
    <div class="execution-content">
      <!-- 执行控制面板 -->
      <div class="control-panel">
        <div class="control-group">
          <label>输入参数</label>
          <textarea 
            v-model="inputParams" 
            class="params-input"
            rows="10"
            placeholder="输入JSON格式的参数"
          ></textarea>
        </div>
        
        <div class="control-actions">
          <button 
            class="btn btn-primary" 
            @click="startExecution"
            :disabled="isExecuting"
          >
            {{ isExecuting ? '执行中...' : '开始执行' }}
          </button>
          <button 
            v-if="isExecuting"
            class="btn btn-danger" 
            @click="stopExecution"
          >
            停止执行
          </button>
        </div>
      </div>
      
      <!-- 执行状态面板 -->
      <div class="status-panel">
        <div class="status-header">
          <h3>执行状态</h3>
          <span class="status-badge" :class="executionStatus">
            {{ statusText }}
          </span>
        </div>
        
        <div class="execution-timeline">
          <div 
            v-for="step in executionSteps" 
            :key="step.id"
            class="timeline-item"
            :class="step.status"
          >
            <div class="timeline-marker"></div>
            <div class="timeline-content">
              <div class="step-header">
                <span class="step-name">{{ step.name }}</span>
                <span class="step-time">{{ step.duration }}ms</span>
              </div>
              <div v-if="step.output" class="step-output">
                <pre>{{ JSON.stringify(step.output, null, 2) }}</pre>
              </div>
              <div v-if="step.error" class="step-error">
                {{ step.error }}
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="executionSteps.length === 0" class="empty-state">
          <p>点击"开始执行"运行工作流</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const workflowName = ref('示例工作流')
const inputParams = ref('{\n  "input": "Hello, World!"\n}')
const isExecuting = ref(false)
const executionStatus = ref('idle')
const executionSteps = ref<any[]>([])

const statusText = computed(() => {
  switch (executionStatus.value) {
    case 'idle': return '空闲'
    case 'running': return '运行中'
    case 'success': return '成功'
    case 'error': return '错误'
    default: return '空闲'
  }
})

function goBack() {
  router.push('/workflow')
}

async function startExecution() {
  isExecuting.value = true
  executionStatus.value = 'running'
  executionSteps.value = []
  
  try {
    // 模拟执行步骤
    const steps = [
      { id: '1', name: '开始节点', duration: 10 },
      { id: '2', name: 'LLM节点', duration: 1500 },
      { id: '3', name: '结束节点', duration: 5 }
    ]
    
    for (const step of steps) {
      await new Promise(resolve => setTimeout(resolve, step.duration))
      
      executionSteps.value.push({
        ...step,
        status: 'success',
        output: { message: `${step.name}执行完成` }
      })
    }
    
    executionStatus.value = 'success'
  } catch (error) {
    executionStatus.value = 'error'
    executionSteps.value.push({
      id: 'error',
      name: '执行错误',
      status: 'error',
      error: String(error)
    })
  } finally {
    isExecuting.value = false
  }
}

function stopExecution() {
  isExecuting.value = false
  executionStatus.value = 'error'
}
</script>

<style scoped>
.workflow-execution-view {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.execution-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.control-panel,
.status-panel {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.control-group {
  margin-bottom: 20px;
}

.control-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #303133;
}

.params-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  resize: vertical;
}

.params-input:focus {
  outline: none;
  border-color: #409eff;
}

.control-actions {
  display: flex;
  gap: 12px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.status-header h3 {
  margin: 0;
  font-size: 16px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge.idle {
  background: #e2e3e5;
  color: #383d41;
}

.status-badge.running {
  background: #fff3cd;
  color: #856404;
}

.status-badge.success {
  background: #d4edda;
  color: #155724;
}

.status-badge.error {
  background: #f8d7da;
  color: #721c24;
}

.execution-timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timeline-item {
  display: flex;
  gap: 12px;
  padding-left: 20px;
  position: relative;
}

.timeline-marker {
  position: absolute;
  left: 0;
  top: 8px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #dcdfe6;
}

.timeline-item.success .timeline-marker {
  background: #67c23a;
}

.timeline-item.error .timeline-marker {
  background: #f56c6c;
}

.timeline-item.running .timeline-marker {
  background: #e6a23c;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.timeline-content {
  flex: 1;
}

.step-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.step-name {
  font-weight: 500;
}

.step-time {
  color: #909399;
  font-size: 12px;
}

.step-output,
.step-error {
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
}

.step-output {
  background: #f5f7fa;
}

.step-output pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', monospace;
}

.step-error {
  background: #f8d7da;
  color: #721c24;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.btn {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-back {
  color: #606266;
}

.btn-primary {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.btn-primary:hover:not(:disabled) {
  background: #66b1ff;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-danger {
  background: #f56c6c;
  color: white;
  border-color: #f56c6c;
}

.btn-danger:hover {
  background: #f78989;
}
</style>
