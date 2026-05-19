<template>
  <div class="llm-panel">
    <!-- 模型选择 -->
    <div class="form-section">
      <label class="form-label">模型选择</label>
      <select v-model="config.model_id" class="form-select" @change="updateConfig">
        <option value="">请选择模型</option>
        <option v-for="model in availableModels" :key="model.id" :value="model.id">
          {{ model.name }}
        </option>
      </select>
    </div>

    <!-- 模型参数 -->
    <div class="form-section">
      <label class="form-label">模型参数</label>
      
      <div class="param-item">
        <label>Temperature</label>
        <input 
          v-model.number="config.model_config.temperature" 
          type="range" 
          min="0" 
          max="2" 
          step="0.1"
          @change="updateConfig"
        />
        <span class="param-value">{{ config.model_config.temperature }}</span>
      </div>
      
      <div class="param-item">
        <label>Max Tokens</label>
        <input 
          v-model.number="config.model_config.max_tokens" 
          type="number" 
          min="100" 
          max="8000"
          @change="updateConfig"
        />
      </div>
      
      <div class="param-item">
        <label>Top P</label>
        <input 
          v-model.number="config.model_config.top_p" 
          type="range" 
          min="0" 
          max="1" 
          step="0.1"
          @change="updateConfig"
        />
        <span class="param-value">{{ config.model_config.top_p }}</span>
      </div>
    </div>

    <!-- 系统提示词 -->
    <div class="form-section">
      <label class="form-label">
        系统提示词
        <button class="btn-variable" @click="insertVariable('sys_prompt')">
          插入变量
        </button>
      </label>
      <textarea 
        v-model="config.sys_prompt" 
        class="form-textarea"
        placeholder="输入系统提示词..."
        rows="4"
        @change="updateConfig"
      ></textarea>
      <VariableHighlighter 
        :text="config.sys_prompt" 
        :variables="availableVariables"
      />
    </div>

    <!-- 用户提示词 -->
    <div class="form-section">
      <label class="form-label">
        用户提示词
        <button class="btn-variable" @click="insertVariable('user_prompt')">
          插入变量
        </button>
      </label>
      <textarea 
        v-model="config.user_prompt" 
        class="form-textarea"
        placeholder="输入用户提示词..."
        rows="6"
        @change="updateConfig"
      ></textarea>
      <VariableHighlighter 
        :text="config.user_prompt" 
        :variables="availableVariables"
      />
    </div>

    <!-- 输出变量名 -->
    <div class="form-section">
      <label class="form-label">输出变量名</label>
      <input 
        v-model="config.output_key" 
        type="text" 
        class="form-input"
        placeholder="llm_output"
        @change="updateConfig"
      />
    </div>

    <!-- 验证信息 -->
    <div v-if="validationMessage" class="validation-message" :class="validationType">
      {{ validationMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { WorkflowNode, VarTreeItem, LLMNodeParam } from '@/types/workflow-node'
import VariableHighlighter from './VariableHighlighter.vue'

interface Props {
  node: WorkflowNode
  availableVariables: VarTreeItem[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  update: [updates: any]
}>()

// 配置数据
const config = ref<LLMNodeParam>({
  model_id: '',
  model_config: {
    temperature: 0.7,
    max_tokens: 2000,
    top_p: 0.9,
    frequency_penalty: 0,
    presence_penalty: 0
  },
  sys_prompt: '',
  user_prompt: '',
  output_key: 'llm_output'
})

// 可用模型列表（从API获取）
const availableModels = ref([
  { id: 'gpt-4', name: 'GPT-4' },
  { id: 'gpt-3.5-turbo', name: 'GPT-3.5 Turbo' },
  { id: 'claude-3', name: 'Claude 3' },
  { id: 'qwen-max', name: '通义千问-Max' },
  { id: 'qwen-plus', name: '通义千问-Plus' }
])

// 验证信息
const validationMessage = ref('')
const validationType = ref<'error' | 'warning'>('error')

// 初始化配置
watch(() => props.node, (node) => {
  if (node?.data?.node_param) {
    config.value = { ...node.data.node_param } as LLMNodeParam
  }
}, { immediate: true })

// 更新配置
function updateConfig() {
  validate()
  
  emit('update', {
    node_param: { ...config.value }
  })
}

// 验证
function validate() {
  if (!config.value.model_id) {
    validationMessage.value = '请选择模型'
    validationType.value = 'error'
    return false
  }
  
  if (!config.value.user_prompt) {
    validationMessage.value = '请输入用户提示词'
    validationType.value = 'error'
    return false
  }
  
  validationMessage.value = ''
  return true
}

// 插入变量
function insertVariable(field: string) {
  // 显示变量选择器
  // TODO: 实现变量选择对话框
  console.log('插入变量到:', field)
}
</script>

<style scoped>
.llm-panel {
  padding: 0;
}

.form-section {
  margin-bottom: 20px;
}

.form-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  transition: all 0.2s;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
  line-height: 1.6;
}

.param-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.param-item label {
  width: 100px;
  font-size: 12px;
  color: #606266;
}

.param-item input[type="range"] {
  flex: 1;
}

.param-item input[type="number"] {
  flex: 1;
  padding: 4px 8px;
}

.param-value {
  width: 40px;
  text-align: right;
  font-size: 12px;
  color: #909399;
}

.btn-variable {
  padding: 2px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 3px;
  background: white;
  font-size: 11px;
  color: #909399;
  cursor: pointer;
}

.btn-variable:hover {
  border-color: #409eff;
  color: #409eff;
}

.validation-message {
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  margin-top: 12px;
}

.validation-message.error {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
}

.validation-message.warning {
  background: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #faecd8;
}
</style>
