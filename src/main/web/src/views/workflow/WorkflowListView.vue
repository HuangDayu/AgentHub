<template>
  <div class="workflow-list-view">
    <div class="page-header">
      <h1>工作流管理</h1>
      <button class="btn btn-primary" @click="createNew">
        + 新建工作流
      </button>
    </div>
    
    <div class="search-bar">
      <input 
        v-model="searchQuery" 
        type="text" 
        placeholder="搜索工作流..."
        class="search-input"
      />
    </div>
    
    <div class="workflow-grid">
      <div 
        v-for="workflow in filteredWorkflows" 
        :key="workflow.id"
        class="workflow-card"
        @click="openWorkflow(workflow.id)"
      >
        <div class="card-header">
          <h3>{{ workflow.name }}</h3>
          <div class="card-actions">
            <button class="btn-icon" @click.stop="editWorkflow(workflow.id)" title="编辑">
              ✏️
            </button>
            <button class="btn-icon" @click.stop="deleteWorkflow(workflow.id)" title="删除">
              🗑️
            </button>
          </div>
        </div>
        <div class="card-body">
          <p class="description">{{ workflow.description || '暂无描述' }}</p>
          <div class="meta">
            <span class="meta-item">
              <span class="icon">📅</span>
              {{ formatDate(workflow.updatedAt) }}
            </span>
            <span class="meta-item">
              <span class="icon">📊</span>
              {{ workflow.nodeCount }} 个节点
            </span>
          </div>
        </div>
        <div class="card-footer">
          <span class="status-badge" :class="workflow.status">
            {{ statusText(workflow.status) }}
          </span>
        </div>
      </div>
    </div>
    
    <div v-if="filteredWorkflows.length === 0" class="empty-state">
      <div class="empty-icon">📋</div>
      <p>暂无工作流</p>
      <button class="btn btn-primary" @click="createNew">
        创建第一个工作流
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const searchQuery = ref('')

// 模拟数据
const workflows = ref([
  {
    id: '1',
    name: '示例工作流',
    description: '这是一个示例工作流，展示了基本的节点连接',
    status: 'active',
    nodeCount: 5,
    updatedAt: new Date().toISOString()
  },
  {
    id: '2',
    name: '数据处理流程',
    description: '数据清洗、转换和导出的完整流程',
    status: 'draft',
    nodeCount: 8,
    updatedAt: new Date(Date.now() - 86400000).toISOString()
  }
])

const filteredWorkflows = computed(() => {
  if (!searchQuery.value) return workflows.value
  const query = searchQuery.value.toLowerCase()
  return workflows.value.filter(w => 
    w.name.toLowerCase().includes(query) ||
    w.description?.toLowerCase().includes(query)
  )
})

function createNew() {
  router.push('/workflow/editor/new')
}

function openWorkflow(id: string) {
  router.push(`/workflow/editor/${id}`)
}

function editWorkflow(id: string) {
  router.push(`/workflow/editor/${id}`)
}

async function deleteWorkflow(id: string) {
  if (!confirm('确定要删除这个工作流吗？')) return
  
  // TODO: 调用删除API
  workflows.value = workflows.value.filter(w => w.id !== id)
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  
  return date.toLocaleDateString()
}

function statusText(status: string) {
  switch (status) {
    case 'active': return '已发布'
    case 'draft': return '草稿'
    case 'archived': return '已归档'
    default: return status
  }
}
</script>

<style scoped>
.workflow-list-view {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.search-bar {
  margin-bottom: 24px;
}

.search-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.workflow-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.workflow-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.workflow-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 4px;
  font-size: 16px;
}

.btn-icon:hover {
  background: #f5f7fa;
  border-radius: 4px;
}

.card-body {
  margin-bottom: 12px;
}

.description {
  color: #606266;
  font-size: 14px;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.icon {
  font-size: 14px;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge.active {
  background: #d4edda;
  color: #155724;
}

.status-badge.draft {
  background: #fff3cd;
  color: #856404;
}

.status-badge.archived {
  background: #e2e3e5;
  color: #383d41;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 24px;
}

.btn {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-primary {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.btn-primary:hover {
  background: #66b1ff;
  border-color: #66b1ff;
}
</style>
