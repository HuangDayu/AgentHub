<template>
  <div class="tool-panel">
    <div class="panel-header">
      <h3>工具节点配置</h3>
    </div>

    <div class="panel-body">
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="config.label" type="text" class="form-control" placeholder="输入节点名称" />
      </div>

      <div class="form-group">
        <label>工具类型</label>
        <select v-model="config.toolType" class="form-control">
          <option value="">请选择工具类型</option>
          <option value="mcp">MCP工具</option>
          <option value="system">系统工具</option>
          <option value="custom">自定义工具</option>
        </select>
      </div>

      <div v-if="config.toolType === 'mcp'" class="form-group">
        <label>MCP工具选择</label>
        <select v-model="config.toolId" class="form-control">
          <option value="">请选择MCP工具</option>
          <option v-for="tool in mcpTools" :key="tool.id" :value="tool.id">
            {{ tool.name }} - {{ tool.description }}
          </option>
        </select>
      </div>

      <div v-if="config.toolType === 'system'" class="form-group">
        <label>系统工具选择</label>
        <select v-model="config.toolId" class="form-control">
          <option value="">请选择系统工具</option>
          <option value="http_request">HTTP请求</option>
          <option value="file_read">文件读取</option>
          <option value="file_write">文件写入</option>
          <option value="json_parse">JSON解析</option>
          <option value="text_process">文本处理</option>
          <option value="data_transform">数据转换</option>
        </select>
      </div>

      <div v-if="config.toolType === 'custom'" class="form-group">
        <label>自定义工具代码</label>
        <textarea v-model="config.customCode" class="form-control" rows="8" placeholder="输入自定义工具代码（JavaScript）"></textarea>
      </div>

      <div class="form-group">
        <label>输入参数配置</label>
        <textarea v-model="config.inputParams" class="form-control" rows="4" placeholder='{"param1": "${variable1}", "param2": "value2"}'></textarea>
        <small class="form-text">JSON格式，支持变量引用 ${变量名}</small>
      </div>

      <div class="form-group">
        <label>输出变量映射</label>
        <textarea v-model="config.outputMapping" class="form-control" rows="3" placeholder='{"result": "outputVar", "data": "dataVar"}'></textarea>
        <small class="form-text">将工具输出映射到工作流变量</small>
      </div>

      <div class="form-group">
        <label>超时时间（秒）</label>
        <input v-model.number="config.timeout" type="number" class="form-control" min="1" placeholder="工具执行超时时间" />
      </div>

      <div class="form-group">
        <label>重试次数</label>
        <input v-model.number="config.retryCount" type="number" class="form-control" min="0" max="5" placeholder="失败后重试次数" />
      </div>

      <div class="form-group">
        <label>
          <input v-model="config.async" type="checkbox" />
          异步执行
        </label>
        <small class="form-text">启用后，工具将在后台异步执行</small>
      </div>

      <div class="form-group">
        <label>错误处理</label>
        <select v-model="config.errorHandling" class="form-control">
          <option value="throw">抛出异常（停止工作流）</option>
          <option value="default">使用默认值</option>
          <option value="ignore">忽略错误继续执行</option>
        </select>
      </div>

      <div v-if="config.errorHandling === 'default'" class="form-group">
        <label>默认返回值</label>
        <textarea v-model="config.defaultValue" class="form-control" rows="3" placeholder='{"result": "default value"}'></textarea>
      </div>
    </div>

    <div class="panel-footer">
      <button @click="handleCancel" class="btn btn-secondary">取消</button>
      <button @click="handleSave" class="btn btn-primary">保存</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

interface ToolConfig {
  label: string
  toolType: 'mcp' | 'system' | 'custom' | ''
  toolId: string
  customCode: string
  inputParams: string
  outputMapping: string
  timeout: number
  retryCount: number
  async: boolean
  errorHandling: 'throw' | 'default' | 'ignore'
  defaultValue: string
}

interface McpTool {
  id: string
  name: string
  description: string
}

const props = defineProps<{
  nodeData?: any
}>()

const emit = defineEmits<{
  save: [config: ToolConfig]
  cancel: []
}>()

const mcpTools = ref<McpTool[]>([])

const config = ref<ToolConfig>({
  label: props.nodeData?.label || '工具节点',
  toolType: props.nodeData?.data?.toolType || '',
  toolId: props.nodeData?.data?.toolId || '',
  customCode: props.nodeData?.data?.customCode || '',
  inputParams: props.nodeData?.data?.inputParams || '{}',
  outputMapping: props.nodeData?.data?.outputMapping || '{}',
  timeout: props.nodeData?.data?.timeout || 60,
  retryCount: props.nodeData?.data?.retryCount || 0,
  async: props.nodeData?.data?.async || false,
  errorHandling: props.nodeData?.data?.errorHandling || 'throw',
  defaultValue: props.nodeData?.data?.defaultValue || '{}'
})

// 加载MCP工具列表
onMounted(async () => {
  try {
    // TODO: 从API加载MCP工具列表
    mcpTools.value = [
      { id: 'web_search', name: '网页搜索', description: '搜索互联网信息' },
      { id: 'code_execute', name: '代码执行', description: '执行代码片段' },
      { id: 'database_query', name: '数据库查询', description: '执行SQL查询' }
    ]
  } catch (error) {
    console.error('Failed to load MCP tools:', error)
  }
})

watch(() => props.nodeData, (newData) => {
  if (newData) {
    config.value = {
      label: newData.label || '工具节点',
      toolType: newData.data?.toolType || '',
      toolId: newData.data?.toolId || '',
      customCode: newData.data?.customCode || '',
      inputParams: newData.data?.inputParams || '{}',
      outputMapping: newData.data?.outputMapping || '{}',
      timeout: newData.data?.timeout || 60,
      retryCount: newData.data?.retryCount || 0,
      async: newData.data?.async || false,
      errorHandling: newData.data?.errorHandling || 'throw',
      defaultValue: newData.data?.defaultValue || '{}'
    }
  }
}, { deep: true })

const handleSave = () => {
  emit('save', config.value)
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.tool-panel {
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
  border-color: #fd7e14;
  box-shadow: 0 0 0 2px rgba(253, 126, 20, 0.1);
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
  background: #fd7e14;
  color: white;
}

.btn-primary:hover {
  background: #e8590c;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}
</style>
