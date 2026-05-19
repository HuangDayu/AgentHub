<template>
  <div class="llm-panel">
    <div class="panel-header">
      <h3>LLM节点配置</h3>
    </div>
    
    <div class="panel-body">
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="config.label" type="text" class="form-control" placeholder="输入节点名称" />
      </div>
      
      <div class="form-group">
        <label>模型选择</label>
        <select v-model="config.model" class="form-control">
          <option value="">请选择模型</option>
          <option value="gpt-4">GPT-4</option>
          <option value="gpt-3.5-turbo">GPT-3.5 Turbo</option>
          <option value="claude-3">Claude 3</option>
          <option value="claude-2">Claude 2</option>
        </select>
      </div>
      
      <div class="form-group">
        <label>温度 (Temperature)</label>
        <input v-model.number="config.temperature" type="number" class="form-control" min="0" max="2" step="0.1" />
      </div>
      
      <div class="form-group">
        <label>最大Token数</label>
        <input v-model.number="config.maxTokens" type="number" class="form-control" min="1" max="100000" />
      </div>
      
      <div class="form-group">
        <label>系统提示词</label>
        <textarea v-model="config.systemPrompt" class="form-control" rows="4" placeholder="输入系统提示词"></textarea>
      </div>
      
      <div class="form-group">
        <label>用户提示词模板</label>
        <textarea v-model="config.userPrompt" class="form-control" rows="4" placeholder="输入用户提示词模板，可使用{{变量名}}引用变量"></textarea>
      </div>
      
      <div class="form-group">
        <label>
          <input v-model="config.stream" type="checkbox" />
          启用流式输出
        </label>
      </div>
    </div>
    
    <div class="panel-footer">
      <button class="btn btn-primary" @click="save">保存</button>
      <button class="btn btn-secondary" @click="cancel">取消</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  nodeData: any
}>()

const emit = defineEmits<{
  'save': [config: any]
  'cancel': []
}>()

const config = ref({
  label: '',
  model: '',
  temperature: 0.7,
  maxTokens: 2000,
  systemPrompt: '',
  userPrompt: '',
  stream: false,
  ...props.nodeData?.config
})

// 监听nodeData变化
watch(() => props.nodeData, (newData) => {
  if (newData) {
    config.value = {
      label: newData.label || '',
      model: '',
      temperature: 0.7,
      maxTokens: 2000,
      systemPrompt: '',
      userPrompt: '',
      stream: false,
      ...newData.config
    }
  }
}, { immediate: true })

function save() {
  emit('save', config.value)
}

function cancel() {
  emit('cancel')
}
</script>

<style scoped>
.llm-panel {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.panel-header {
  padding: 16px;
  background: #007bff;
  color: white;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.panel-body {
  padding: 16px;
  max-height: 500px;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-control:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.1);
}

textarea.form-control {
  resize: vertical;
  font-family: 'Monaco', 'Menlo', monospace;
}

.panel-footer {
  padding: 16px;
  border-top: 1px solid #ddd;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}
</style>
