<template>
  <div class="start-node">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">▶</div>
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
.start-node {
  background: linear-gradient(135deg, #27ae60, #2ecc71);
  border: 2px solid #229954;
  border-radius: 8px;
  padding: 10px 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(39, 174, 96, 0.3);
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
