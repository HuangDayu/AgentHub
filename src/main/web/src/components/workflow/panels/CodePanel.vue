<template>
  <div class="code-panel">
    <div class="panel-header">
      <h3>代码节点配置</h3>
    </div>

    <div class="panel-body">
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="config.label" type="text" class="form-control" placeholder="输入节点名称" />
      </div>

      <div class="form-group">
        <label>编程语言</label>
        <select v-model="config.language" class="form-control">
          <option value="javascript">JavaScript</option>
          <option value="python">Python</option>
          <option value="groovy">Groovy</option>
        </select>
      </div>

      <div class="form-group">
        <label>代码内容</label>
        <textarea v-model="config.code" class="form-control code-editor" rows="12" placeholder="输入代码内容"></textarea>
        <small class="form-text">
          可用变量：<code>context</code>（上下文）、<code>input</code>（输入）、<code>output</code>（输出）
        </small>
      </div>

      <div class="form-group">
        <label>输入变量</label>
        <textarea v-model="config.inputVariables" class="form-control" rows="3" placeholder='["var1", "var2", "var3"]'></textarea>
        <small class="form-text">JSON数组格式，定义从工作流上下文传入的变量</small>
      </div>

      <div class="form-group">
        <label>输出变量</label>
        <textarea v-model="config.outputVariables" class="form-control" rows="3" placeholder='{"result": "outputVar"}'></textarea>
        <small class="form-text">JSON格式，将代码输出映射到工作流变量</small>
      </div>

      <div class="form-group">
        <label>执行环境</label>
        <select v-model="config.environment" class="form-control">
          <option value="sandbox">沙箱环境（安全隔离）</option>
          <option value="native">原生环境（完全权限）</option>
          <option value="docker">Docker容器（隔离且可控）</option>
        </select>
      </div>

      <div class="form-group">
        <label>超时时间（秒）</label>
        <input v-model.number="config.timeout" type="number" class="form-control" min="1" placeholder="代码执行超时时间" />
      </div>

      <div class="form-group">
        <label>内存限制（MB）</label>
        <input v-model.number="config.memoryLimit" type="number" class="form-control" min="64" placeholder="执行内存限制" />
      </div>

      <div class="form-group">
        <label>
          <input v-model="config.enableLogging" type="checkbox" />
          启用日志输出
        </label>
        <small class="form-text">启用后，console.log输出将被捕获并记录</small>
      </div>

      <div class="form-group">
        <label>依赖包</label>
        <textarea v-model="config.dependencies" class="form-control" rows="2" placeholder='["lodash", "axios"]'></textarea>
        <small class="form-text">JSON数组格式，定义需要引入的外部依赖包</small>
      </div>

      <div class="form-group">
        <label>错误处理</label>
        <select v-model="config.errorHandling" class="form-control">
          <option value="throw">抛出异常（停止工作流）</option>
          <option value="return">返回错误信息</option>
          <option value="ignore">忽略错误继续执行</option>
        </select>
      </div>
    </div>

    <div class="panel-footer">
      <button @click="handleTest" class="btn btn-info">测试</button>
      <button @click="handleCancel" class="btn btn-secondary">取消</button>
      <button @click="handleSave" class="btn btn-primary">保存</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface CodeConfig {
  label: string
  language: 'javascript' | 'python' | 'groovy'
  code: string
  inputVariables: string
  outputVariables: string
  environment: 'sandbox' | 'native' | 'docker'
  timeout: number
  memoryLimit: number
  enableLogging: boolean
  dependencies: string
  errorHandling: 'throw' | 'return' | 'ignore'
}

const props = defineProps<{
  nodeData?: any
}>()

const emit = defineEmits<{
  save: [config: CodeConfig]
  cancel: []
  test: [config: CodeConfig]
}>()

const config = ref<CodeConfig>({
  label: props.nodeData?.label || '代码节点',
  language: props.nodeData?.data?.language || 'javascript',
  code: props.nodeData?.data?.code || '// 在这里编写代码\nconst result = input.data;\noutput.result = result;',
  inputVariables: props.nodeData?.data?.inputVariables || '[]',
  outputVariables: props.nodeData?.data?.outputVariables || '{}',
  environment: props.nodeData?.data?.environment || 'sandbox',
  timeout: props.nodeData?.data?.timeout || 30,
  memoryLimit: props.nodeData?.data?.memoryLimit || 256,
  enableLogging: props.nodeData?.data?.enableLogging || true,
  dependencies: props.nodeData?.data?.dependencies || '[]',
  errorHandling: props.nodeData?.data?.errorHandling || 'throw'
})

watch(() => props.nodeData, (newData) => {
  if (newData) {
    config.value = {
      label: newData.label || '代码节点',
      language: newData.data?.language || 'javascript',
      code: newData.data?.code || '// 在这里编写代码\nconst result = input.data;\noutput.result = result;',
      inputVariables: newData.data?.inputVariables || '[]',
      outputVariables: newData.data?.outputVariables || '{}',
      environment: newData.data?.environment || 'sandbox',
      timeout: newData.data?.timeout || 30,
      memoryLimit: newData.data?.memoryLimit || 256,
      enableLogging: newData.data?.enableLogging || true,
      dependencies: newData.data?.dependencies || '[]',
      errorHandling: newData.data?.errorHandling || 'throw'
    }
  }
}, { deep: true })

const handleSave = () => {
  emit('save', config.value)
}

const handleCancel = () => {
  emit('cancel')
}

const handleTest = () => {
  emit('test', config.value)
}
</script>

<style scoped>
.code-panel {
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
  border-color: #e83e8c;
  box-shadow: 0 0 0 2px rgba(232, 62, 140, 0.1);
}

textarea.form-control {
  resize: vertical;
}

.code-editor {
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
  background: #f8f9fa;
}

.form-text {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #6c757d;
}

.form-text code {
  background: #e9ecef;
  padding: 2px 4px;
  border-radius: 3px;
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
  background: #e83e8c;
  color: white;
}

.btn-primary:hover {
  background: #d63384;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.btn-info {
  background: #17a2b8;
  color: white;
}

.btn-info:hover {
  background: #138496;
}
</style>
