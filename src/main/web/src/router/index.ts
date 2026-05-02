import { createRouter, createWebHistory } from 'vue-router'

// ── Login ───────────────────────────────────────────────
const LoginView = () => import('@/views/LoginView.vue')

// ── AgentHub ──────────────────────────────────────────────
const agentHubRoutes = {
  path: '/agenthub',
  component: () => import('@/components/TenantLayout.vue'),
  meta: { requiresAuth: true },
  children: [
    { path: '', component: () => import('@/views/tenant/WorkspaceOverviewView.vue') },
    { path: 'knowledge', component: () => import('@/views/tenant/KnowledgeWorkbenchView.vue') },
    { path: 'agents', component: () => import('@/views/tenant/AgentStudioView.vue') },
    { path: 'retrieval', component: () => import('@/views/tenant/RetrievalView.vue') },
    { path: 'chat', component: () => import('@/views/tenant/RuntimeChatView.vue') },
    { path: 'vector-stores', component: () => import('@/views/tenant/VectorStoreConfigView.vue') },
    { path: 'models', component: () => import('@/views/tenant/ModelConfigView.vue') },
    { path: 'strategies', component: () => import('@/views/tenant/StrategyManagementView.vue') },
    { path: 'mcp-tools', component: () => import('@/views/tenant/McpToolView.vue') },
    { path: 'prompt-templates', component: () => import('@/views/tenant/PromptTemplateView.vue') },
    { path: 'memories', component: () => import('@/views/tenant/MemoryManagementView.vue') },
    { path: 'skills', component: () => import('@/views/tenant/SkillManagementView.vue') },
    { path: 'workflows', component: () => import('@/views/tenant/WorkflowManagementView.vue') },
    { path: 'teams', component: () => import('@/views/tenant/AgentTeamManagementView.vue') },
    { path: 'security-policies', component: () => import('@/views/tenant/SecurityPolicyManagementView.vue') },
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
