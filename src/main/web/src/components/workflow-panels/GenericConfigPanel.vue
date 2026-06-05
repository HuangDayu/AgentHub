<template>
  <div class="generic-config-panel">
    <!-- 节点参数字段 -->
    <div v-for="(fieldConfig, key) in schemaFields" :key="key" class="form-group">
      <label>{{ fieldConfig.label }}</label>
      <!-- 布尔值 -->
      <select v-if="fieldConfig.type === 'boolean'" v-model="localConfig[key]" @change="emitUpdate">
        <option :value="true">是</option>
        <option :value="false">否</option>
      </select>
      <!-- 数字 -->
      <input v-else-if="fieldConfig.type === 'number'"
        type="number"
        v-model.number="localConfig[key]"
        :min="fieldConfig.min"
        :max="fieldConfig.max"
        :step="fieldConfig.step || 1"
        :placeholder="fieldConfig.placeholder"
        @input="emitUpdate"
      />
      <!-- 对象/数组 → JSON文本域 -->
      <textarea v-else-if="fieldConfig.type === 'object' || fieldConfig.type === 'array'"
        :value="jsonStrings[key] || ''"
        rows="4"
        @input="onJsonInput(key, ($event.target as HTMLTextAreaElement).value)"
        :placeholder="fieldConfig.placeholder || 'JSON格式'"
        class="json-input"
      ></textarea>
      <!-- 选择框 -->
      <select v-else-if="fieldConfig.options" v-model="localConfig[key]" @change="emitUpdate">
        <option v-for="opt in fieldConfig.options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
      </select>
      <!-- 文本 -->
      <input v-else
        v-model="localConfig[key]"
        :placeholder="fieldConfig.placeholder"
        @input="emitUpdate"
      />
      <span v-if="fieldConfig.desc" class="field-desc">{{ fieldConfig.desc }}</span>
    </div>

    <!-- 输入参数 -->
    <h4 v-if="nodeData.input_params && nodeData.input_params.length > 0" class="section-title">输入参数</h4>
    <div v-for="(param, idx) in nodeData.input_params" :key="'in-' + idx" class="param-row">
      <span class="param-key">{{ param.key }}</span>
      <span class="param-type">{{ param.type }}</span>
      <input v-model="param.value" :placeholder="param.desc || param.key" @input="emitUpdate" />
    </div>

    <!-- 输出参数 -->
    <h4 v-if="nodeData.output_params && nodeData.output_params.length > 0" class="section-title">输出参数</h4>
    <div v-for="(param, idx) in nodeData.output_params" :key="'out-' + idx" class="param-row">
      <span class="param-key">{{ param.key }}</span>
      <span class="param-type">{{ param.type }}</span>
      <span class="param-desc">{{ param.desc }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch, computed } from 'vue'
import { NODE_SCHEMA_MAP } from '@/constants/node-schemas'

const props = defineProps<{ node: any }>()
const emit = defineEmits<{ update: [updates: any] }>()

const nodeData = props.node.data
const nodeType = props.node.type
const schema = NODE_SCHEMA_MAP[nodeType]
const defaultNodeParam = schema?.defaultParams?.node_param || {}

// 解析schema的defaultParams.node_param字段描述，用于生成表单
interface FieldConfig {
  label: string
  type: string
  placeholder?: string
  desc?: string
  options?: { label: string; value: string }[]
  min?: number
  max?: number
  step?: number
}

interface FieldRange {
  min: number
  max: number
  step?: number
  desc?: string
}

const FIELD_LABELS: Record<string, string> = {
  url: '请求URL',
  method: '请求方法',
  body: '请求体',
  timeoutMs: '超时时间(ms)',
  timeout: '超时时间(秒)',
  agentId: 'Agent ID',
  prompt: '提示词模板',
  streaming: '流式响应',
  script: '脚本代码',
  knowledgeBaseId: '知识库ID',
  query: '检索查询',
  topK: '检索数量(Top-K)',
  scoreThreshold: '分数阈值',
  retrievalType: '检索类型',
  processMode: '处理模式',
  includeMetadata: '包含元数据',
  includeScores: '包含分数',
  separator: '分隔符',
  outputVariable: '输出变量名',
  branches: '分支配置',
  items: '循环项表达式',
  maxIterations: '最大迭代次数',
  assignments: '变量赋值列表',
  toolName: '工具名称',
  parameters: '工具参数',
  subWorkflowId: '子工作流ID',
  inputMapping: '输入映射(JSON)',
  outputMapping: '输出映射(JSON)',
  concurrency: '并发数',
  nodes: '并行子节点',
}

const SELECT_OPTIONS: Record<string, { label: string; value: string }[]> = {
  method: [
    { label: 'GET', value: 'GET' },
    { label: 'POST', value: 'POST' },
    { label: 'PUT', value: 'PUT' },
    { label: 'DELETE', value: 'DELETE' },
  ],
  retrievalType: [
    { label: '向量相似度', value: 'similarity' },
    { label: '混合检索', value: 'hybrid' },
  ],
  processMode: [
    { label: '列表', value: 'list' },
    { label: '拼接', value: 'concat' },
    { label: '结构化', value: 'structured' },
  ],
}

