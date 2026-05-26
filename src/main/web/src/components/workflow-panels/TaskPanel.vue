<template>
  <div class="task-panel">
    <p class="hint">任务节点配置</p>
    <div class="form-group">
      <label>任务类型</label>
      <select v-model="localConfig.task_type" @change="emitUpdate">
        <option value="agent">Agent执行</option>
        <option value="tool">工具调用</option>
        <option value="notification">通知发送</option>
      </select>
    </div>
    <div class="form-group">
      <label>配置 (JSON)</label>
      <textarea v-model="configStr" rows="8" @input="updateConfig" style="font-family: monospace; font-size: 12px;"></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'

const props = defineProps<{ node: any }>()
const emit = defineEmits<{ update: [updates: any] }>()

const localConfig = reactive({ task_type: 'agent', ...(props.node.data?.node_param || {}) })
const configStr = ref(JSON.stringify(localConfig, null, 2))

watch(() => props.node, (val) => {
  const np = val.data?.node_param || {}
  Object.assign(localConfig, np)
  configStr.value = JSON.stringify(localConfig, null, 2)
}, { deep: true })

function updateConfig() {
  try {
    const parsed = JSON.parse(configStr.value)
    Object.assign(localConfig, parsed)
    emitUpdate()
  } catch { /* ignore */ }
}

function emitUpdate() {
  emit('update', { node_param: { ...localConfig } })
}
</script>

<style scoped>
.task-panel { padding: 0; }
.hint { font-size: 12px; color: #999; margin-bottom: 12px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 12px; font-weight: 600; color: #666; margin-bottom: 6px; }
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--color-border-strong);
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}
</style>
