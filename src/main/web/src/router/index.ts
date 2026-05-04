import { createRouter, createWebHistory } from 'vue-router'

// ── Login ───────────────────────────────────────────────
const LoginView = () => import('@/views/LoginView.vue')

// ── AgentHub ──────────────────────────────────────────────
const agentHubRoutes = {
  path: '/agenthub',
  component: () => import('@/components/TenantLayout.vue'),
  meta: { requiresAuth: true },
  children: [
    { path: '', component: () => import('@/views/agenthub/WorkspaceOverviewView.vue') },
    { path: 'knowledge', component: () => import('@/views/agenthub/KnowledgeWorkbenchView.vue') },
    { path: 'agents', component: () => import('@/views/agenthub/AgentStudioView.vue') },
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
    { path: 'security-policies', component: () => import('@/views/agenthub/SecurityPolicyManagementView.vue') },
    { path: 'function-tools', component: () => import('@/views/agenthub/FunctionToolsView.vue') },
  ],
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView },
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
