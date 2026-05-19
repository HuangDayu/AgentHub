<template>
  <BaseNode
    node-type="end"
    :label="data.label || '结束'"
    icon="⏹"
    :selected="selected"
    :status="data.status"
    :show-output-handle="false"
    @select="$emit('select')"
  >
    <div class="end-content">
      <div v-if="data.config?.outputParams?.length" class="output-params">
        <div class="param-label">输出参数:</div>
        <div class="param-list">
          <span 
            v-for="param in data.config.outputParams" 
            :key="param.name"
            class="param-tag"
          >
            {{ param.name }}
          </span>
        </div>
      </div>
    </div>
  </BaseNode>
</template>

<script setup lang="ts">
import BaseNode from './BaseNode.vue'
import type { WorkflowNodeData } from '@/types/workflow-node'

const props = defineProps<{
  data: WorkflowNodeData
  selected?: boolean
}>()

defineEmits<{
  'select': []
}>()
</script>

<style scoped>
.end-content {
  min-height: 20px;
}

.output-params {
  margin-top: 4px;
}

.param-label {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.param-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.param-tag {
  padding: 2px 6px;
  background: #fef0f0;
  color: #f56c6c;
  font-size: 11px;
  border-radius: 3px;
}
</style>
