<template>
  <div class="start-panel">
    <p class="hint">开始节点是工作流的入口，定义输入参数</p>
    <h4 class="section-title">输出参数</h4>
    <div v-for="(param, idx) in nodeData.output_params" :key="idx" class="param-row">
      <span class="param-key">{{ param.key }}</span>
      <span class="param-type">{{ param.type }}</span>
      <span class="param-desc">{{ param.desc }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ node: any }>()
const emit = defineEmits<{ update: [updates: any] }>()
const nodeData = props.node.data
</script>

<style scoped>
.start-panel { padding: 0; }
.hint { font-size: 12px; color: #999; margin-bottom: 12px; }
.section-title {
  font-size: 13px; font-weight: 600; color: #333;
  margin: 16px 0 8px; padding-bottom: 4px;
  border-bottom: 1px solid var(--bg-stripe);
}
.param-row {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 8px; font-size: 12px;
}
.param-key { font-weight: 600; color: #333; min-width: 80px; }
.param-type { color: #999; font-size: 11px; min-width: 60px; }
.param-desc { color: #666; font-size: 11px; }
</style>
