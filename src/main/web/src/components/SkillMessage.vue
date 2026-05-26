<template>
  <div class="skill-message">
    <div class="skill-header" @click="toggleExpand">
      <div class="skill-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
        </svg>
      </div>
      <div class="skill-info">
        <span class="skill-name">{{ skillName }}</span>
        <span class="skill-label">技能读取</span>
      </div>
      <div class="expand-icon" :class="{ expanded: isExpanded }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="6 9 12 15 18 9"/>
        </svg>
      </div>
    </div>
    <div v-if="isExpanded" class="skill-details">
      <div class="detail-section">
        <div class="detail-label">技能内容</div>
        <div class="skill-content">
          <MarkdownRenderer :content="skillContent" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import MarkdownRenderer from './MarkdownRenderer.vue'
import type { ToolResponse } from '../domain/types'

const props = defineProps<{
  response: ToolResponse
}>()

const isExpanded = ref(false)

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

const skillName = computed(() => {
  // 从工具名称中提取技能名称
  if (props.response.name === 'read_skill') {
    return '读取技能'
  }
  return props.response.name
})

const skillContent = computed(() => {
  try {
    // 尝试解析responseData，如果是JSON字符串需要解析
    const data = props.response.responseData
    if (data.startsWith('"') && data.endsWith('"')) {
      return JSON.parse(data)
    }
    return data
  } catch {
    return props.response.responseData
  }
})
</script>

<style scoped>
.skill-message {
  background: rgba(156, 39, 176, 0.08);
  border: 1px solid rgba(156, 39, 176, 0.2);
  border-radius: 8px;
  overflow: hidden;
  margin: 4px 0;
}

.skill-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.skill-header:hover {
  background: rgba(156, 39, 176, 0.12);
}

.skill-icon {
  width: 20px;
  height: 20px;
  color: var(--color-purple);
  flex-shrink: 0;
}

.skill-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.skill-name {
  font-weight: 600;
  color: var(--color-primary-dark);
  font-size: 0.9rem;
}

.skill-label {
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

.skill-details {
  padding: 12px;
  border-top: 1px solid rgba(156, 39, 176, 0.15);
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

.skill-content {
  padding: 8px;
  background: var(--bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 0.85rem;
  line-height: 1.5;
  color: var(--color-primary-dark);
  max-height: 400px;
  overflow-y: auto;
}
</style>
