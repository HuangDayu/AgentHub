<template>
  <div class="script-node" @dblclick="$emit('edit', id)">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">📜</div>
      <div class="node-info">
        <div class="node-label">{{ data.label }}</div>
        <div v-if="data.node_param?.language" class="node-detail">
          {{ data.node_param.language }}
        </div>
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
.script-node {
  background: linear-gradient(135deg, var(--color-purple-subtle), #efdbff);
  border: 2px solid var(--color-purple);
  border-radius: 8px;
  padding: 12px 16px;
  min-width: 150px;
  box-shadow: 0 4px 12px rgba(114, 46, 209, 0.15);
  cursor: pointer;
  transition: all 0.2s;
}
.script-node:hover {
  box-shadow: 0 6px 16px rgba(114, 46, 209, 0.25);
  transform: translateY(-2px);
}
.node-content {
  display: flex;
  align-items: center;
  gap: 10px;
}
.node-icon { font-size: 20px; }
.node-info { flex: 1; }
.node-label { font-size: 14px; font-weight: 600; color: var(--color-text); }
.node-detail { font-size: 11px; color: #666; margin-top: 2px; }
</style>
