<template>
  <div class="end-node">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">⏹</div>
      <div class="node-label">{{ data.label }}</div>
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
</script>

<style scoped>
.end-node {
  background: linear-gradient(135deg, var(--color-error), var(--color-error-dark));
  border: 2px solid var(--color-error-dark);
  border-radius: 8px;
  padding: 10px 20px; color: var(--color-text-inverse);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
}

.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-icon {
  font-size: 18px;
}

.node-label {
  font-size: 14px;
  font-weight: 600;
}
</style>
