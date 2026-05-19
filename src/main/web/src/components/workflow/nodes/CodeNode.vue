<template>
  <div class="codenode" :class="{ selected: selected, disabled: data.disabled }">
    <Handle type="target" :position="Position.Left" id="input" />
    
    <div class="node-header">
      <div class="node-icon">💻</div>
      <div class="node-title">{{ data.label || '代码' }}</div>
      <button v-if="!data.disabled" class="config-btn" @click="openConfig">⚙️</button>
    </div>
    
    <div class="node-body">
      <div class="config-info">
        <div v-if="data.config?.description" class="info-item">
          <span class="value">{{ data.config.description }}</span>
        </div>
      </div>
    </div>
    
    <div v-if="data.status" class="node-status" :class="data.status">
      {{ statusText }}
    </div>
    
    <Handle type="source" :position="Position.Right" id="output" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'

const props = defineProps<{
  data: any
  selected?: boolean
}>()

const emit = defineEmits<{
  'config': [nodeId: string]
}>()

const statusText = computed(() => {
  switch (props.data.status) {
    case 'running': return '运行中'
    case 'success': return '成功'
    case 'error': return '错误'
    case 'waiting': return '等待'
    default: return ''
  }
})

function openConfig() {
  emit('config', props.data.id)
}
</script>

<style scoped>
.codenode {
  background: white;
  border: 2px solid #6f42c1;
  border-radius: 8px;
  min-width: 150px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.codenode.selected {
  border-color: #6f42c1;
  box-shadow: 0 4px 8px rgba(102, 16, 242, 0.3);
}

.codenode.disabled {
  opacity: 0.6;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #6f42c1;
  color: white;
  border-radius: 6px 6px 0 0;
}

.node-icon {
  font-size: 16px;
}

.node-title {
  flex: 1;
  font-weight: bold;
  font-size: 14px;
}

.config-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 2px;
  opacity: 0.8;
}

.config-btn:hover {
  opacity: 1;
}

.node-body {
  padding: 8px 12px;
  background: white;
}

.config-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item {
  display: flex;
  gap: 4px;
  font-size: 12px;
}

.info-item .value {
  color: #333;
  font-weight: 500;
}

.node-status {
  padding: 4px 12px;
  font-size: 12px;
  text-align: center;
  border-radius: 0 0 6px 6px;
}

.node-status.running {
  background: #fff3cd;
  color: #856404;
}

.node-status.success {
  background: #d4edda;
  color: #155724;
}

.node-status.error {
  background: #f8d7da;
  color: #721c24;
}

.node-status.waiting {
  background: #e2e3e5;
  color: #383d41;
}
</style>
