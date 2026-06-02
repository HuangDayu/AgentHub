<template>
  <div class="app-shell" :class="{ 'fullscreen-mode': isFullScreen }">
    <!-- 顶部栏 -->
    <header v-if="!isFullScreen" class="app-header">
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
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>

    <template v-if="!isFullScreen">
      <!-- 悬浮设置按钮 -->
      <FloatingSettingsButton :bottom="buttonPositions.settings" />
    </template>

    <!-- 异常通知（Toast + 弹窗，不占浮动按钮位置） -->
    <FloatingErrorButton />

    <!-- 全局确认弹窗 -->
    <ConfirmDialog />
  </div>
    <template v-if="!isFullScreen">
      <FloatingEffectButton :bottom="buttonPositions.effect" />
      <FloatingSkillConfigButton v-if="showSkillConfigButton" :bottom="buttonPositions.skillConfig" />
      <FloatingHomeButton v-if="showHomeButton" :bottom="buttonPositions.home" />
      <FloatingSyncButton v-if="showSyncButton" :bottom="buttonPositions.sync" @sync="handleSync" />
      <FloatingAddButton v-if="showAddButton" :bottom="buttonPositions.add" @add="handleAdd" />
    </template>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import { useWorkspaceStore } from '@/store/workspace-store'
import { getCurrentUser } from '@/api/tenant-api'
import WorkspaceSelector from './WorkspaceSelector.vue'
import FloatingSettingsButton from './FloatingSettingsButton.vue'

import FloatingErrorButton from './FloatingErrorButton.vue'
import ConfirmDialog from './ConfirmDialog.vue'
import FloatingEffectButton from './FloatingEffectButton.vue'
import FloatingAddButton from './FloatingAddButton.vue'
import FloatingSyncButton from './FloatingSyncButton.vue'
import FloatingHomeButton from './FloatingHomeButton.vue'
import FloatingSkillConfigButton from './FloatingSkillConfigButton.vue'
const store = useWorkspaceStore()
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 是否全屏模式（隐藏header和悬浮按钮）
const isFullScreen = computed(() => {
  return route.meta.fullScreen === true
})

// 判断是否显示新增按钮
const showAddButton = computed(() => {
  const path = route.path
  return path.includes('strategies') ||
         path.includes('agents') ||
         path.includes('workspace') ||
         path.includes('vector-stores') ||
         path.includes('models') ||
         path.includes('agent-configs') ||
         path.includes('knowledge') ||
         path.includes('retrieval') ||
         path.includes('strategy') ||
         path.includes('mcp-tool') ||
         path.includes('prompt-template') ||
         path.includes('memories') ||
         path.includes('skill') ||
         path.includes('workflow') ||
         path.includes('teams') ||
         path.includes('security-policy') ||
         path.includes('scheduled-task')
})

// 判断是否显示同步按钮
const showSyncButton = computed(() => {
  const path = route.path
  return path.includes('skill') || path.includes('system-tools') || path.includes('agent-configs')
})

// 判断是否显示技能配置按钮
const showSkillConfigButton = computed(() => {
  return route.path.includes('skill')
})

// 判断是否显示返回首页按钮（非首页时显示）
const showHomeButton = computed(() => {
  const path = route.path
  // 首页路径是 /agenthub
  return path !== '/agenthub'
})

// 处理新增按钮点击
const handleAdd = () => {
  // 触发全局事件，让各个页面监听
  window.dispatchEvent(new CustomEvent('global-add'))
}

// 处理同步按钮点击
const handleSync = () => {
  // 触发全局事件，让各个页面监听
  window.dispatchEvent(new CustomEvent('global-sync'))
}

// 计算按钮位置
const buttonPositions = computed(() => {
  const BUTTON_HEIGHT = 48
  const BUTTON_GAP = 8
  const BASE_BOTTOM = 24

  let currentBottom = BASE_BOTTOM
  const positions = {
    settings: currentBottom,
    effect: 0,
    skillConfig: 0,
    home: 0,
    sync: 0,
    add: 0
  }

  // 设置按钮：24px
  currentBottom += BUTTON_HEIGHT + BUTTON_GAP
  // 主题按钮
  positions.effect = currentBottom

  // 技能配置按钮（在主题按钮上面，同步按钮下面）
  if (showSkillConfigButton.value) {
    currentBottom += BUTTON_HEIGHT + BUTTON_GAP
    positions.skillConfig = currentBottom
  }

  // 返回首页按钮（在技能配置按钮上面）
  if (showHomeButton.value) {
    currentBottom += BUTTON_HEIGHT + BUTTON_GAP
    positions.home = currentBottom
  }

  // 如果有同步按钮
  if (showSyncButton.value) {
    currentBottom += BUTTON_HEIGHT + BUTTON_GAP
    positions.sync = currentBottom
  }

  // 如果有新增按钮
  if (showAddButton.value) {
    currentBottom += BUTTON_HEIGHT + BUTTON_GAP
    positions.add = currentBottom
  }

  return positions
})
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

.app-shell.fullscreen-mode {
  overflow: hidden;
}

.app-shell.fullscreen-mode .app-content {
  max-width: none;
  padding: 0;
  margin: 0;
  height: 100vh;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: var(--bg-header, rgba(255, 255, 255, 0.78));
  border-bottom: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
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
  background: linear-gradient(135deg, var(--color-primary-dark, #1e3a6f), var(--color-primary, #3a7bd5));
  border-radius: 10px;
  color: var(--color-text-inverse, #f8faff);
  box-shadow: var(--shadow-glow, 0 4px 20px rgba(58, 123, 213, 0.25));
}

.logo svg {
  width: 100%;
  height: 100%;
}

.header-left h1 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  font-family: var(--font-heading, inherit);
  background: linear-gradient(135deg, var(--color-primary-dark, #1e3a6f), var(--color-primary, #3a7bd5));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--color-border-strong, rgba(26, 30, 43, 0.12));
  background: var(--bg-card-solid, #ffffff);
  border-radius: 10px;
  font-family: var(--font-body, inherit);
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-primary-dark, #1e3a6f);
  cursor: pointer;
  transition: all 0.25s ease;
}

.logout-btn svg {
  width: 18px;
  height: 18px;
}

.logout-btn:hover {
  border-color: var(--color-error, #d44c3a);
  color: var(--color-error, #d44c3a);
  background: rgba(212, 76, 58, 0.05);
}

.app-content {
  flex: 1;
  padding: 24px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

/* Page Transitions */
.page-enter-active {
  animation: pageIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.page-leave-active {
  animation: pageOut 0.25s ease forwards;
}

@keyframes pageIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes pageOut {
  from { opacity: 1; }
  to { opacity: 0; }
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
