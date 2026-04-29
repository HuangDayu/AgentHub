<template>
  <div class="workspace-selector">
    <select 
      v-model="selectedWorkspaceId" 
      @change="handleWorkspaceChange"
      class="workspace-select"
    >
      <option value="" disabled>选择工作区</option>
      <option v-for="ws in workspaces" :key="ws.id" :value="ws.id">
        {{ ws.name }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '@/store/workspace-store'
import { listWorkspaces } from '@/api/tenant-api'
import type { Workspace } from '@/domain/types'

const store = useWorkspaceStore()
const router = useRouter()
const workspaces = ref<Workspace[]>([])
const selectedWorkspaceId = ref(store.workspaceId)

// 加载工作区列表
async function loadWorkspaces() {
  if (!store.tenantId) return
  try {
    workspaces.value = await listWorkspaces(store.tenantId)
    // 如果没有选择工作区，自动选择第一个
    if (workspaces.value.length && !store.workspaceId) {
      store.selectWorkspace(workspaces.value[0].id)
      selectedWorkspaceId.value = workspaces.value[0].id
    }
  } catch (error) {
    console.error('加载工作区列表失败:', error)
  }
}

// 处理工作区切换
function handleWorkspaceChange() {
  if (selectedWorkspaceId.value && selectedWorkspaceId.value !== store.workspaceId) {
    const oldWorkspaceId = store.workspaceId
    store.selectWorkspace(selectedWorkspaceId.value)
    // 触发页面刷新（只在非工作区管理页面刷新）
    if (router.currentRoute.value.path !== '/tenant') {
      refreshCurrentPage()
    }
  }
}

// 刷新当前页面数据
function refreshCurrentPage() {
  // 通过重新导航到当前路由来刷新数据
  const currentRoute = router.currentRoute.value
  // 使用时间戳参数强制刷新
  router.push({ path: currentRoute.path, query: { ...currentRoute.query, _t: Date.now() } })
}

// 监听store中的workspaceId变化
watch(() => store.workspaceId, (newId) => {
  selectedWorkspaceId.value = newId
})

// 监听tenantId变化，重新加载工作区列表
watch(() => store.tenantId, () => {
  loadWorkspaces()
})

// 监听路由变化，当进入或离开工作区管理页面时刷新列表
watch(() => router.currentRoute.value.path, (newPath, oldPath) => {
  // 当从工作区管理页面离开时，重新加载工作区列表
  if (oldPath === '/tenant' && newPath !== '/tenant') {
    loadWorkspaces()
  }
})

// 监听自定义事件：工作区列表更新
function handleWorkspaceListUpdate() {
  loadWorkspaces()
}

onMounted(() => {
  loadWorkspaces()
  // 监听工作区列表更新事件
  window.addEventListener('workspace-list-updated', handleWorkspaceListUpdate)
})

onUnmounted(() => {
  // 移除事件监听
  window.removeEventListener('workspace-list-updated', handleWorkspaceListUpdate)
})
</script>

<style scoped>
.workspace-selector {
  display: flex;
  align-items: center;
}

.workspace-select {
  padding: 6px 12px;
  border: 1px solid rgba(38, 66, 102, 0.2);
  border-radius: 4px;
  background: white;
  font-size: 0.85rem;
  color: #1a1e29;
  cursor: pointer;
  min-width: 150px;
}

.workspace-select:hover {
  border-color: rgba(58, 138, 214, 0.5);
}

.workspace-select:focus {
  outline: none;
  border-color: #3a8ad6;
  box-shadow: 0 0 0 2px rgba(58, 138, 214, 0.1);
}
</style>
