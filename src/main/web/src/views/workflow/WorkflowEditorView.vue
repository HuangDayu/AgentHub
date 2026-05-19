<template>
  <div class="workflow-editor-view">
    <!-- 顶部工具栏 -->
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <button class="btn btn-back" @click="goBack">
          ← 返回
        </button>
        <div class="workflow-info">
          <input
            v-model="workflowCode"
            class="code-input"
            placeholder="编码"
            @change="markDirty"
          />
          <input
            v-model="workflowName"
            class="name-input"
            placeholder="名称"
            @change="markDirty"
          />
          <input
            v-model="workflowDescription"
            class="desc-input"
            placeholder="描述"
            @change="markDirty"
          />
          <span v-if="isDirty" class="dirty-badge">未保存</span>
          <span class="status-badge" :class="workflowStatus">
            {{ statusText }}
          </span>
        </div>
      </div>
      
      <div class="toolbar-center">
        <button 
          class="btn btn-icon" 
          @click="undo"
          :disabled="!canUndo"
          title="撤销"
        >
          ↶
        </button>
        <button 
          class="btn btn-icon" 
          @click="redo"
          :disabled="!canRedo"
          title="重做"
        >
          ↷
        </button>
        <div class="divider"></div>
        <button 
          class="btn btn-icon" 
          @click="autoLayout"
          title="自动布局"
        >
          ⚙
        </button>
        <button 
          class="btn btn-icon" 
          @click="toggleMiniMap"
          :class="{ active: showMiniMap }"
          title="小地图"
        >
          🗺
        </button>
      </div>
      
      <div class="toolbar-right">
        <button 
          class="btn btn-secondary"
          @click="validate"
          :disabled="isValidating"
        >
          {{ isValidating ? '验证中...' : '验证' }}
        </button>
        <button 
          class="btn btn-primary"
          @click="save"
          :disabled="isSaving || !isDirty"
        >
          {{ isSaving ? '保存中...' : '保存' }}
        </button>
        <button 
          class="btn btn-success"
          @click="execute"
          :disabled="isExecuting"
        >
          {{ isExecuting ? '执行中...' : '执行' }}
        </button>
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-body">
      <!-- DAG编辑器 -->
      <div class="canvas-container">
        <DagEditor
          ref="dagEditorRef"
          v-model="graphDefinition"
          :show-mini-map="showMiniMap"
          :read-only="readOnly"
          @node-select="handleNodeSelect"
          @edge-select="handleEdgeSelect"
        />
      </div>
      
      <!-- 配置面板 -->
      <div v-if="showConfigPanel" class="config-panel">
        <ConfigPanelContainer
          @save="handleConfigSave"
          @cancel="handleConfigCancel"
        />
      </div>
    </div>

    <!-- 执行结果面板 -->
    <div v-if="executionResult" class="execution-panel">
      <div class="panel-header">
        <h3>执行结果</h3>
        <button class="btn-close" @click="closeExecutionPanel">×</button>
      </div>
      <div class="panel-body">
        <div class="result-summary">
          <div class="summary-item">
            <span class="label">状态:</span>
            <span class="value" :class="executionResult.status">
              {{ executionResult.status }}
            </span>
          </div>
          <div class="summary-item">
            <span class="label">耗时:</span>
            <span class="value">{{ executionResult.duration }}ms</span>
          </div>
        </div>
        <div class="result-details">
          <div 
            v-for="nodeResult in executionResult.nodeResults" 
            :key="nodeResult.nodeId"
            class="node-result"
          >
            <div class="node-header">
              <span class="node-name">{{ nodeResult.nodeName }}</span>
              <span class="node-status" :class="nodeResult.status">
                {{ nodeResult.status }}
              </span>
            </div>
            <div v-if="nodeResult.output" class="node-output">
              <pre>{{ JSON.stringify(nodeResult.output, null, 2) }}</pre>
            </div>
            <div v-if="nodeResult.error" class="node-error">
              {{ nodeResult.error }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useWorkflowStore } from '@/stores/workflow-store'
