<template>
  <section class="grid">
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h2>System Tools 管理</h2>
          <p class="muted">管理系统工具函数，支持启用/禁用控制</p>
        </div>
        <div class="header-filters">
          <select v-model="filterCategory" class="filter-select">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <select v-model="filterEnabled" class="filter-select">
            <option value="">全部状态</option>
            <option value="true">已启用</option>
            <option value="false">已禁用</option>
          </select>
        </div>
      </div>
    </div>

    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <article v-else-if="loading" class="empty-state">加载中...</article>

    <template v-else>
      <article class="panel stack">

        <div v-if="filteredTools.length === 0" class="empty-state">暂无工具数据</div>

        <table v-else>
          <thead>
            <tr>
              <th>工具名称</th>
              <th>分类</th>
              <th>方法数</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tool in filteredTools" :key="tool.id">
              <td>
                <strong>{{ tool.toolName }}</strong>
                <div class="muted">{{ tool.description }}</div>
              </td>
              <td><span class="tag">{{ tool.category }}</span></td>
              <td>{{ tool.methodCount }}</td>
              <td>
                <span :class="['tag', tool.enabled ? 'tag-success' : 'tag-error']">
                  {{ tool.enabled ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDateTime(tool.createdAt) }}</td>
              <td>
                <div class="chip-row">
                  <button
                    :class="['ghost', tool.enabled ? 'danger' : 'success']"
                    type="button"
                    @click="toggleEnabled(tool)"
                  >
                    {{ tool.enabled ? '禁用' : '启用' }}
                  </button>
                  <button class="ghost" type="button" @click="handleDelete(tool)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { formatDateTime } from '@/common/format'
import { useWorkspaceStore } from '@/store/workspace-store'
import { 
  listSystemTools,
  syncSystemTools,
  enableSystemTool,
  disableSystemTool,
  deleteSystemTool,
  type FunctionTool
} from '@/api/system-tools-api'

const store = useWorkspaceStore()
const tools = ref<FunctionTool[]>([])
const loading = ref(false)
const syncing = ref(false)
const filterCategory = ref('')
const filterEnabled = ref('')

const selectionReady = computed(() => !!store.tenantId && !!store.workspaceId)

// Get selection object
function getSelection() {
  return {
    tenantId: store.tenantId!,
    workspaceId: store.workspaceId!
  }
}

const categories = computed(() => {
  const cats = new Set(tools.value.map(t => t.category))
  return Array.from(cats).sort()
})

const filteredTools = computed(() => {
  let result = tools.value
  if (filterCategory.value) {
    result = result.filter(t => t.category === filterCategory.value)
  }
  if (filterEnabled.value) {
    const enabled = filterEnabled.value === 'true'
    result = result.filter(t => t.enabled === enabled)
  }
  return result
})

const loadTools = async () => {
  if (!selectionReady.value) return
  loading.value = true
  try {
    tools.value = await listSystemTools(getSelection())
  } catch (error) {
    console.error('Failed to load tools:', error)
  } finally {
    loading.value = false
  }
}

const syncTools = async () => {
  if (!selectionReady.value) return
  syncing.value = true
  try {
    await syncSystemTools(getSelection())
    await loadTools()
  } catch (error) {
    console.error('Failed to sync tools:', error)
  } finally {
    syncing.value = false
  }
}

const toggleEnabled = async (tool: FunctionTool) => {
  if (!selectionReady.value) return
  try {
    if (tool.enabled) {
      await disableSystemTool(getSelection(), tool.id)
    } else {
      await enableSystemTool(getSelection(), tool.id)
    }
    tool.enabled = !tool.enabled
  } catch (error) {
    console.error('Failed to toggle:', error)
  }
}

const handleDelete = async (tool: FunctionTool) => {
  if (!selectionReady.value) return
  if (!confirm(`确定删除 ${tool.toolName}?`)) return
  try {
    await deleteSystemTool(getSelection(), tool.id)
    tools.value = tools.value.filter(t => t.id !== tool.id)
  } catch (error) {
    console.error('Failed to delete:', error)
  }
}

watch(() => store.workspaceId, () => {
  loadTools()
})

const handleGlobalSync = () => {
  if (window.location.pathname.includes('system-tools')) {
    syncTools()
  }
}

onMounted(() => {
  window.addEventListener('global-sync', handleGlobalSync)
  loadTools()
})

onUnmounted(() => {
  window.removeEventListener('global-sync', handleGlobalSync)
})
</script>

<style scoped>
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
}

.header-text {
  flex: 0 0 auto;
}

.header-filters {
  display: flex;
  gap: 12px;
  align-items: center;
  flex: 1;
  justify-content: flex-end;
}

.filter-select {
  padding: 0.5rem 1rem;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 8px;
  background: var(--bg-color, white);
  font-size: 0.875rem;
  min-width: 150px;
  flex: 1;
  max-width: 200px;
}

.filter-select:focus {
  outline: none;
  border-color: var(--primary-color, #4CAF50);
}

.success {
  color: var(--success-color, #4CAF50);
  border-color: var(--success-color, #4CAF50);
}

.danger {
  color: var(--danger-color, #f44336);
  border-color: var(--danger-color, #f44336);
}
</style>
