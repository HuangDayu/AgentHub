<template>
  <BaseNode
    node-type="start"
    :label="data.label || '开始'"
    icon="▶"
    :selected="selected"
    :status="data.status"
    :show-input-handle="false"
    @select="$emit('select')"
  >
    <div class="start-content">
      <div v-if="data.config?.inputParams?.length" class="input-params">
        <div class="param-label">输入参数:</div>
        <div class="param-list">
          <span 
            v-for="param in data.config.inputParams" 
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
.start-content {
  min-height: 20px;
}

.input-params {
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
  background: #f0f9ff;
  color: #409eff;
  font-size: 11px;
  border-radius: 3px;
}
</style>