import DagEditor from '@/components/workflow/DagEditor.vue'
import ConfigPanelContainer from '@/components/workflow/panels/ConfigPanelContainer.vue'
import type { Node, Edge } from '@vue-flow/core'

const router = useRouter()
const route = useRoute()
const workflowStore = useWorkflowStore()

// Refs
const dagEditorRef = ref<InstanceType<typeof DagEditor>>()

// 状态
const workflowCode = ref('')
const workflowName = ref('')
const workflowDescription = ref('')
const graphDefinition = ref('{\n  "nodes": [],\n  "edges": []\n}')
const isDirty = ref(false)
const isSaving = ref(false)
const isValidating = ref(false)
const isExecuting = ref(false)
const showMiniMap = ref(true)
const showConfigPanel = ref(true)
const readOnly = ref(false)
const executionResult = ref<any>(null)

// 计算属性
const workflowStatus = computed(() => {
  if (isExecuting.value) return 'running'
  if (executionResult.value) {
    return executionResult.value.status === 'success' ? 'success' : 'error'
  }
  return 'idle'
})

const statusText = computed(() => {
  switch (workflowStatus.value) {
    case 'running': return '运行中'
    case 'success': return '成功'
    case 'error': return '错误'
    case 'idle': return '空闲'
    default: return '空闲'
  }
})

const canUndo = computed(() => workflowStore.canUndo)
const canRedo = computed(() => workflowStore.canRedo)

// 方法
function goBack() {
  if (isDirty.value) {
    if (!confirm('工作流未保存，确定要离开吗？')) {
      return
    }
  }
  router.push('/workflow')
}

function markDirty() {
  isDirty.value = true
}

function undo() {
  workflowStore.undo()
}

function redo() {
  workflowStore.redo()
}

function autoLayout() {
  dagEditorRef.value?.autoLayout()
}

function toggleMiniMap() {
  showMiniMap.value = !showMiniMap.value
}

async function validate() {
  isValidating.value = true
  try {
    // TODO: 调用验证API
    await new Promise(resolve => setTimeout(resolve, 1000))
    alert('验证通过')
  } catch (error) {
    alert('验证失败: ' + error)
  } finally {
    isValidating.value = false
  }
}

async function save() {
  isSaving.value = true
  try {
    // TODO: 调用保存API
    await new Promise(resolve => setTimeout(resolve, 1000))
    isDirty.value = false
    workflowStore.markAsSaved()
    alert('保存成功')
  } catch (error) {
    alert('保存失败: ' + error)
  } finally {
    isSaving.value = false
  }
}

async function execute() {
  isExecuting.value = true
  try {
    // TODO: 调用执行API
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 模拟执行结果
    executionResult.value = {
      status: 'success',
      duration: 1234,
      nodeResults: [
        {
          nodeId: 'start-1',
          nodeName: '开始',
          status: 'success',
          output: { message: '工作流开始' }
        },
        {
          nodeId: 'llm-1',
          nodeName: 'LLM',
          status: 'success',
          output: { response: '这是LLM的响应' }
        }
      ]
    }
  } catch (error) {
    executionResult.value = {
      status: 'error',
      duration: 0,
      nodeResults: [],
      error: String(error)
    }
  } finally {
    isExecuting.value = false
  }
}

function handleNodeSelect(node: Node) {
  showConfigPanel.value = true
}

function handleEdgeSelect(edge: Edge) {
  // 可以显示边的配置面板
}

function handleConfigSave(nodeId: string, config: any) {
  isDirty.value = true
}

function handleConfigCancel() {
  showConfigPanel.value = false
}

function closeExecutionPanel() {
  executionResult.value = null
}

// 监听graphDefinition变化
watch(graphDefinition, () => {
  isDirty.value = true
  try {
    const graph = JSON.parse(graphDefinition.value)
    workflowStore.setGraph(graph)
  } catch (error) {
    // JSON解析错误，忽略
  }
}, { deep: true })

