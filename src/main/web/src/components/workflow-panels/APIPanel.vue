<template>
  <div class="api-panel">
    <div class="form-group">
      <label>请求方法</label>
      <CustomSelect v-model="localConfig.method" :options="methodOptions" @change="emitUpdate" />
    </div>
    <div class="form-group">
      <label>请求URL</label>
      <input v-model="localConfig.url" placeholder="https://api.example.com/endpoint" @input="emitUpdate" />
      <span class="field-desc">支持 {{变量名}} 引用上下文变量</span>
    </div>
    <div class="form-group">
      <label>请求体 (JSON)</label>
      <textarea :value="bodyStr" rows="6" @input="onBodyInput" placeholder='{"key": "value"}' class="json-input"></textarea>
      <span class="field-desc">POST/PUT 请求的JSON请求体</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import CustomSelect from '@/components/CustomSelect.vue'

const methodOptions = [
  { value: 'GET', label: 'GET' },
  { value: 'POST', label: 'POST' },
  { value: 'PUT', label: 'PUT' },
  { value: 'DELETE', label: 'DELETE' },
]

const props = defineProps<{ node: any }>()
const emit = defineEmits<{ update: [updates: any] }>()

const localConfig = reactive({ ...(props.node.data?.node_param || {}) })
const bodyStr = ref(JSON.stringify(localConfig.body || {}, null, 2))

watch(() => props.node, (val) => {
  Object.assign(localConfig, val.data?.node_param || {})
  bodyStr.value = JSON.stringify(localConfig.body || {}, null, 2)
}, { deep: true })

function onBodyInput(e: Event) {
  const raw = (e.target as HTMLTextAreaElement).value
  bodyStr.value = raw
  try {
    localConfig.body = JSON.parse(raw)
    emitUpdate()
  } catch { /* JSON parse error */ }
}

function emitUpdate() {
  emit('update', { node_param: { ...localConfig } })
}
</script>

<style scoped>
.api-panel { padding: 0; }
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
.json-input { min-height: 120px; }
.field-desc { display: block; font-size: 11px; color: #999; margin-top: 4px; }
</style>
