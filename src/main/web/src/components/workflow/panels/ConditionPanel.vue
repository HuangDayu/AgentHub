<template>
  <div class="condition-panel">
    <div class="panel-header">
      <h3>条件节点配置</h3>
    </div>
    
    <div class="panel-body">
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="config.label" type="text" class="form-control" placeholder="输入节点名称" />
      </div>
      
      <div class="form-group">
        <label>条件表达式</label>
        <textarea v-model="config.expression" class="form-control" rows="4" placeholder="输入条件表达式，例如: {{变量名}} > 10"></textarea>
        <small class="form-text">支持JavaScript表达式，可使用{{变量名}}引用变量</small>
      </div>
      
      <div class="form-group">
        <label>条件类型</label>
        <select v-model="config.conditionType" class="form-control">
          <option value="simple">简单条件</option>
          <option value="expression">表达式</option>
          <option value="script">脚本</option>
        </select>
      </div>
      
      <div v-if="config.conditionType === 'simple'" class="condition-builder">
        <div class="condition-row">
          <select v-model="config.leftOperand" class="form-control">
            <option value="">选择变量</option>
            <option v-for="v in availableVariables" :key="v" :value="v">{{ v }}</option>
          </select>
          
          <select v-model="config.operator" class="form-control">
            <option value="==">等于</option>
            <option value="!=">不等于</option>
            <option value=">">大于</option>
            <option value=">=">大于等于</option>
            <option value="<">小于</option>
            <option value="<=">小于等于</option>
            <option value="contains">包含</option>
            <option value="notContains">不包含</option>
          </select>
          
          <input v-model="config.rightOperand" type="text" class="form-control" placeholder="值" />
        </div>
      </div>
      
      <div v-if="config.conditionType === 'script'" class="form-group">
        <label>脚本代码</label>
        <textarea v-model="config.script" class="form-control" rows="8" placeholder="输入JavaScript代码，返回true或false"></textarea>
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
  availableVariables?: string[]
}>()

const emit = defineEmits<{
  'save': [config: any]
  'cancel': []
}>()

const config = ref({
  label: '',
  expression: '',
  conditionType: 'simple',
  leftOperand: '',
  operator: '==',
  rightOperand: '',
  script: '',
  ...props.nodeData?.config
})

const availableVariables = ref(props.availableVariables || [])

watch(() => props.nodeData, (newData) => {
  if (newData) {
    config.value = {
      label: newData.label || '',
      expression: '',
      conditionType: 'simple',
      leftOperand: '',
      operator: '==',
      rightOperand: '',
      script: '',
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
.condition-panel {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.panel-header {
  padding: 16px;
  background: #ffc107;
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
  border-color: #ffc107;
  box-shadow: 0 0 0 2px rgba(255, 193, 7, 0.1);
}

textarea.form-control {
  resize: vertical;
  font-family: 'Monaco', 'Menlo', monospace;
}

.form-text {
  display: block;
  margin-top: 4px;
  color: #666;
  font-size: 12px;
}

.condition-builder {
  margin-top: 16px;
}

.condition-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.condition-row .form-control {
  flex: 1;
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
  background: #ffc107;
  color: white;
}

.btn-primary:hover {
  background: #d39e00;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}
</style>
