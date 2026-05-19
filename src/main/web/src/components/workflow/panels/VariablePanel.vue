<template>
  <div class="variable-panel">
    <div class="panel-header">
      <h3>变量节点配置</h3>
    </div>

    <div class="panel-body">
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="config.label" type="text" class="form-control" placeholder="输入节点名称" />
      </div>

      <div class="form-group">
        <label>操作类型</label>
        <select v-model="config.operation" class="form-control">
          <option value="set">设置变量</option>
          <option value="update">更新变量</option>
          <option value="delete">删除变量</option>
          <option value="merge">合并变量</option>
        </select>
      </div>

      <div v-if="config.operation !== 'delete'" class="form-group">
        <label>变量定义</label>
        <div class="variable-list">
          <div v-for="(variable, index) in config.variables" :key="index" class="variable-item">
            <input v-model="variable.name" type="text" class="form-control var-name" placeholder="变量名" />
            <select v-model="variable.type" class="form-control var-type">
              <option value="string">字符串</option>
              <option value="number">数字</option>
              <option value="boolean">布尔值</option>
              <option value="object">对象</option>
              <option value="array">数组</option>
              <option value="expression">表达式</option>
            </select>
            <input v-model="variable.value" type="text" class="form-control var-value" placeholder="变量值" />
            <button @click="removeVariable(index)" class="btn-remove">×</button>
          </div>
          <button @click="addVariable" class="btn-add">+ 添加变量</button>
        </div>
      </div>

      <div v-if="config.operation === 'delete'" class="form-group">
        <label>要删除的变量</label>
        <textarea v-model="config.variablesToDelete" class="form-control" rows="3" placeholder="变量名列表，逗号分隔"></textarea>
      </div>

      <div v-if="config.operation === 'merge'" class="form-group">
        <label>合并策略</label>
        <select v-model="config.mergeStrategy" class="form-control">
          <option value="deep">深度合并</option>
          <option value="shallow">浅层合并</option>
          <option value="replace">替换合并</option>
        </select>
      </div>

      <div class="form-group">
        <label>作用域</label>
        <select v-model="config.scope" class="form-control">
          <option value="local">局部变量（当前工作流）</option>
          <option value="global">全局变量（跨工作流）</option>
          <option value="session">会话变量（当前会话）</option>
        </select>
      </div>

      <div class="form-group">
        <label>
          <input v-model="config.persist" type="checkbox" />
          持久化变量
        </label>
        <small class="form-text">启用后，变量将在工作流执行完成后保留</small>
      </div>

      <div class="form-group">
        <label>变量说明</label>
        <textarea v-model="config.description" class="form-control" rows="2" placeholder="描述这些变量的用途"></textarea>
      </div>
    </div>

    <div class="panel-footer">
      <button @click="handleCancel" class="btn btn-secondary">取消</button>
      <button @click="handleSave" class="btn btn-primary">保存</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Variable {
  name: string
  type: 'string' | 'number' | 'boolean' | 'object' | 'array' | 'expression'
  value: string
}

interface VariableConfig {
  label: string
  operation: 'set' | 'update' | 'delete' | 'merge'
  variables: Variable[]
  variablesToDelete: string
  mergeStrategy: 'deep' | 'shallow' | 'replace'
  scope: 'local' | 'global' | 'session'
  persist: boolean
  description: string
}

const props = defineProps<{
  nodeData?: any
}>()

const emit = defineEmits<{
  save: [config: VariableConfig]
  cancel: []
}>()

const config = ref<VariableConfig>({
  label: props.nodeData?.label || '变量节点',
  operation: props.nodeData?.data?.operation || 'set',
  variables: props.nodeData?.data?.variables || [{ name: '', type: 'string', value: '' }],
  variablesToDelete: props.nodeData?.data?.variablesToDelete || '',
  mergeStrategy: props.nodeData?.data?.mergeStrategy || 'deep',
  scope: props.nodeData?.data?.scope || 'local',
  persist: props.nodeData?.data?.persist || false,
  description: props.nodeData?.data?.description || ''
})

watch(() => props.nodeData, (newData) => {
  if (newData) {
    config.value = {
      label: newData.label || '变量节点',
      operation: newData.data?.operation || 'set',
      variables: newData.data?.variables || [{ name: '', type: 'string', value: '' }],
      variablesToDelete: newData.data?.variablesToDelete || '',
      mergeStrategy: newData.data?.mergeStrategy || 'deep',
      scope: newData.data?.scope || 'local',
      persist: newData.data?.persist || false,
      description: newData.data?.description || ''
    }
  }
}, { deep: true })

const addVariable = () => {
  config.value.variables.push({ name: '', type: 'string', value: '' })
}

const removeVariable = (index: number) => {
  if (config.value.variables.length > 1) {
    config.value.variables.splice(index, 1)
  }
}

const handleSave = () => {
  emit('save', config.value)
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.variable-panel {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #ddd;
  background: #f8f9fa;
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
  border-color: #20c997;
  box-shadow: 0 0 0 2px rgba(32, 201, 151, 0.1);
}

textarea.form-control {
  resize: vertical;
  font-family: 'Monaco', 'Menlo', monospace;
}

.form-text {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #6c757d;
}

.variable-list {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px;
}

.variable-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.var-name {
  flex: 1;
}

.var-type {
  width: 120px;
}

.var-value {
  flex: 2;
}

.btn-remove {
  width: 32px;
  height: 32px;
  border: none;
  background: #dc3545;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
}

.btn-remove:hover {
  background: #c82333;
}

.btn-add {
  width: 100%;
  padding: 8px;
  border: 1px dashed #ddd;
  background: white;
  color: #6c757d;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-add:hover {
  border-color: #20c997;
  color: #20c997;
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
  background: #20c997;
  color: white;
}

.btn-primary:hover {
  background: #1aa179;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}
</style>
