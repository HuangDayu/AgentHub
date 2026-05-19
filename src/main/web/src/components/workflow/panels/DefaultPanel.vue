<template>
  <div class="default-panel">
    <div class="panel-header">
      <h3>{{ nodeData?.label || '节点' }}配置</h3>
    </div>
    
    <div class="panel-body">
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="config.label" type="text" class="form-control" placeholder="输入节点名称" />
      </div>
      
      <div class="form-group">
        <label>描述</label>
        <textarea v-model="config.description" class="form-control" rows="3" placeholder="输入节点描述"></textarea>
      </div>
      
      <div class="form-group">
        <label>
          <input v-model="config.disabled" type="checkbox" />
          禁用此节点
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
  description: '',
  disabled: false,
  ...props.nodeData?.config
})

watch(() => props.nodeData, (newData) => {
  if (newData) {
    config.value = {
      label: newData.label || '',
      description: '',
      disabled: false,
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
.default-panel {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.panel-header {
  padding: 16px;
  background: #6c757d;
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
  border-color: #6c757d;
  box-shadow: 0 0 0 2px rgba(108, 117, 125, 0.1);
}

textarea.form-control {
  resize: vertical;
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
  background: #6c757d;
  color: white;
}

.btn-primary:hover {
  background: #545b62;
}

.btn-secondary {
  background: #adb5bd;
  color: white;
}

.btn-secondary:hover {
  background: #868e96;
}
</style>