const SELECT_DESCRIPTIONS: Record<string, string> = {
  method: '支持 GET/POST/PUT/DELETE',
  retrievalType: 'hybrid 模式同时使用向量检索和全文检索',
  processMode: '列表模式返回文档数组，拼接模式合并为文本，结构化模式含完整元数据',
}

const BOOLEAN_KEYS = new Set(['streaming', 'includeMetadata', 'includeScores'])

const NUMBER_RANGES: Record<string, FieldRange> = {
  timeoutMs: { min: 1000, max: 300000, desc: '毫秒，默认30000' },
  timeout: { min: 1, max: 3600, desc: '秒，默认300' },
  topK: { min: 1, max: 50 },
  scoreThreshold: { min: 0, max: 1, step: 0.05 },
  maxIterations: { min: 1, max: 10000, desc: '最大10000次' },
  concurrency: { min: 1, max: 100, desc: '默认4' },
}

function inferFieldConfig(key: string, defaultValue: unknown): FieldConfig {
  const type = typeof defaultValue
  const config = baseFieldConfig(key, type)
  if (!tryApplySelect(config, key) && !tryApplyBoolean(config, key)) {
    if (!tryApplyNumber(config, key, type)) {
      applyObjectFallback(config, type, defaultValue)
    }
  }
  return config
}

function baseFieldConfig(key: string, type: string): FieldConfig {
  const label = FIELD_LABELS[key] || key
  return { label, type, placeholder: `输入${label}` }
}

function tryApplySelect(config: FieldConfig, key: string): boolean {
  const options = SELECT_OPTIONS[key]
  if (!options) return false
  config.type = 'select'
  config.options = options
  config.desc = SELECT_DESCRIPTIONS[key]
  return true
}

function tryApplyBoolean(config: FieldConfig, key: string): boolean {
  if (!BOOLEAN_KEYS.has(key)) return false
  config.type = 'boolean'
  return true
}

function tryApplyNumber(config: FieldConfig, key: string, type: string): boolean {
  if (type !== 'number' && type !== 'bigint') return false
  config.type = 'number'
  applyNumberRange(config, NUMBER_RANGES[key])
  return true
}

function applyNumberRange(config: FieldConfig, range: FieldRange | undefined): void {
  if (!range) return
  config.min = range.min
  config.max = range.max
  config.step = range.step
  config.desc = range.desc
}

function applyObjectFallback(config: FieldConfig, type: string, value: unknown): void {
  if (isObjectLikeType(type, value)) {
    config.type = 'object'
    config.placeholder = JSON.stringify(value || {}, null, 2)
  }
  if (isExpandableObject(value)) {
    config.type = 'object'
  }
}

function isObjectLikeType(type: string, value: unknown): boolean {
  return type === 'object' || type === 'undefined' || Array.isArray(value)
}

// 检查字段是否为可展开的嵌套对象
function isExpandableObject(val: unknown): boolean {
  return typeof val === 'object' && val !== null && !Array.isArray(val) && Object.keys(val).length > 0
}

// 生成字段配置
const schemaFields = computed(() => {
  const fields: Record<string, FieldConfig> = {}
  for (const [key, defaultValue] of Object.entries(defaultNodeParam)) {
    fields[key] = inferFieldConfig(key, defaultValue)
  }
  return fields
})

// 本地配置
const localConfig: Record<string, any> = reactive(JSON.parse(JSON.stringify(defaultNodeParam)))

// JSON字段的字符串表示（用于textarea）
const jsonStrings: Record<string, string> = reactive({})

function initJsonStrings() {
  for (const [key, val] of Object.entries(localConfig)) {
    if (typeof val === 'object' || Array.isArray(val)) {
      jsonStrings[key] = JSON.stringify(val, null, 2)
    }
  }
}
initJsonStrings()

function onJsonInput(key: string, raw: string) {
  jsonStrings[key] = raw
  try {
    const parsed = JSON.parse(raw)
    localConfig[key] = parsed
    emitUpdate()
  } catch {
    // JSON语法错误时不更新localConfig
  }
}

watch(() => props.node, (val) => {
  const np = val.data?.node_param || {}
  for (const key of Object.keys(defaultNodeParam)) { if (key in np) syncConfigKey(key, np[key]) }
}, { deep: true })

function syncConfigKey(key: string, value: any) {
  ;(localConfig as any)[key] = value
  if (typeof value === 'object' || Array.isArray(value)) jsonStrings[key] = JSON.stringify(value, null, 2)
}

function emitUpdate() {
  emit('update', { node_param: { ...localConfig } })
}
</script>

<style scoped>
.generic-config-panel { padding: 0; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 12px; font-weight: 600; color: #666; margin-bottom: 6px; }
.form-group select,
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--color-border-strong);
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}
.form-group textarea { resize: vertical; font-family: monospace; font-size: 12px; }
.json-input { min-height: 80px; }
.field-desc { display: block; font-size: 11px; color: #999; margin-top: 4px; }
.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin: 16px 0 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid var(--bg-stripe);
}
.param-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 12px;
}
.param-key { font-weight: 600; color: #333; min-width: 80px; }
.param-type { color: #999; font-size: 11px; min-width: 60px; }
.param-desc { color: #666; font-size: 11px; }
.param-row input { flex: 1; padding: 6px 8px; border: 1px solid var(--color-border-strong); border-radius: 4px; font-size: 12px; }
</style>
