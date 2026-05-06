<template>
  <div class="shell">
    <header class="console-header">
      <div class="header-left">
        <h1>AgentHub 控制台</h1>
      </div>
      <div class="header-right">
        <WorkspaceSelector />
        <button class="ghost" type="button" @click="logout">退出登录</button>
      </div>
    </header>
    <nav class="nav">
      <RouterLink to="/agenthub">工作区</RouterLink>
      <RouterLink to="/agenthub/knowledge">知识库</RouterLink>
      <RouterLink to="/agenthub/retrieval">检索</RouterLink>
      <RouterLink to="/agenthub/agents">Agent管理</RouterLink>
      <RouterLink to="/agenthub/agent-configs">Agent配置</RouterLink>
      <RouterLink to="/agenthub/chat">Agent对话</RouterLink>
      <RouterLink to="/agenthub/vector-stores">向量库</RouterLink>
      <RouterLink to="/agenthub/models">大模型</RouterLink>
      <RouterLink to="/agenthub/strategies">策略</RouterLink>
      <RouterLink to="/agenthub/mcp-tools">MCP</RouterLink>
      <RouterLink to="/agenthub/prompt-templates">提示词</RouterLink>
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
  localStorage.removeItem('agenthub_access_token')
  localStorage.removeItem('agenthub_refresh_token')
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
