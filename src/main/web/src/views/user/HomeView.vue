<template>
  <section class="grid">
    <div class="page-header">
      <div>
        <h2>欢迎回来</h2>
        <p class="muted">快速访问知识检索和 AI 对话。</p>
      </div>
    </div>

    <div class="grid two-up">
      <RouterLink to="/search" class="card quick-action">
        <div class="action-icon">🔍</div>
        <div>
          <h3>知识检索</h3>
          <p class="muted">搜索知识库，快速找到你需要的文档和答案。</p>
        </div>
      </RouterLink>
      <RouterLink to="/chat" class="card quick-action">
        <div class="action-icon">💬</div>
        <div>
          <h3>AI 对话</h3>
          <p class="muted">与 AI Agent 对话，获取智能解答和分析。</p>
        </div>
      </RouterLink>
      <RouterLink to="/notifications" class="card quick-action">
        <div class="action-icon">🔔</div>
        <div>
          <h3>通知中心</h3>
          <p class="muted">查看最新通知和系统消息。</p>
        </div>
      </RouterLink>
      <RouterLink to="/settings" class="card quick-action">
        <div class="action-icon">⚙️</div>
        <div>
          <h3>个人设置</h3>
          <p class="muted">管理个人信息和偏好设置。</p>
        </div>
      </RouterLink>
    </div>

    <article class="panel">
      <h3>最近的对话</h3>
      <p v-if="loading" class="muted">
        <span class="loading-spinner"></span> 加载中...
      </p>
      <p v-else-if="error" class="status">{{ error }}</p>
      <template v-else-if="recentSessions.length > 0">
        <div
          v-for="session in recentSessions"
          :key="session.id"
          class="session-item"
        >
          <RouterLink :to="`/chat/${session.id}`" class="session-link">
            <div class="session-info">
              <strong>{{ session.title || '未命名对话' }}</strong>
              <span class="muted">{{ session.agentName }} · {{ formatTime(session.updatedAt) }}</span>
            </div>
          </RouterLink>
        </div>
      </template>
      <p v-else class="muted">暂无对话记录，开始你的第一次对话吧。</p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { listAgents, listSessions, type Session, type Agent } from '@/api/user-runtime-api'
import { relativeTime } from '@/common/format'

const recentSessions = ref<Session[]>([])
const loading = ref(true)
const error = ref('')

function formatTime(iso: string): string {
  return relativeTime(iso)
}

onMounted(async () => {
  try {
    // Get agents first
    const agents = await listAgents()
    if (agents.length === 0) {
      loading.value = false
      return
    }
    // Load sessions for the first agent
    const sessions = await listSessions(agents[0].id)
    recentSessions.value = sessions
      .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
      .slice(0, 5)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.quick-action {
  display: flex;
  gap: 16px;
  align-items: center;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.quick-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 32px 48px rgba(40, 54, 79, 0.12);
}

.quick-action h3 {
  margin: 0 0 4px;
}

.quick-action p {
  margin: 0;
  font-size: 14px;
}

.action-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.session-item {
  padding: 12px 0;
  border-bottom: 1px solid rgba(22, 33, 50, 0.08);
}

.session-item:last-child {
  border-bottom: none;
}

.session-link {
  display: block;
  text-decoration: none;
  color: inherit;
  transition: background 0.15s;
  border-radius: 8px;
  padding: 8px;
}

.session-link:hover {
  background: rgba(38, 66, 102, 0.04);
}

.session-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.session-info .muted {
  font-size: 13px;
  flex-shrink: 0;
}

.panel h3 {
  margin: 0 0 16px;
}

.status {
  color: #8a3b2f;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(22, 33, 50, 0.08);
  border-top-color: #3a8ad6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  vertical-align: middle;
  margin-right: 8px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>


