<template>
  <BaseNode
    node-type="condition"
    :label="data.label || '条件'"
    icon="◇"
    :selected="selected"
    :status="data.status"
    @select="$emit('select')"
  >
    <div class="condition-content">
      <div v-if="conditions.length" class="conditions-list">
        <div 
          v-for="(condition, index) in conditions" 
          :key="index"
          class="condition-item"
        >
          <span class="condition-label">{{ condition.label || `分支${index + 1}` }}</span>
          <span class="condition-expr">{{ condition.expression }}</span>
        </div>
      </div>
      <div v-else class="empty-hint">
        点击配置条件分支
      </div>
    </div>
  </BaseNode>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseNode from './BaseNode.vue'
import type { WorkflowNodeData } from '@/types/workflow-node'

const props = defineProps<{
  data: WorkflowNodeData
  selected?: boolean
}>()

defineEmits<{
  'select': []
}>()

const conditions = computed(() => {
  return props.data.node_param?.conditions || []
})
</script>

<style scoped>
.condition-content {
  min-height: 20px;
}

.conditions-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.condition-item {
  display: flex;
  flex-direction: column;
  padding: 6px 8px;
  background: #fdf6ec;
  border-radius: 4px;
}

.condition-label {
  font-size: 11px;
  font-weight: 500;
  color: #e6a23c;
  margin-bottom: 2px;
}

.condition-expr {
  font-size: 11px;
  color: #606266;
  font-family: 'Monaco', 'Menlo', monospace;
}

.empty-hint {
  font-size: 11px;
  color: #c0c4cc;
  font-style: italic;
}
</style>