// 初始化
onMounted(async () => {
  const workflowId = route.params.id as string
  
  if (workflowId && workflowId !== 'new') {
    // 加载现有工作流
    try {
      // TODO: 调用API加载工作流
      workflowName.value = '示例工作流'
      graphDefinition.value = JSON.stringify({
        nodes: [
          {
            id: 'start-1',
            type: 'start',
            position: { x: 100, y: 100 },
            data: { label: '开始' }
          },
          {
            id: 'llm-1',
            type: 'llm',
            position: { x: 300, y: 100 },
            data: { label: 'LLM', config: { model: 'gpt-4', temperature: 0.7 } }
          },
          {
            id: 'end-1',
            type: 'end',
            position: { x: 500, y: 100 },
            data: { label: '结束' }
          }
        ],
        edges: [
          { id: 'e1', source: 'start-1', target: 'llm-1' },
          { id: 'e2', source: 'llm-1', target: 'end-1' }
        ]
      }, null, 2)
      
      workflowStore.setWorkflowInfo(workflowId, workflowName.value, '')
    } catch (error) {
      alert('加载工作流失败: ' + error)
    }
  } else {
    // 新建工作流
    workflowName.value = '新建工作流'
    workflowStore.setWorkflowInfo('new', workflowName.value, '')
  }
})
</script>

<style scoped>
.workflow-editor-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.toolbar-left,
.toolbar-center,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.workflow-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-input {
  width: 120px;
  font-size: 14px;
  border: 1px solid #dcdfe6;
  padding: 4px 8px;
  border-radius: 4px;
}

.code-input:focus {
  outline: none;
  border-color: #409eff;
}

.name-input {
  width: 180px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid #dcdfe6;
  padding: 4px 8px;
  border-radius: 4px;
}

.name-input:focus {
  outline: none;
  border-color: #409eff;
}

.desc-input {
  width: 240px;
  font-size: 14px;
  border: 1px solid #dcdfe6;
  padding: 4px 8px;
  border-radius: 4px;
}

.desc-input:focus {
  outline: none;
  border-color: #409eff;
}

.dirty-badge {
  padding: 2px 8px;
  background: #fff3cd;
  color: #856404;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge {
  padding: 2px 8px;
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

.divider {
  width: 1px;
  height: 24px;
  background: #dcdfe6;
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

.btn:hover:not(:disabled) {
  border-color: #409eff;
  color: #409eff;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  padding: 8px;
  min-width: 36px;
}

.btn-icon.active {
  background: #409eff;
  color: white;
  border-color: #409eff;
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
  border-color: #66b1ff;
}

.btn-secondary {
  color: #606266;
}

.btn-success {
  background: #67c23a;
  color: white;
  border-color: #67c23a;
}

.btn-success:hover:not(:disabled) {
  background: #85ce61;
  border-color: #85ce61;
}

.editor-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.canvas-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  min-width: 800px;
}

.config-panel {
  width: 320px;
  background: white;
  border-left: 1px solid #e4e7ed;
  overflow-y: auto;
  flex-shrink: 0;
}

.execution-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 300px;
  background: white;
  border-top: 1px solid #e4e7ed;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #909399;
}

.btn-close:hover {
  color: #606266;
}

.panel-body {
  padding: 20px;
  overflow-y: auto;
  height: calc(100% - 48px);
}

.result-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.summary-item {
  display: flex;
  gap: 8px;
}

.summary-item .label {
  color: #909399;
}

.summary-item .value {
  font-weight: 500;
}

.summary-item .value.success {
  color: #67c23a;
}

.summary-item .value.error {
  color: #f56c6c;
}

.result-details {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.node-result {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.node-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.node-name {
  font-weight: 500;
}

.node-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.node-status.success {
  background: #d4edda;
  color: #155724;
}

.node-status.error {
  background: #f8d7da;
  color: #721c24;
}

.node-output {
  margin-top: 8px;
}

.node-output pre {
  margin: 0;
  padding: 8px;
  background: white;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
}

.node-error {
  margin-top: 8px;
  padding: 8px;
  background: #f8d7da;
  color: #721c24;
  border-radius: 4px;
}
</style>
