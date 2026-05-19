<template>
  <div class="llm-node">
    <Handle type="target" :position="Position.Left" />
    <div class="node-content">
      <div class="node-icon">🤖</div>
      <div class="node-label">LLM</div>
    </div>
    <Handle type="source" :position="Position.Right" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
</script>

<style scoped>
.llm-node {
  background: #007bff;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  border: 2px solid #0056b3;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-icon {
  font-size: 16px;
}

.node-label {
  font-weight: bold;
}
</style>
