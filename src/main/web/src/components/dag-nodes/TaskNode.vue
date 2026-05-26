<template>
  <div class="task-node" @dblclick="$emit('edit', id)">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">⚙</div>
      <div class="node-info">
        <div class="node-label">{{ data.label }}</div>
        <div v-if="data.description" class="node-description">{{ data.description }}</div>
      </div>
    </div>
    <Handle type="source" :position="Position.Right" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps<{
  data: any
  id: string
}>()

defineEmits<{
  (e: 'edit', id: string): void
}>()
</script>

<style scoped>
.task-node {
  background: var(--bg-card-solid);
  border: 2px solid var(--color-primary);
  border-radius: 8px;
  padding: 12px 16px;
  min-width: 150px;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.2);
  cursor: pointer;
  transition: all 0.2s;
}

.task-node:hover {
  box-shadow: 0 6px 16px rgba(52, 152, 219, 0.3);
  transform: translateY(-2px);
}

.node-content {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.node-icon {
  font-size: 20px;
  color: var(--color-primary);
}

.node-info {
  flex: 1;
}

.node-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 4px;
}

.node-description {
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
