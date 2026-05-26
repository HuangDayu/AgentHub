<template>
  <div class="tool-call-message">
    <div class="tool-call-header" @click="toggleExpand">
      <div class="tool-call-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 0-7.94-7.94l-3.77 3.77a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0"/>
          <path d="M9.3 17.7a1 1 0 0 0 0-1.4l-1.6-1.6a1 1 0 0 0-1.4 0l-3.77 3.77a6 6 0 0 0 7.94 7.94l3.77-3.77a1 1 0 0 0 0-1.4l-1.6-1.6a1 1 0 0 0-1.4 0"/>
        </svg>
      </div>
      <div class="tool-call-info">
        <span class="tool-name">{{ toolCall.name }}</span>
        <span class="tool-label">工具调用</span>
      </div>
      <div class="expand-icon" :class="{ expanded: isExpanded }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="6 9 12 15 18 9"/>
        </svg>
      </div>
    </div>
    <div v-if="isExpanded" class="tool-call-details">
      <div class="detail-section">
        <div class="detail-label">参数</div>
        <pre class="detail-content">{{ formattedArguments }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ToolCall } from '../domain/types'

const props = defineProps<{
  toolCall: ToolCall
}>()

const isExpanded = ref(false)

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

const formattedArguments = computed(() => {
  try {
    return JSON.stringify(JSON.parse(props.toolCall.arguments), null, 2)
  } catch {
    return props.toolCall.arguments
  }
})
</script>

<style scoped>
.tool-call-message {
  background: rgba(58, 138, 214, 0.08);
  border: 1px solid rgba(58, 123, 213, 0.2);
  border-radius: 8px;
  overflow: hidden;
  margin: 4px 0;
}

.tool-call-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.tool-call-header:hover {
  background: rgba(58, 138, 214, 0.12);
}

.tool-call-icon {
  width: 20px;
  height: 20px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.tool-call-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tool-name {
  font-weight: 600;
  color: var(--color-primary-dark);
  font-size: 0.9rem;
}

.tool-label {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.expand-icon {
  width: 16px;
  height: 16px;
  color: var(--color-text-muted);
  transition: transform 0.2s;
  flex-shrink: 0;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.tool-call-details {
  padding: 12px;
  border-top: 1px solid rgba(58, 123, 213, 0.15);
  background: rgba(255, 255, 255, 0.5);
}

.detail-section {
  margin-bottom: 8px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-content {
  margin: 0;
  padding: 8px;
  background: var(--bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 0.8rem;
  line-height: 1.4;
  color: var(--color-primary-dark);
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
