<template>
  <section class="subagent-page">
    <div v-if="error" class="error-toast fade-in">
      <span>{{ error }}</span>
      <button @click="error = ''">&times;</button>
    </div>

    <div class="page-header">
      <h2>子Agent运行时监控</h2>
    </div>

    <div class="agent-selector">
      <label>父Agent：</label>
      <CustomSelect v-model="selectedAgentId" @change="loadSubagents" :options="agentOptions" placeholder="请选择 Agent" />
    </div>

    <div v-if="subagents.length" class="subagent-list">
      <div v-for="sub in subagents" :key="sub.id" class="subagent-card"
           :class="{ expanded: selectedSubagentId === sub.id }"
           @click="selectedSubagentId = selectedSubagentId === sub.id ? '' : sub.id">
        <div class="card-header">
          <div class="card-info">
            <h3>{{ sub.name }}</h3>
            <span class="desc">{{ sub.description || '无描述' }}</span>
          </div>
          <span :class="['status-badge', sub.status.toLowerCase()]">{{ sub.status }}</span>
        </div>
        <div class="card-meta">
          <span>系统提示: {{ (sub.systemPrompt || '无').slice(0, 80) }}{{ (sub.systemPrompt || '').length > 80 ? '...' : '' }}</span>
          <span>创建时间: {{ formatTime(sub.createdAt) }}</span>
        </div>
        <div v-if="selectedSubagentId === sub.id" class="card-detail">
          <div class="detail-section">
            <strong>父Agent ID:</strong> {{ sub.parentAgentId }}
          </div>
          <div class="detail-section" v-if="sub.parentSubagentId">
            <strong>父Subagent ID:</strong> {{ sub.parentSubagentId }}
          </div>
          <div class="detail-section">
            <strong>模型配置:</strong> {{ sub.modelConfigId || '继承父Agent' }}
          </div>
          <div class="detail-section">
            <strong>完整提示词:</strong>
            <pre>{{ sub.systemPrompt || '无' }}</pre>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="selectedAgentId" class="empty-state">
      <p>暂无子Agent（Subagent 由 Agent 运行时自动创建）</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CustomSelect from '@/components/CustomSelect.vue'
import { listAgents } from '@/api/agent-api'
import { listSubagents } from '@/api/subagent-api'
import { useWorkspaceStore } from '@/store/workspace-store'
import type { Subagent } from '@/types/subagent'
import type { Agent } from '@/types/agent'

const agentOptions = computed(() => agents.value.map(a => ({ value: a.id, label: a.name })))

const store = useWorkspaceStore()
const error = ref('')
const agents = ref<Agent[]>([])
const selectedAgentId = ref('')
const subagents = ref<Subagent[]>([])
const selectedSubagentId = ref('')

onMounted(async () => { try { await performMountLoad() } catch (e: any) { error.value = e.message || '加载失败' } })

async function performMountLoad(): Promise<void> {
  agents.value = await listAgents(getSelection())
  if (agents.value.length) { selectedAgentId.value = agents.value[0].id; await loadSubagents() }
}

function getSelection() {
  return { tenantId: store.tenantId!, workspaceId: store.workspaceId! }
}

async function loadSubagents() {
  if (!selectedAgentId.value) return
  try {
    subagents.value = await listSubagents(getSelection(), selectedAgentId.value)
  } catch (e: any) {
    error.value = e.message || '加载子Agent失败'
  }
}

function formatTime(iso: string) {
  if (!iso) return '-'
  return new Date(iso).toLocaleString('zh-CN')
}
</script>

<style scoped>
.subagent-page { padding: 24px; max-width: 960px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; }
.agent-selector { margin-bottom: 20px; display: flex; align-items: center; gap: 8px; }
.agent-selector select { padding: 6px 12px; border: 1px solid #ccc; border-radius: 4px; }
.subagent-list { display: flex; flex-direction: column; gap: 12px; }
.subagent-card { border: 1px solid #e0e0e0; border-radius: 8px; padding: 14px 16px; background: #fff; cursor: pointer; }
.subagent-card:hover { border-color: #1976d2; }
.subagent-card.expanded { border-color: #1976d2; box-shadow: 0 2px 8px rgba(25,118,210,0.1); }
.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px; }
.card-info h3 { margin: 0; font-size: 15px; }
.card-info .desc { font-size: 12px; color: #666; }
.status-badge { padding: 2px 8px; border-radius: 4px; font-size: 11px; white-space: nowrap; }
.status-badge.active { background: #e8f5e9; color: #2e7d32; }
.status-badge.inactive { background: #fbe9e7; color: #c62828; }
.status-badge.completed { background: #e8f5e9; color: #2e7d32; }
.status-badge.running { background: #fff3e0; color: #e65100; }
.status-badge.interrupted { background: #fce4ec; color: #c62828; }
.card-meta { font-size: 12px; color: #888; display: flex; flex-direction: column; gap: 2px; }
.card-detail { margin-top: 10px; padding-top: 10px; border-top: 1px solid #eee; }
.detail-section { margin-bottom: 6px; font-size: 13px; }
.detail-section pre { background: #f5f5f5; padding: 8px; border-radius: 4px; font-size: 12px; margin: 4px 0 0; white-space: pre-wrap; }
.empty-state { text-align: center; padding: 60px 20px; color: #999; }
.error-toast { background: #f44336; color: #fff; padding: 10px 16px; border-radius: 4px; margin-bottom: 16px; }
</style>
