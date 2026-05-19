<template>
  <div class="api-panel">
    <div class="panel-header">
      <h3>API节点配置</h3>
    </div>
    
    <div class="panel-body">
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="config.label" type="text" class="form-control" placeholder="输入节点名称" />
      </div>
      
      <div class="form-group">
        <label>请求方法</label>
        <select v-model="config.method" class="form-control">
          <option value="GET">GET</option>
          <option value="POST">POST</option>
          <option value="PUT">PUT</option>
          <option value="DELETE">DELETE</option>
          <option value="PATCH">PATCH</option>
        </select>
      </div>
      
      <div class="form-group">
        <label>URL</label>
        <input v-model="config.url" type="text" class="form-control" placeholder="输入API URL，可使用{{变量名}}" />
      </div>
      
      <div class="form-group">
        <label>请求头 (JSON格式)</label>
        <textarea v-model="config.headers" class="form-control" rows="4" placeholder='{"Content-Type": "application/json"}'></textarea>
      </div>
      
      <div class="form-group">
        <label>请求体 (JSON格式)</label>
        <textarea v-model="config.body" class="form-control" rows="6" placeholder='{"key": "value"}'></textarea>
      </div>
      
      <div class="form-group">
        <label>超时时间 (毫秒)</label>
        <input v-model.number="config.timeout" type="number" class="form-control" min="1000" max="60000" />
      </div>
      
      <div class="form-group">
        <label>
          <input v-model="config.retry" type="checkbox" />
          失败重试
        </label>
      </div>
      
      <div v-if="config.retry" class="form-group">
        <label>重试次数</label>
        <input v-model.number="config.retryCount" type="number" class="form-control" min="1" max="5" />
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
  method: 'GET',
  url: '',
  headers: '{}',
  body: '{}',
  timeout: 30000,
  retry: false,
  retryCount: 3,
  ...props.nodeData?.config
})

watch(() => props.nodeData, (newData) => {
  if (newData) {
    config.value = {
      label: newData.label || '',
      method: 'GET',
      url: '',
      headers: '{}',
      body: '{}',
      timeout: 30000,
      retry: false,
      retryCount: 3,
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
.api-panel {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.panel-header {
  padding: 16px;
  background: #17a2b8;
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
  border-color: #17a2b8;
  box-shadow: 0 0 0 2px rgba(23, 162, 184, 0.1);
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
  background: #17a2b8;
  color: white;
}

.btn-primary:hover {
  background: #0c5460;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}
</style>
