<template>
  <div class="shell">
    <header class="console-header">
      <div class="header-left">
        <h1>租户控制台</h1>
      </div>
      <div class="header-right">
        <WorkspaceSelector />
        <button class="ghost" type="button" @click="logout">退出登录</button>
      </div>
    </header>
    <nav class="nav">
      <RouterLink to="/tenant">工作区</RouterLink>
      <RouterLink to="/tenant/knowledge">知识库</RouterLink>
      <RouterLink to="/tenant/retrieval">知识检索</RouterLink>
      <RouterLink to="/tenant/agents">Agent Studio</RouterLink>
      <RouterLink to="/tenant/chat">Agent 对话</RouterLink>
      <RouterLink to="/tenant/vector-stores">向量数据库</RouterLink>
      <RouterLink to="/tenant/models">大模型配置</RouterLink>
      <RouterLink to="/tenant/strategies">策略管理</RouterLink>
      <RouterLink to="/tenant/mcp-tools">MCP工具</RouterLink>
      <RouterLink to="/tenant/prompt-templates">提示词</RouterLink>
    </nav>
    <main class="content">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useWorkspaceStore } from '@/store/workspace-store'
import { getCurrentUser } from '@/api/tenant-api'
import WorkspaceSelector from './WorkspaceSelector.vue'

const store = useWorkspaceStore()
const router = useRouter()

// 初始化租户ID
onMounted(async () => {
  if (!store.tenantId) {
    try {
      const user = await getCurrentUser()
      store.selectTenant(user.tenantId)
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
})

function logout() {
  localStorage.removeItem('things_knowledge_access_token')
  localStorage.removeItem('things_knowledge_refresh_token')
  localStorage.removeItem('tenant_console_access_token')
  localStorage.removeItem('user_console_access_token')
  router.push('/login')
}
</script>

<style scoped>
.console-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(26, 30, 41, 0.08);
  box-shadow: 0 4px 12px rgba(32, 44, 68, 0.06);
  backdrop-filter: blur(12px);
  border-radius: 10px;
}
.header-left h1 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #1a1e29;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.tenant-badge {
  font-size: 0.8rem;
  color: #5d6678;
}
</style>
