<template>
  <div class="workflow-property-panel">
    <div class="panel-header">
      <h3>节点属性</h3>
      <button class="btn-close" @click="$emit('close')">×</button>
    </div>
    
    <div class="panel-body">
      <!-- 基本信息 -->
      <div class="property-section">
        <div class="section-title">基本信息</div>
        <div class="form-item">
          <label>节点名称</label>
          <input 
            v-model="localNode.name"
            class="form-input"
            @change="handleUpdate"
          />
        </div>
        <div class="form-item">
          <label>节点描述</label>
          <textarea 
            v-model="localNode.description"
            class="form-textarea"
            rows="2"
            @change="handleUpdate"
          />
        </div>
      </div>
      
      <!-- 输入参数 -->
      <div class="property-section" v-if="localNode.config.inputParams?.length">
        <div class="section-title">输入参数</div>
        <div 
          v-for="(param, index) in localNode.config.inputParams"
          :key="param.name"
          class="param-item"
        >
          <div class="param-header">
            <span class="param-name">{{ param.name }}</span>
            <span class="param-type">{{ param.type }}</span>
            <span v-if="param.required" class="required-badge">必填</span>
          </div>
          <div class="param-input">
            <input 
              v-model="param.value"
              class="form-input"
              :placeholder="param.description"
              @change="handleUpdate"
            />
            <button 
              class="btn-variable"
              @click="openVariableSelector(index, 'input')"
              title="选择变量"
            >
              ${}
            </button>
          </div>
        </div>
      </div>
      
      <!-- 输出参数 -->
      <div class="property-section" v-if="localNode.config.outputParams?.length">
        <div class="section-title">输出参数</div>
        <div 
          v-for="param in localNode.config.outputParams"
          :key="param.name"
          class="param-item readonly"
        >
          <div class="param-header">
            <span class="param-name">{{ param.name }}</span>
            <span class="param-type">{{ param.type }}</span>
          </div>
          <div class="param-desc">{{ param.description }}</div>
        </div>
      </div>
      
      <!-- 节点特定配置 -->
      <div class="property-section">
        <div class="section-title">节点配置</div>
        
        <!-- LLM节点配置 -->
        <template v-if="localNode.type === 'llm'">
          <div class="form-item">
            <label>模型</label>
            <select 
              v-model="localNode.config.nodeParams!.model"
              class="form-select"
              @change="handleUpdate"
            >
              <option value="">请选择模型</option>
              <option value="gpt-4">GPT-4</option>
              <option value="gpt-3.5-turbo">GPT-3.5 Turbo</option>
              <option value="claude-3">Claude 3</option>
              <option value="qwen-max">通义千问-Max</option>
            </select>
          </div>
          <div class="form-item">
            <label>温度 (Temperature)</label>
            <input 
              v-model.number="localNode.config.nodeParams!.temperature"
              type="number"
              min="0"
              max="2"
              step="0.1"
              class="form-input"
              @change="handleUpdate"
            />
          </div>
          <div class="form-item">
            <label>最大Token数</label>
            <input 
              v-model.number="localNode.config.nodeParams!.maxTokens"
              type="number"
              min="1"
              class="form-input"
              @change="handleUpdate"
            />
          </div>
          <div class="form-item">
            <label>系统提示词</label>
            <textarea 
              v-model="localNode.config.nodeParams!.systemPrompt"
              class="form-textarea"
              rows="4"
              placeholder="输入系统提示词..."
              @change="handleUpdate"
            />
          </div>
        </template>
        
        <!-- 条件节点配置 -->
        <template v-if="localNode.type === 'condition'">
          <div class="form-item">
            <label>条件表达式</label>
            <textarea 
              v-model="localNode.config.nodeParams!.expression"
              class="form-textarea"
              rows="3"
              placeholder="输入条件表达式..."
              @change="handleUpdate"
            />
          </div>
        </template>
        
        <!-- 循环节点配置 -->
        <template v-if="localNode.type === 'loop'">
          <div class="form-item">
            <label>最大迭代次数</label>
            <input 
              v-model.number="localNode.config.nodeParams!.maxIterations"
              type="number"
              min="1"
              class="form-input"
              @change="handleUpdate"
            />
          </div>
          <div class="form-item">
            <label>循环条件</label>
            <textarea 
              v-model="localNode.config.nodeParams!.condition"
              class="form-textarea"
              rows="2"
              placeholder="输入循环条件..."
              @change="handleUpdate"
            />
          </div>
        </template>
        
        <!-- 工具节点配置 -->
        <template v-if="localNode.type === 'tool'">
          <div class="form-item">
            <label>工具名称</label>
            <input 
              v-model="localNode.config.nodeParams!.toolName"
              class="form-input"
              placeholder="输入工具名称"
              @change="handleUpdate"
            />
          </div>
          <div class="form-item">
            <label>工具参数 (JSON)</label>
            <textarea 
              v-model="localNode.config.nodeParams!.parameters"
              class="form-textarea"
              rows="3"
              placeholder='{"key": "value"}'
              @change="handleUpdate"
            />
          </div>
        </template>
      </div>
      
      <!-- 高级配置 -->
      <div class="property-section">
        <div class="section-title">高级配置</div>
        <div class="form-item">
          <label>超时时间 (毫秒)</label>
          <input 
            v-model.number="localNode.config.timeout"
            type="number"
            min="1000"
            class="form-input"
            placeholder="30000"
            @change="handleUpdate"
          />
        </div>
        <div class="form-item">
          <label>重试次数</label>
          <input 
            v-model.number="localNode.config.retryPolicy!.maxRetries"
            type="number"
            min="0"
            max="5"
            class="form-input"
            @change="handleUpdate"
          />
        </div>
      </div>
    </div>
    
    <!-- 变量选择器 -->
    <div v-if="showVariableSelector" class="variable-selector-overlay">
      <div class="variable-selector">
        <div class="selector-header">
          <span>选择变量</span>
          <button class="btn-close" @click="showVariableSelector = false">×</button>
        </div>
        <div class="selector-body">
          <div 
            v-for="variable in variables"
            :key="variable.key"
            class="variable-item"
            @click="selectVariable(variable)"
          >
            <span class="variable-key">{{ variable.key }}</span>
            <span class="variable-label">{{ variable.label }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { WorkflowNode } from '@/types/workflow'

interface Variable {
  key: string
  label: string
  type: string
  nodeId: string
}

const props = defineProps<{
  node: WorkflowNode
  variables: Variable[]
}>()

const emit = defineEmits<{
  'update': [node: WorkflowNode]
  'close': []
}>()

// 本地节点副本
const localNode = ref<WorkflowNode>(JSON.parse(JSON.stringify(props.node)))

// 变量选择器状态
const showVariableSelector = ref(false)
const currentParamIndex = ref(0)
const currentParamType = ref<'input' | 'output'>('input')

// 监听props变化
watch(() => props.node, (newNode) => {
  localNode.value = JSON.parse(JSON.stringify(newNode))
}, { deep: true })

// 方法
function handleUpdate() {
  emit('update', localNode.value)
}

function openVariableSelector(index: number, type: 'input' | 'output') {
  currentParamIndex.value = index
  currentParamType.value = type
  showVariableSelector.value = true
}

function selectVariable(variable: Variable) {
  if (currentParamType.value === 'input' && localNode.value.config.inputParams) {
    localNode.value.config.inputParams[currentParamIndex.value].value = `\${${variable.key}}`
  }
  showVariableSelector.value = false
  handleUpdate()
}
</script>

<style scoped>
.workflow-property-panel {
  width: 320px;
  background: white;
  border-left: 1px solid #e1e5eb;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e1e5eb;
}

.panel-header h3 {
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

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.property-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e1e5eb;
}

.form-item {
  margin-bottom: 12px;
}

.form-item label {
  display: block;
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #409eff;
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

.param-item {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}

.param-item.readonly {
  background: #fafafa;
}

.param-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.param-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.param-type {
  font-size: 12px;
  color: #909399;
  padding: 2px 6px;
  background: #e4e7ed;
  border-radius: 3px;
}

.required-badge {
  font-size: 11px;
  color: #f56c6c;
  padding: 2px 6px;
  background: #fef0f0;
  border-radius: 3px;
}

.param-input {
  display: flex;
  gap: 8px;
}

.param-input .form-input {
  flex: 1;
}

.btn-variable {
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  color: #909399;
}

.btn-variable:hover {
  background: #f5f7fa;
  color: #606266;
}

.param-desc {
  font-size: 12px;
  color: #909399;
}

.variable-selector-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.variable-selector {
  width: 400px;
  max-height: 500px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
}

.selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e1e5eb;
  font-weight: 600;
}

.selector-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.variable-item {
  display: flex;
  flex-direction: column;
  padding: 10px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.variable-item:hover {
  background: #f5f7fa;
}

.variable-key {
  font-size: 13px;
  font-family: 'Monaco', 'Menlo', monospace;
  color: #409eff;
  margin-bottom: 2px;
}

.variable-label {
  font-size: 12px;
  color: #909399;
}
</style>
