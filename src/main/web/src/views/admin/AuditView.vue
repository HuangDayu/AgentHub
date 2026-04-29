<template>
  <section class="grid">
    <article class="panel">
      <div class="toolbar">
        <label class="field">
          <span>按 actor 过滤</span>
          <input v-model="actorId" placeholder="ops-admin" />
        </label>
        <button class="secondary" @click="loadEvents">查询事件</button>
        <span class="status">{{ error }}</span>
      </div>
    </article>
    <article class="panel">
      <h2>导出作业</h2>
      <form class="form-grid" @submit.prevent="submitExportJob">
        <label class="field">
          <span>格式</span>
          <select v-model="format">
            <option value="CSV">CSV</option>
            <option value="JSON">JSON</option>
          </select>
        </label>
        <label class="field">
          <span>申请人</span>
          <input v-model="requestedBy" placeholder="audit-admin" />
        </label>
        <button class="primary" type="submit">发起导出</button>
      </form>
      <div v-if="exportJob" class="muted">
        最新作业：{{ exportJob.jobId }} / {{ exportJob.status }} / {{ formatDate(exportJob.createdAt) }}
      </div>
    </article>
    <article class="panel">
      <h2>审计事件</h2>
      <table class="table">
        <thead>
          <tr>
            <th>时间</th>
            <th>Actor</th>
            <th>动作</th>
            <th>资源</th>
            <th>结果</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="event in events" :key="event.eventId">
            <td>{{ formatDate(event.occurredAt) }}</td>
            <td>{{ event.actorId }}</td>
            <td>{{ event.action }}</td>
            <td>{{ event.resourceType }} / {{ event.resourceId }}</td>
            <td><span class="tag">{{ event.outcome }}</span></td>
          </tr>
        </tbody>
      </table>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { createExportJob, listAuditEvents } from '@/api/admin-api'
import { formatDate } from '@/common/format'
import type { AuditEvent, AuditExportJob } from '@/domain/types'

const actorId = ref('')
const format = ref('CSV')
const requestedBy = ref('audit-admin')
const events = ref<AuditEvent[]>([])
const exportJob = ref<AuditExportJob | null>(null)
const error = ref('')

onMounted(loadEvents)

async function loadEvents() {
  await execute(async () => {
    events.value = await listAuditEvents(actorId.value.trim())
  })
}

async function submitExportJob() {
  await execute(async () => {
    exportJob.value = await createExportJob(format.value, requestedBy.value.trim())
  })
}

async function execute(action: () => Promise<void>) {
  error.value = ''
  try {
    await action()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '请求失败'
  }
}
</script>

