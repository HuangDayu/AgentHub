<template>
  <div class="tool-result-message">
    <div class="tool-result-header" @click="toggleExpand">
      <div class="tool-result-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
          <polyline points="22 4 12 14.01 9 11.01"/>
        </svg>
      </div>
      <div class="tool-result-info">
        <span class="tool-name">{{ response.name }}</span>
        <span class="tool-label">工具结果</span>
      </div>
      <div class="expand-icon" :class="{ expanded: isExpanded }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="6 9 12 15 18 9"/>
        </svg>
      </div>
    </div>
    <div v-if="isExpanded" class="tool-result-details">
      <div class="detail-section">
        <div class="detail-label">返回数据</div>
        <pre class="detail-content">{{ formattedResponse }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ToolResponse } from '../domain/types'

const props = defineProps<{
  response: ToolResponse
}>()

const isExpanded = ref(false)

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

const formattedResponse = computed(() => {
  try {
    // 尝试解析JSON并格式化
    const parsed = JSON.parse(props.response.responseData)
    return JSON.stringify(parsed, null, 2)
  } catch {
    // 如果不是JSON，直接返回原始数据
    return props.response.responseData
  }
})
</script>

<style scoped>
.tool-result-message {
  background: rgba(76, 175, 80, 0.08);
  border: 1px solid rgba(76, 175, 80, 0.2);
  border-radius: 8px;
  overflow: hidden;
  margin: 4px 0;
}

.tool-result-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.tool-result-header:hover {
  background: rgba(76, 175, 80, 0.12);
}

.tool-result-icon {
  width: 20px;
  height: 20px;
  color: var(--color-success);
  flex-shrink: 0;
}

.tool-result-info {
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

.tool-result-details {
  padding: 12px;
  border-top: 1px solid rgba(76, 175, 80, 0.15);
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
  max-height: 400px;
  overflow-y: auto;
}
</style>
