<template>
  <section class="grid glass-float">
    <div class="page-header">
      <div>
        <h2>审计日志</h2>
        <p class="muted">全平台审计事件（AGENT / DATA_SOURCE / PERMISSION_STRATEGY）</p>
      </div>
      <p v-if="error" class="error-text">{{ error }}</p>
    </div>

    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <article class="filter-card">
        <div class="filter-grid">
          <label class="field">
            <span>工作空间</span>
            <input v-model="filter.workspaceId" placeholder="留空查询租户全部" />
          </label>
          <label class="field">
            <span>资源类型</span>
            <CustomSelect v-model="filter.resourceType" :options="resourceTypeOptions" placeholder="全部" />
          </label>
          <label class="field">
            <span>动作</span>
            <CustomSelect v-model="filter.action" :options="actionOptions" placeholder="全部" />
          </label>
          <label class="field">
            <span>状态</span>
            <CustomSelect v-model="filter.status" :options="statusOptions" placeholder="全部" />
          </label>
          <label class="field">
            <span>起始时间</span>
            <input type="datetime-local" v-model="filter.from" />
          </label>
          <label class="field">
            <span>结束时间</span>
            <input type="datetime-local" v-model="filter.to" />
          </label>
          <label class="field">
            <span>每页</span>
            <CustomSelect v-model="filter.size" :options="sizeOptions" />
          </label>
        </div>
        <div class="filter-actions">
          <CustomButton type="primary" @click="search" :disabled="loading">查询</CustomButton>
          <CustomButton type="ghost" @click="reset">重置</CustomButton>
        </div>
      </article>

      <article class="table-card float-effect">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>资源</th>
              <th>动作</th>
              <th>状态</th>
              <th>用户</th>
              <th>工作空间</th>
              <th>耗时</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="event in events" :key="event.id">
              <td>{{ event.createdAt ? formatDateTime(event.createdAt) : '-' }}</td>
              <td>
                <strong>{{ event.resourceType }}</strong>
                <div class="muted">{{ event.resourceName || event.resourceId }}</div>
              </td>
              <td><span class="tag tag-info">{{ event.action }}</span></td>
              <td>
                <span :class="['tag', statusClass(event.status)]">{{ event.status }}</span>
              </td>
              <td>{{ event.actorId }}</td>
              <td class="muted">{{ event.workspaceId || '-' }}</td>
              <td>{{ event.elapsedMs ?? 0 }} ms</td>
            </tr>
          </tbody>
        </table>
        <p v-if="events.length === 0" class="empty-hint">无数据</p>
      </article>

      <div class="pagination">
        <CustomButton type="ghost" :disabled="filter.page <= 1" @click="prev">上一页</CustomButton>
        <span class="muted">第 {{ filter.page }} 页 / 共 {{ totalPages }} 页（共 {{ total }} 条）</span>
        <CustomButton type="ghost" :disabled="filter.page >= totalPages" @click="next">下一页</CustomButton>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { formatDateTime } from '@/common/format'
import {
  listAuditLogActions,
  listAuditLogResourceTypes,
  queryAuditLogs,
} from '@/api/audit-log-api'
import { useWorkspaceStore } from '@/store/workspace-store'
import type { AuditEvent, AuditLogQuery } from '@/types/audit-log'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const events = ref<AuditEvent[]>([])
const total = ref(0)
const resourceTypes = ref<string[]>([])
const actions = ref<string[]>([])
const error = ref('')
const loading = ref(false)
const filter = reactive<Required<AuditLogQuery>>({
  workspaceId: '',
  resourceType: '',
  resourceId: '',
  actorId: '',
  action: '',
  status: '',
  from: '',
  to: '',
  page: 1,
  size: 50,
})

const resourceTypeOptions = computed(() => resourceTypes.value.map(rt => ({ value: rt, label: rt })))
const actionOptions = computed(() => actions.value.map(a => ({ value: a, label: a })))
const statusOptions = [
  { value: 'SUCCESS', label: '成功' },
  { value: 'FAILED', label: '失败' },
  { value: 'DENIED', label: '拒绝' },
]
const sizeOptions = [
  { value: 20, label: '20' },
  { value: 50, label: '50' },
  { value: 100, label: '100' },
]

const selectionReady = computed(() => Boolean(store.tenantId))
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / filter.size)))

function statusClass(status: string): string {
  if (status === 'SUCCESS') return 'tag-success'
  if (status === 'FAILED') return 'tag-error'
  if (status === 'DENIED') return 'tag-warn'
  return ''
}

async function loadDictionaries() {
  try {
    resourceTypes.value = await listAuditLogResourceTypes()
    actions.value = await listAuditLogActions()
  } catch (reason) {
    console.error('加载字典失败', reason)
  }
}

async function search() {
  if (!store.tenantId) return
  loading.value = true
  error.value = ''
  try {
    const query: AuditLogQuery = {
      workspaceId: filter.workspaceId || undefined,
      resourceType: filter.resourceType || undefined,
      resourceId: filter.resourceId || undefined,
      actorId: filter.actorId || undefined,
      action: filter.action || undefined,
      status: filter.status || undefined,
      from: filter.from ? new Date(filter.from).toISOString() : undefined,
      to: filter.to ? new Date(filter.to).toISOString() : undefined,
      page: filter.page,
      size: filter.size,
    }
    const result = await queryAuditLogs(store.tenantId, query)
    events.value = result.items
    total.value = result.total
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '查询失败'
  } finally {
    loading.value = false
  }
}

function reset() {
  filter.workspaceId = ''
  filter.resourceType = ''
  filter.resourceId = ''
  filter.actorId = ''
  filter.action = ''
  filter.status = ''
  filter.from = ''
  filter.to = ''
  filter.page = 1
  search()
}

function prev() {
  if (filter.page <= 1) return
  filter.page -= 1
  search()
}

function next() {
  if (filter.page >= totalPages.value) return
  filter.page += 1
  search()
}

onMounted(() => { loadDictionaries(); search() })
watch(() => store.tenantId, search)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-end; }
.error-text { color: var(--color-error, #ef4444); }
.muted { color: var(--color-text-light); font-size: 0.75rem; }
.tag { padding: 2px 8px; border-radius: 4px; background: var(--bg-hover); font-size: 0.75rem; }
.tag-info { background: rgba(58,123,213,0.12); color: var(--color-primary); }
.tag-success { background: rgba(34,197,94,0.14); color: var(--color-success); }
.tag-error { background: rgba(239,68,68,0.14); color: var(--color-error); }
.tag-warn { background: rgba(245,158,11,0.14); color: #d97706; }
.filter-card { background: var(--bg-card-solid); padding: 1rem; border-radius: 8px; }
.filter-grid {
  display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.75rem;
}
.field { display: flex; flex-direction: column; gap: 0.25rem; }
.field input, .field select {
  padding: 6px 10px; border: 1px solid var(--color-border); border-radius: 6px; background: var(--bg-card-solid);
}
.filter-actions { display: flex; gap: 0.5rem; margin-top: 0.75rem; }
.empty-hint { color: var(--color-text-light); padding: 1rem; text-align: center; }
.pagination {
  display: flex; align-items: center; gap: 1rem; justify-content: center; padding: 0.5rem 0;
}
</style>
