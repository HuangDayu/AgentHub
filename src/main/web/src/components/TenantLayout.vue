<template>
  <div class="app-shell">
    <!-- 顶部栏 -->
    <header class="app-header">
      <div class="header-left">
        <div class="logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <h1>AgentHub</h1>
      </div>
      <div class="header-right">
        <WorkspaceSelector />
        <button class="logout-btn" @click="logout">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          退出
        </button>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="app-content">
      <RouterView />
    </main>

    <!-- 悬浮设置按钮 -->
    <FloatingSettingsButton />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import { useWorkspaceStore } from '@/store/workspace-store'
import { getCurrentUser } from '@/api/tenant-api'
import WorkspaceSelector from './WorkspaceSelector.vue'
import FloatingSettingsButton from './FloatingSettingsButton.vue'

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
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid rgba(22, 33, 50, 0.08);
  box-shadow: 0 2px 8px rgba(32, 44, 68, 0.04);
  backdrop-filter: blur(12px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 36px;
  height: 36px;
  padding: 8px;
  background: linear-gradient(135deg, #264266, #3a8ad6);
  border-radius: 10px;
  color: white;
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
}

.logo svg {
  width: 100%;
  height: 100%;
}

.header-left h1 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  background: linear-gradient(135deg, #264266, #3a8ad6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid rgba(38, 66, 102, 0.14);
  background: white;
  border-radius: 10px;
  font: inherit;
  font-size: 0.9rem;
  font-weight: 500;
  color: #264266;
  cursor: pointer;
  transition: all 0.25s ease;
}

.logout-btn svg {
  width: 18px;
  height: 18px;
}

.logout-btn:hover {
  border-color: #c94a35;
  color: #c94a35;
  background: rgba(201, 74, 53, 0.05);
}

.app-content {
  flex: 1;
  padding: 24px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

/* Responsive */
@media (max-width: 768px) {
  .app-header {
    padding: 12px 16px;
  }
  
  .header-left h1 {
    font-size: 1rem;
  }
  
  .app-content {
    padding: 16px;
  }
  
  .logout-btn span {
    display: none;
  }
  
  .logout-btn {
    padding: 8px;
  }
}
</style>
