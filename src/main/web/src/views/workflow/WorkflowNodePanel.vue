<template>
  <div class="workflow-node-panel">
    <div class="panel-header">
      <h3>节点面板</h3>
    </div>
    
    <div class="panel-body">
      <!-- 搜索框 -->
      <div class="search-box">
        <input 
          v-model="searchText"
          placeholder="搜索节点..."
          class="search-input"
        />
      </div>
      
      <!-- 节点分类 -->
      <div 
        v-for="category in categories" 
        :key="category.name"
        class="node-category"
      >
        <div 
          class="category-header"
          @click="toggleCategory(category.name)"
        >
          <span class="category-icon">{{ category.icon }}</span>
          <span class="category-name">{{ category.label }}</span>
          <span class="category-arrow" :class="{ expanded: expandedCategories.has(category.name) }">
            ▼
          </span>
        </div>
        
        <div v-if="expandedCategories.has(category.name)" class="category-nodes">
          <div
            v-for="node in getNodesByCategory(category.name)"
            :key="node.type"
            class="node-item"
            draggable="true"
            @dragstart="handleDragStart($event, node)"
            @click="handleNodeClick(node)"
          >
            <span class="node-icon">{{ node.icon }}</span>
            <div class="node-info">
              <span class="node-name">{{ node.name }}</span>
              <span class="node-desc">{{ node.description }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { NODE_TEMPLATES } from '@/types/workflow'
import type { NodeTemplate, NodeType } from '@/types/workflow'

const emit = defineEmits<{
  'node-drag-start': [nodeType: NodeType]
  'node-click': [node: NodeTemplate]
}>()

// 状态
const searchText = ref('')
const expandedCategories = ref(new Set(['control', 'ai', 'action']))

// 分类定义
const categories = [
  { name: 'control', label: '控制节点', icon: '⚙' },
  { name: 'ai', label: 'AI节点', icon: '🤖' },
  { name: 'action', label: '动作节点', icon: '⚡' }
]

// 过滤后的节点
const filteredNodes = computed(() => {
  if (!searchText.value) {
    return NODE_TEMPLATES
  }
  const search = searchText.value.toLowerCase()
  return NODE_TEMPLATES.filter(node => 
    node.name.toLowerCase().includes(search) ||
    node.description.toLowerCase().includes(search)
  )
})

// 方法
function getNodesByCategory(category: string) {
  return filteredNodes.value.filter(node => node.category === category)
}

function toggleCategory(categoryName: string) {
  if (expandedCategories.value.has(categoryName)) {
    expandedCategories.value.delete(categoryName)
  } else {
    expandedCategories.value.add(categoryName)
  }
}

function handleDragStart(event: DragEvent, node: NodeTemplate) {
  event.dataTransfer?.setData('nodeType', node.type)
  event.dataTransfer?.setData('nodeName', node.name)
  emit('node-drag-start', node.type)
}

function handleNodeClick(node: NodeTemplate) {
  emit('node-click', node)
}
</script>

<style scoped>
.workflow-node-panel {
  width: 260px;
  background: white;
  border-right: 1px solid #e1e5eb;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e1e5eb;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.search-box {
  margin-bottom: 12px;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #409eff;
}

.node-category {
  margin-bottom: 8px;
}

.category-header {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
}

.category-header:hover {
  background: #eef1f6;
}

.category-icon {
  font-size: 16px;
  margin-right: 8px;
}

.category-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.category-arrow {
  font-size: 10px;
  color: #909399;
  transition: transform 0.2s;
}

.category-arrow.expanded {
  transform: rotate(180deg);
}

.category-nodes {
  padding: 8px 0;
}

.node-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 4px;
  cursor: grab;
  transition: all 0.2s;
}

.node-item:hover {
  background: #f5f7fa;
}

.node-item:active {
  cursor: grabbing;
  background: #eef1f6;
}

.node-icon {
  font-size: 20px;
  margin-right: 10px;
}

.node-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.node-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.node-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
</style>
