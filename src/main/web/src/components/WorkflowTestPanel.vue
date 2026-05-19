<template>
  <div class="workflow-test-panel">
    <!-- 输入参数 -->
    <div class="section">
      <h4 class="section-title">输入参数</h4>
      <div class="input-params">
        <div v-for="param in inputParams" :key="param.key" class="param-item">
          <label class="param-label">
            {{ param.name }}
            <span v-if="param.required" class="required">*</span>
          </label>
          <input
            v-model="inputValues[param.key]"
            :type="getInputType(param.type)"
            :placeholder="param.description"
            class="param-input"
          />
        </div>
        <div v-if="inputParams.length === 0" class="empty-message">
          无输入参数
        </div>
      </div>
    </div>

    <!-- 执行控制 -->
    <div class="section">
      <div class="control-buttons">
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
          停止
        </button>
        <button 
          v-if="executionResult"
          class="btn btn-secondary" 
          @click="clearResult"
        >
          清空结果
        </button>
      </div>
    </div>

    <!-- 执行进度 -->
    <div v-if="isExecuting || executionResult" class="section">
      <h4 class="section-title">执行进度</h4>
      <div class="execution-progress">
        <!-- 整体状态 -->
        <div class="status-header">
          <span class="status-badge" :class="executionStatus">
            {{ getStatusText(executionStatus) }}
          </span>
          <span v-if="executionResult?.duration" class="duration">
            耗时: {{ executionResult.duration }}ms
          </span>
        </div>

        <!-- 节点执行状态 -->
        <div class="node-status-list">
          <div 
            v-for="nodeResult in nodeResults" 
            :key="nodeResult.node_id"
            class="node-status-item"
            :class="nodeResult.status"
          >
            <div class="node-header">
              <span class="status-icon">{{ getStatusIcon(nodeResult.status) }}</span>
              <span class="node-name">{{ getNodeName(nodeResult.node_id) }}</span>
              <span v-if="nodeResult.duration" class="node-duration">
                {{ nodeResult.duration }}ms
              </span>
            </div>
            
            <!-- 节点详情（可展开） -->
            <div v-if="expandedNodes.has(nodeResult.node_id)" class="node-details">
              <div class="detail-section">
                <h5>输入</h5>
                <pre class="code-block">{{ JSON.stringify(nodeResult.input, null, 2) }}</pre>
              </div>
              <div v-if="nodeResult.output" class="detail-section">
                <h5>输出</h5>
                <pre class="code-block">{{ JSON.stringify(nodeResult.output, null, 2) }}</pre>
              </div>
              <div v-if="nodeResult.error" class="detail-section error">
                <h5>错误</h5>
                <pre class="code-block">{{ nodeResult.error }}</pre>
              </div>
            </div>
            
            <button 
              class="btn-expand"
              @click="toggleNodeExpand(nodeResult.node_id)"
            >
              {{ expandedNodes.has(nodeResult.node_id) ? '收起' : '展开' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 执行结果 -->
    <div v-if="executionResult && executionResult.status === 'success'" class="section">
      <h4 class="section-title">执行结果</h4>
      <div class="result-display">
        <pre class="code-block">{{ JSON.stringify(getFinalOutput(), null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useWorkflowStore } from '@/stores/workflow-store'
import { 
  startExecution as apiStartExecution,
  stopExecution as apiStopExecution,
  getExecutionStatus
} from '@/api/workflow-execution-api'
import { useWorkspaceStore } from '@/stores/workspace-store'
import type { NodeResult, TaskStatus } from '@/types/workflow-node'

interface Props {
  workflowId: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  close: []
}>()

const workflowStore = useWorkflowStore()
const workspaceStore = useWorkspaceStore()

// 状态
const inputValues = ref<Record<string, any>>({})
const isExecuting = ref(false)
const executionResult = ref<any>(null)
const expandedNodes = ref(new Set<string>())
const taskId = ref('')

// 计算属性
const inputParams = computed(() => {
  // 从开始节点获取输入参数定义
  const startNode = workflowStore.nodes.find(n => n.type === 'start')
  return startNode?.data?.input_params || []
})

const executionStatus = computed<TaskStatus>(() => {
  return executionResult.value?.status || 'pending'
})

const nodeResults = computed<NodeResult[]>(() => {
  return executionResult.value?.node_results || []
})

// 方法
function getInputType(type: string): string {
  const typeMap: Record<string, string> = {
    string: 'text',
    number: 'number',
    boolean: 'checkbox',
    object: 'text',
    array: 'text'
  }
  return typeMap[type] || 'text'
}

async function startExecution() {
  if (isExecuting.value) return
  
  isExecuting.value = true
  
  try {
    const selection = {
      tenantId: workspaceStore.tenantId!,
      workspaceId: workspaceStore.workspaceId!
    }
    
    const result = await apiStartExecution(selection, {
      workflowId: props.workflowId,
      input: inputValues.value,
      debug: true
    })
    
    taskId.value = result.task_id
    executionResult.value = result
    
    // 轮询执行状态
    pollExecutionStatus()
  } catch (error) {
    console.error('执行失败:', error)
    isExecuting.value = false
  }
}

async function pollExecutionStatus() {
  if (!taskId.value) return
  
  const pollInterval = setInterval(async () => {
    try {
      const selection = {
        tenantId: workspaceStore.tenantId!,
        workspaceId: workspaceStore.workspaceId!
      }
      
      const status = await getExecutionStatus(
        selection,
        props.workflowId,
        taskId.value
      )
      
      executionResult.value = status
      
      if (status.status !== 'running') {
        clearInterval(pollInterval)
        isExecuting.value = false
      }
    } catch (error) {
      console.error('获取状态失败:', error)
      clearInterval(pollInterval)
      isExecuting.value = false
    }
  }, 1000)
}

async function stopExecution() {
  if (!taskId.value) return
  
  try {
    const selection = {
      tenantId: workspaceStore.tenantId!,
      workspaceId: workspaceStore.workspaceId!
    }
    
    await apiStopExecution(selection, props.workflowId, taskId.value)
    isExecuting.value = false
  } catch (error) {
    console.error('停止失败:', error)
  }
}

function clearResult() {
  executionResult.value = null
  taskId.value = ''
  expandedNodes.value.clear()
}

function getNodeName(nodeId: string): string {
  const node = workflowStore.nodes.find(n => n.id === nodeId)
  return node?.data?.label || nodeId
}

function getStatusText(status: TaskStatus): string {
  const texts: Record<TaskStatus, string> = {
    pending: '待执行',
    running: '执行中',
    success: '执行成功',
    failed: '执行失败',
    timeout: '执行超时',
    skipped: '已跳过'
  }
  return texts[status] || status
}

function getStatusIcon(status: TaskStatus): string {
  const icons: Record<TaskStatus, string> = {
    pending: '○',
    running: '⏳',
    success: '✓',
    failed: '✗',
    timeout: '⏱',
    skipped: '→'
  }
  return icons[status] || '○'
}

function toggleNodeExpand(nodeId: string) {
  if (expandedNodes.value.has(nodeId)) {
    expandedNodes.value.delete(nodeId)
  } else {
    expandedNodes.value.add(nodeId)
  }
}

function getFinalOutput(): any {
  // 从结束节点获取最终输出
  const endNode = workflowStore.nodes.find(n => n.type === 'end')
  if (!endNode) return null
  
  const endResult = nodeResults.value.find(r => r.node_id === endNode.id)
  return endResult?.output || null
}
</script>

<style scoped>
.workflow-test-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.input-params {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.param-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.param-label {
  font-size: 13px;
  color: #606266;
}

.required {
  color: #f56c6c;
  margin-left: 2px;
}

.param-input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
}

.empty-message {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.control-buttons {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn:hover:not(:disabled) {
  opacity: 0.8;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #409eff;
  border-color: #409eff;
  color: white;
}

.btn-secondary {
  background: white;
  color: #606266;
}

.btn-danger {
  background: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.execution-progress {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #f4f4f5;
  color: #909399;
}

.status-badge.running {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-badge.success {
  background: #f0f9eb;
  color: #67c23a;
}

.status-badge.failed {
  background: #fef0f0;
  color: #f56c6c;
}

.duration {
  font-size: 12px;
  color: #909399;
}

.node-status-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.node-status-item {
  padding: 12px;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #dcdfe6;
}

.node-status-item.success {
  border-left-color: #67c23a;
}

.node-status-item.failed {
  border-left-color: #f56c6c;
}

.node-status-item.running {
  border-left-color: #e6a23c;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.status-icon {
  font-size: 14px;
}

.node-name {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.node-duration {
  font-size: 12px;
  color: #909399;
}

.node-details {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.detail-section {
  margin-bottom: 12px;
}

.detail-section h5 {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #909399;
}

.detail-section.error h5 {
  color: #f56c6c;
}

.code-block {
  margin: 0;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 11px;
  font-family: 'Monaco', 'Menlo', monospace;
  overflow-x: auto;
}

.btn-expand {
  padding: 4px 8px;
  border: none;
  background: transparent;
  color: #409eff;
  font-size: 12px;
  cursor: pointer;
}

.result-display {
  background: white;
  border-radius: 4px;
  padding: 12px;
}
</style>
