<template>
  <div class="llm-panel">
    <div class="form-group">
      <label>Agent ID</label>
      <input v-model="localConfig.agentId" placeholder="输入Agent ID，默认为default" @input="emitUpdate" />
      <span class="field-desc">使用Agent ID调用AgentChatPort进行对话，留空使用default</span>
    </div>
    <div class="form-group">
      <label>提示词模板</label>
      <textarea v-model="localConfig.prompt" rows="5" placeholder="输入提示词模板，支持 {{变量名}} 引用上下文变量" @input="emitUpdate"></textarea>
      <span class="field-desc">使用 <code>{<!-- -->{变量名}}</code> 语法引用上下文中的变量</span>
    </div>
    <div class="form-group">
      <label class="checkbox-label">
        <input type="checkbox" v-model="localConfig.streaming" @change="emitUpdate" />
        流式响应
      </label>
      <span class="field-desc">开启后使用流式方式调用LLM</span>
    </div>
    <h4 class="section-title">输入参数</h4>
    <div v-for="(param, idx) in nodeData.input_params" :key="idx" class="param-row">
      <span class="param-key">{{ param.key }}</span>
      <span class="param-type">{{ param.type }}</span>
      <span class="param-desc">{{ param.description || param.desc }}</span>
    </div>
    <h4 class="section-title">输出参数</h4>
    <div v-for="(param, idx) in nodeData.output_params" :key="idx" class="param-row">
      <span class="param-key">{{ param.key }}</span>
      <span class="param-type">{{ param.type }}</span>
      <span class="param-desc">{{ param.description || param.desc }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'

const props = defineProps<{
  node: any
}>()

const emit = defineEmits<{
  update: [updates: any]
}>()

const nodeData = props.node.data
const localConfig = reactive({ ...(nodeData.node_param || {}) })

watch(() => props.node, (val) => {
  Object.assign(localConfig, val.data?.node_param || {})
}, { deep: true })

function emitUpdate() {
  emit('update', { node_param: { ...localConfig } })
}
</script>

<style scoped>
.llm-panel { padding: 0; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 12px; font-weight: 600; color: #666; margin-bottom: 6px; }
.form-group select,
.form-group input[type="text"],
.form-group input[type="number"],
.form-group textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}
.form-group textarea { resize: vertical; font-family: inherit; }
.field-desc { display: block; font-size: 11px; color: #999; margin-top: 4px; }
.checkbox-label { display: flex; align-items: center; gap: 8px; cursor: pointer; font-weight: 400 !important; }
.checkbox-label input[type="checkbox"] { width: auto; }
.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin: 16px 0 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid #f0f0f0;
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
</style>
