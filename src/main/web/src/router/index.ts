import { createRouter, createWebHistory } from 'vue-router'

// ── Login ───────────────────────────────────────────────
const LoginView = () => import('@/views/LoginView.vue')

// ── Admin ───────────────────────────────────────────────
const adminRoutes = {
  path: '/admin',
  component: () => import('@/components/AdminLayout.vue'),
  meta: { requiresAuth: true, role: 'OWNER' },
  children: [
    { path: '', component: () => import('@/views/admin/OverviewView.vue') },
    { path: 'tenants', component: () => import('@/views/admin/TenantsView.vue') },
    { path: 'billing', component: () => import('@/views/admin/BillingView.vue') },
    { path: 'audit', component: () => import('@/views/admin/AuditView.vue') },
    { path: 'connectors', component: () => import('@/views/admin/ConnectorsView.vue') },
    { path: 'policies', component: () => import('@/views/admin/PolicyView.vue') },
    { path: 'tools', component: () => import('@/views/admin/ToolView.vue') },
  ],
}

// ── Tenant ──────────────────────────────────────────────
const tenantRoutes = {
  path: '/tenant',
  component: () => import('@/components/TenantLayout.vue'),
  meta: { requiresAuth: true, role: 'ADMIN' },
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

// ── User ────────────────────────────────────────────────
const userRoutes = {
  path: '/user',
  component: () => import('@/components/UserLayout.vue'),
  meta: { requiresAuth: true, role: 'VIEWER' },
  children: [
    { path: '', component: () => import('@/views/user/HomeView.vue') },
    { path: 'search', component: () => import('@/views/user/SearchView.vue') },
    { path: 'chat', component: () => import('@/views/user/ChatView.vue') },
    { path: 'chat/:sessionId', component: () => import('@/views/user/ChatView.vue') },
    { path: 'notifications', component: () => import('@/views/user/NotificationView.vue') },
    { path: 'settings', component: () => import('@/views/user/SettingsView.vue') },
  ],
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView },
    adminRoutes,
    tenantRoutes,
    userRoutes,
    { path: '/:pathMatch(.*)*', redirect: '/login' },
  ],
})

// Auth guard
router.beforeEach((to) => {
  // Allow login page
  if (to.path === '/login') return true

  // Check if authenticated
  const token = localStorage.getItem('things_knowledge_access_token')
  if (!token) return '/login'

  // Check role-based access
  const storedRole = localStorage.getItem('things_knowledge_user_role') ?? ''
  const allowedRole = to.meta.role as string

  if (allowedRole) {
    const normalizedStored = storedRole.replace(/^ROLE_/, '')
    const normalizedAllowed = allowedRole.replace(/^ROLE_/, '')

    // Admin console: only OWNER
    if (normalizedAllowed === 'OWNER' && normalizedStored !== 'OWNER') return '/tenant'
    // Tenant console: OWNER or ADMIN
    if (normalizedAllowed === 'ADMIN' && normalizedStored !== 'ADMIN' && normalizedStored !== 'OWNER') return '/user'
    // User console: everyone allowed
  }

  return true
})

export default router
