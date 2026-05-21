<template>
  <div class="script-panel">
    <div class="form-group">
      <label>脚本代码</label>
      <textarea v-model="localConfig.script" rows="10" placeholder="// 编写JavaScript代码..."
        @input="emitUpdate" class="code-textarea"></textarea>
      <span class="field-desc">使用Java ScriptEngine(Nashorn)执行JavaScript代码</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'

const props = defineProps<{ node: any }>()
const emit = defineEmits<{ update: [updates: any] }>()

const localConfig = reactive({ ...(props.node.data?.node_param || {}) })

watch(() => props.node, (val) => {
  Object.assign(localConfig, val.data?.node_param || {})
}, { deep: true })

function emitUpdate() {
  emit('update', { node_param: { ...localConfig } })
}
</script>

<style scoped>
.script-panel { padding: 0; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 12px; font-weight: 600; color: #666; margin-bottom: 6px; }
.form-group textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}
.code-textarea { font-family: monospace; font-size: 12px; white-space: pre; resize: vertical; min-height: 200px; }
.field-desc { display: block; font-size: 11px; color: #999; margin-top: 4px; }
</style>
