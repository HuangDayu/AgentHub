<template>
  <div class="api-node" @dblclick="$emit('edit', id)">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">🔗</div>
      <div class="node-info">
        <div class="node-label">{{ data.label }}</div>
        <div v-if="data.node_param?.url" class="node-detail" :title="data.node_param.url">
          {{ data.node_param.method }} {{ truncateUrl(data.node_param.url) }}
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

function truncateUrl(url: string): string {
  if (!url) return ''
  return url.length > 30 ? url.substring(0, 30) + '...' : url
}
</script>

<style scoped>
.api-node {
  background: linear-gradient(135deg, #fff7e6, #ffe7ba);
  border: 2px solid #fa8c16;
  border-radius: 8px;
  padding: 12px 16px;
  min-width: 180px;
  box-shadow: 0 4px 12px rgba(250, 140, 22, 0.15);
  cursor: pointer;
  transition: all 0.2s;
}
.api-node:hover {
  box-shadow: 0 6px 16px rgba(250, 140, 22, 0.25);
  transform: translateY(-2px);
}
.node-content {
  display: flex;
  align-items: center;
  gap: 10px;
}
.node-icon { font-size: 20px; }
.node-info { flex: 1; }
.node-label { font-size: 14px; font-weight: 600; color: #1a1a1a; }
.node-detail { font-size: 11px; color: #666; margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
