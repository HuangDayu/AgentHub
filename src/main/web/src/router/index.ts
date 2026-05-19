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

// ── Workflow ──────────────────────────────────────────────
const workflowRoutes = {
  path: '/workflow',
  component: () => import('@/components/TenantLayout.vue'),
  meta: { requiresAuth: true },
  children: [
    // 工作流列表
    { 
      path: '', 
      component: () => import('@/views/workflow/WorkflowListView.vue'),
      name: 'workflow-list'
    },
    // 工作流编辑器
    { 
      path: 'editor/:id?', 
      component: () => import('@/views/workflow/WorkflowEditorView.vue'),
      name: 'workflow-editor'
    },
    // 工作流执行
    { 
      path: 'execution/:id', 
      component: () => import('@/views/workflow/WorkflowExecutionView.vue'),
      name: 'workflow-execution'
    },
  ],
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView },
    agentHubRoutes,
    workflowRoutes,
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
