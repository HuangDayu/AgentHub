import { createRouter, createWebHistory } from 'vue-router'

// ── Login ───────────────────────────────────────────────
const LoginView = () => import('@/views/LoginView.vue')

// ── AgentHub ──────────────────────────────────────────────
const agentHubRoutes = {
  path: '/agenthub',
  component: () => import('@/components/TenantLayout.vue'),
  meta: { requiresAuth: true },
  children: [
    // 首页改为Agent对话
    { path: '', component: () => import('@/views/agenthub/RuntimeChatView.vue') },
    // 设置页面
    { path: 'settings', component: () => import('@/views/agenthub/SettingsView.vue') },
    
    // 监控和追踪功能
    { 
      path: 'monitor', 
      component: () => import('@/views/monitor/MetricDashboard.vue'),
      meta: { title: '监控仪表盘' }
    },
    { 
      path: 'alerts', 
      component: () => import('@/views/monitor/AlertList.vue'),
      meta: { title: '告警管理' }
    },
    { 
      path: 'traces', 
      component: () => import('@/views/trace/TraceList.vue'),
      meta: { title: '追踪列表' }
    },
    { 
      path: 'traces/:traceId', 
      component: () => import('@/views/trace/TraceDetail.vue'),
      meta: { title: '追踪详情' }
    },
    
    // 其他功能页面（保留但不在导航显示）
    { path: 'workspace', component: () => import('@/views/agenthub/WorkspaceOverviewView.vue') },
    { path: 'knowledge', component: () => import('@/views/agenthub/KnowledgeWorkbenchView.vue') },
    { path: 'agents', component: () => import('@/views/agenthub/AgentStudioView.vue') },
    { path: 'agent-configs', component: () => import('@/views/agenthub/AgentConfigView.vue') },
    { path: 'retrieval', component: () => import('@/views/agenthub/RetrievalView.vue') },
    { path: 'chat', component: () => import('@/views/agenthub/RuntimeChatView.vue') },
    { path: 'vector-stores', component: () => import('@/views/agenthub/VectorStoreConfigView.vue') },
    { path: 'models', component: () => import('@/views/agenthub/ModelConfigView.vue') },
    { path: 'strategies', component: () => import('@/views/agenthub/StrategyManagementView.vue') },
    { path: 'mcp-tools', component: () => import('@/views/agenthub/McpToolView.vue') },
    { path: 'prompt-templates', component: () => import('@/views/agenthub/PromptTemplateView.vue') },
    { path: 'memories', component: () => import('@/views/agenthub/MemoryManagementView.vue') },
    { path: 'skills', component: () => import('@/views/agenthub/SkillManagementView.vue') },
    { path: 'workflows', component: () => import('@/views/agenthub/WorkflowManagementView.vue') },
    { path: 'teams', component: () => import('@/views/agenthub/AgentTeamManagementView.vue') },
    { path: 'system-tools', component: () => import('@/views/agenthub/SystemToolsView.vue') },
    { path: 'scheduled-tasks', component: () => import('@/views/agenthub/ScheduledTaskView.vue') },
  ],
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView },
    {
      path: '/agenthub/workflows/:id',
      component: () => import('@/views/agenthub/WorkflowEditorView.vue'),
      meta: { requiresAuth: true, fullScreen: true },
    },
    agentHubRoutes,
    { path: '/:pathMatch(.*)*', redirect: '/login' },
  ],
})

// Auth guard
router.beforeEach((to) => {
  // Allow login page
  if (to.path === '/login') return true

  // Check if authenticated
  const token = localStorage.getItem('agenthub_access_token')
  if (!token) return '/login'

  return true
})

export default router
