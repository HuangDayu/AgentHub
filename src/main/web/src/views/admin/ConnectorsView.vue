<template>
  <section class="grid">
    <!-- 创建/编辑连接器表单 -->
    <article class="panel">
      <h2>{{ editingId ? '编辑连接器' : '创建连接器' }}</h2>
      <form class="form-grid" @submit.prevent="submitForm">
        <label class="field">
          <span>连接器名称</span>
          <input v-model="form.name" placeholder="飞书知识同步" />
        </label>
        <label class="field">
          <span>连接器类型</span>
          <input v-model="form.type" placeholder="FEISHU / OSS / GITHUB" />
        </label>
        <label class="field">
          <span>配置 (JSON)</span>
          <textarea v-model="form.configJson" rows="3" placeholder='{"apiKey":"xxx"}'></textarea>
        </label>
        <div class="toolbar">
          <button class="primary" type="submit">{{ editingId ? '保存修改' : '创建连接器' }}</button>
          <button v-if="editingId" class="ghost" type="button" @click="resetForm">取消编辑</button>
          <button class="secondary" type="button" @click="loadConnectors">刷新列表</button>
        </div>
      </form>
      <p class="status" v-if="error">{{ error }}</p>
      <p class="muted" v-if="syncJob">
        最近同步：{{ syncJob.connectorId }} / {{ syncJob.status }} / {{ formatDate(syncJob.requestedAt) }}
      </p>
    </article>

    <!-- 同步作业日志 -->
    <article class="panel" v-if="selectedSyncJobs.length">
      <h2>同步作业记录 — {{ selectedSyncName }}</h2>
      <table class="table">
        <thead>
          <tr>
            <th>作业 ID</th>
            <th>状态</th>
            <th>发起时间</th>
            <th>完成时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="job in selectedSyncJobs" :key="job.id">
            <td>{{ job.id }}</td>
            <td><span class="tag">{{ job.status }}</span></td>
            <td>{{ formatDate(job.requestedAt) }}</td>
            <td>{{ formatDate(job.finishedAt) }}</td>
          </tr>
        </tbody>
      </table>
      <button class="ghost" @click="selectedSyncJobs = []">关闭</button>
    </article>

    <!-- 连接器列表 -->
    <article class="panel">
      <h2>连接器列表</h2>
      <table class="table">
        <thead>
          <tr>
            <th>名称</th>
            <th>类型</th>
            <th>状态</th>
            <th>最近同步</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="connector in connectors" :key="connector.id">
            <td>{{ connector.name }}</td>
            <td>{{ connector.type }}</td>
            <td><span class="tag" :class="connector.enabled ? 'tag-on' : 'tag-off'">{{ connector.enabled ? '启用' : '停用' }}</span></td>
            <td>{{ formatDate(connector.lastSyncedAt) }}</td>
            <td>
              <div class="chip-row">
                <button class="ghost" @click="startEdit(connector)">编辑</button>
                <button class="ghost" @click="toggleEnabled(connector)">
                  {{ connector.enabled ? '禁用' : '启用' }}
                </button>
                <button class="ghost" @click="sync(connector.id)">同步</button>
                <button class="ghost" @click="viewSyncJobs(connector)">作业</button>
                <button class="ghost danger" @click="removeConnector(connector.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <p class="muted" v-if="!connectors.length">暂无连接器</p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  createConnector,
  deleteConnector,
  listConnectors,
  listSyncJobs,
  setConnectorEnabled,
  triggerSync,
  updateConnector,
} from '@/api/admin-api'
import { formatDate } from '@/common/format'
import type { Connector, SyncJob } from '@/domain/types'

interface ConnectorForm {
  name: string
  type: string
  configJson: string
}

const connectors = ref<Connector[]>([])
const syncJob = ref<SyncJob | null>(null)
const editingId = ref<string | null>(null)
const selectedSyncJobs = ref<SyncJob[]>([])
const selectedSyncName = ref('')
const error = ref('')
const form = ref<ConnectorForm>({ name: '', type: 'FEISHU', configJson: '{}' })

onMounted(loadConnectors)

async function loadConnectors() {
  await run(async () => {
    connectors.value = await listConnectors()
  })
}

async function submitForm() {
  let config: Record<string, string>
  try {
    config = JSON.parse(form.value.configJson || '{}')
  } catch {
    error.value = '配置 JSON 格式不正确'
    return
  }
  await run(async () => {
    if (editingId.value) {
      await updateConnector(editingId.value, form.value.name.trim(), form.value.type.trim(), config)
    } else {
      await createConnector(form.value.name.trim(), form.value.type.trim(), config)
    }
    resetForm()
    await loadConnectors()
  })
}

function startEdit(connector: Connector) {
  editingId.value = connector.id
  form.value = {
    name: connector.name,
    type: connector.type,
    configJson: JSON.stringify(connector.config ?? {}, null, 2),
  }
}

function resetForm() {
  editingId.value = null
  form.value = { name: '', type: 'FEISHU', configJson: '{}' }
}

async function toggleEnabled(connector: Connector) {
  await run(async () => {
    await setConnectorEnabled(connector.id, !connector.enabled)
    await loadConnectors()
  })
}

async function sync(connectorId: string) {
  await run(async () => {
    syncJob.value = await triggerSync(connectorId)
  })
}

async function viewSyncJobs(connector: Connector) {
  await run(async () => {
    selectedSyncName.value = connector.name
    selectedSyncJobs.value = await listSyncJobs(connector.id)
  })
}

async function removeConnector(id: string) {
  if (!confirm('确定删除此连接器？')) return
  await run(async () => {
    await deleteConnector(id)
    await loadConnectors()
  })
}

async function run(action: () => Promise<void>) {
  error.value = ''
  try {
    await action()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '请求失败'
  }
}
</script>

<style scoped>
.tag-on {
  background: rgba(38, 166, 91, 0.14);
}
.tag-off {
  background: rgba(149, 63, 43, 0.14);
}
.danger {
  color: #c0392b;
}
textarea {
  width: 100%;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(39, 65, 93, 0.14);
  background: rgba(249, 251, 255, 0.92);
  font-family: 'Cascadia Code', 'Consolas', monospace;
  resize: vertical;
}
</style>

