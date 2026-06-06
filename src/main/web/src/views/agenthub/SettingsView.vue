<template>
  <section class="settings-page fade-in glass-float">
    <div class="page-header">
      <div>
        <h2>设置</h2>
        <p class="muted">管理您的AgentHub配置和功能模块</p>
      </div>
      <CustomButton type="ghost" class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        返回
      </CustomButton>
    </div>


    <div class="settings-grid">
      <!-- 工作区管理 -->
      <article class="settings-card" @click="navigateTo('workspace')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>工作区管理</h3>
          <p class="muted">管理工作区，选择工作区开始使用</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 知识库 -->
      <article class="settings-card" @click="navigateTo('knowledge')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>知识库</h3>
          <p class="muted">管理文档和知识库内容</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 记忆管理 -->
      <article class="settings-card" @click="navigateTo('memories')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>记忆管理</h3>
          <p class="muted">管理对话记忆和上下文</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- Agent管理 -->
      <article class="settings-card" @click="navigateTo('agents')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>Agent管理</h3>
          <p class="muted">创建和管理智能Agent</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- Agent配置 -->
      <article class="settings-card" @click="navigateTo('agent-configs')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>Agent配置</h3>
          <p class="muted">配置Agent参数和行为</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- Agent团队 -->
      <article class="settings-card" @click="navigateTo('teams')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>Agent团队</h3>
          <p class="muted">管理多Agent协作团队</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 向量库 -->
      <article class="settings-card" @click="navigateTo('vector-stores')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <ellipse cx="12" cy="5" rx="9" ry="3"/>
            <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/>
            <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>向量数据库</h3>
          <p class="muted">管理向量存储配置</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 大模型 -->
      <article class="settings-card" @click="navigateTo('models')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" y1="22.08" x2="12" y2="12"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>大模型</h3>
          <p class="muted">配置和管理AI模型</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 策略管理 -->
      <article class="settings-card" @click="navigateTo('strategies')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="12 2 2 7 12 12 22 7 12 2"/>
            <polyline points="2 17 12 22 22 17"/>
            <polyline points="2 12 12 17 22 12"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>策略配置</h3>
          <p class="muted">配置检索和执行策略</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 提示词模板 -->
      <article class="settings-card" @click="navigateTo('prompt-templates')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>提示词</h3>
          <p class="muted">管理提示词模板库</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 工作流 -->
      <article class="settings-card" @click="navigateTo('workflows')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>工作流</h3>
          <p class="muted">管理工作流和自动化</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- MCP工具 -->
      <article class="settings-card" @click="navigateTo('mcp-tools')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-3.77 3.77a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-3.77 3.77a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>MCP工具</h3>
          <p class="muted">管理MCP工具集成</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 技能管理 -->
      <article class="settings-card" @click="navigateTo('skills')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>技能</h3>
          <p class="muted">管理Agent技能和能力</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 系统工具 -->
      <article class="settings-card" @click="navigateTo('system-tools')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
            <line x1="8" y1="21" x2="16" y2="21"/>
            <line x1="12" y1="17" x2="12" y2="21"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>系统工具</h3>
          <p class="muted">系统工具和实用功能</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 定时任务 -->
      <article class="settings-card" @click="navigateTo('scheduled-tasks')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>定时任务</h3>
          <p class="muted">定时任务调度管理</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 数据源 -->
      <article class="settings-card" @click="navigateTo('agent-data-sources')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <ellipse cx="12" cy="5" rx="9" ry="3"/>
            <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/>
            <path d="M3 12c0 1.66 4 3 9 3s9-1.34 9-3"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>数据源</h3>
          <p class="muted">配置 Agent 可访问的数据库/API/文件</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>

      <!-- 审计日志 -->
      <article class="settings-card" @click="navigateTo('audit-logs')">
        <div class="card-icon float-effect">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="9" y1="13" x2="15" y2="13"/>
            <line x1="9" y1="17" x2="15" y2="17"/>
          </svg>
        </div>
        <div class="card-content float-effect">
          <h3>审计日志</h3>
          <p class="muted">查看全平台操作审计事件</p>
        </div>
        <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </article>
    </div>
  
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const router = useRouter()

function goBack() {
  router.push('/agenthub')
}

function navigateTo(path: string) {
  router.push(`/agenthub/${path}`)
}
</script>

<style scoped>
.settings-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: 1px solid var(--color-border);
  background: var(--bg-card);
  border-radius: 12px;
  font: inherit;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  color: var(--color-primary-dark);
  backdrop-filter: blur(8px);
}

.back-btn svg {
  width: 18px;
  height: 18px;
}

.back-btn:hover {
  border-color: var(--color-primary);
  background: var(--bg-card-solid);
  transform: translateX(-2px);
}

.page-header h2 {
  margin: 0;
  font-size: 1.75rem;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.muted {
  color: var(--color-text-muted);
  margin: 4px 0 0;
  font-size: 0.9rem;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.settings-card {
  background: var(--bg-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(12px);
  box-shadow: 0 4px 12px rgba(26, 30, 43, 0.06);
}

.settings-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(26, 30, 43, 0.12);
  border-color: rgba(58, 123, 213, 0.2);
  background: var(--bg-card-hover);
}

.card-icon {
  width: 48px;
  height: 48px;
  padding: 12px;
  background: linear-gradient(135deg, var(--color-primary-subtle), rgba(58, 138, 214, 0.05));
  border-radius: 12px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.card-icon svg {
  width: 100%;
  height: 100%;
}

.card-content {
  flex: 1;
}

.card-content h3 {
  margin: 0 0 4px;
  font-size: 1rem;
  color: var(--color-text);
}

.arrow-icon {
  width: 20px;
  height: 20px;
  color: var(--color-text-light);
  flex-shrink: 0;
  transition: transform 0.25s ease;
}

.settings-card:hover .arrow-icon {
  transform: translateX(4px);
  color: var(--color-primary);
}

/* Animation */
.fade-in {
  animation: fade-in 0.4s ease forwards;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* Responsive */
/* Responsive */
@media (max-width: 768px) {
  .settings-page {
    padding: 16px;
  }

  .settings-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
