<template>
  <section class="grid glass-float">
    <div class="page-header">
      <div>
        <h2>Agent 数据源</h2>
        <p class="muted">配置 Agent 可访问的外部数据源（数据库 / API / 文件）</p>
      </div>
      <p v-if="error" class="error-text">{{ error }}</p>
    </div>

    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <article class="table-card float-effect">
        <table>
          <thead>
            <tr>
              <th>名称</th>
              <th>协议</th>
              <th>端点</th>
              <th>状态</th>
              <th>启用</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="ds in dataSources" :key="ds.id">
              <td>
                <strong>{{ ds.name }}</strong>
                <div class="muted">{{ ds.id }}</div>
              </td>
              <td><span class="tag tag-info">{{ ds.protocol }}</span></td>
              <td class="endpoint-cell" :title="ds.endpointUri">{{ ds.endpointUri }}</td>
              <td><span class="tag">{{ ds.status }}</span></td>
              <td>
                <span :class="['tag', ds.enabled ? 'tag-success' : 'tag-error']">
                  {{ ds.enabled ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ ds.updatedAt ? formatDateTime(ds.updatedAt) : '-' }}</td>
              <td>
                <div class="chip-row">
                  <CustomButton type="ghost" @click="openDetail(ds)">详情</CustomButton>
                  <CustomButton type="ghost" @click="openEdit(ds)">编辑</CustomButton>
                  <CustomButton type="ghost" @click="testConnection(ds.id)">测试</CustomButton>
                  <CustomButton v-if="ds.enabled" type="ghost" @click="toggle(ds.id, false)">禁用</CustomButton>
                  <CustomButton v-else type="ghost" @click="toggle(ds.id, true)">启用</CustomButton>
                  <CustomButton type="ghost" @click="remove(ds.id)">删除</CustomButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </article>
    </template>

    <!-- 创建/编辑 弹窗 -->
    <ModalDialog v-model:visible="showForm" :title="editingId ? '编辑数据源' : '创建数据源'" size="large"
                 @confirm="submit" @close="cancelForm" :confirm-text="editingId ? '更新' : '创建'">
      <form class="field-grid">
        <div class="grid-2">
          <label class="field">
            <span>名称 *</span>
            <input v-model="form.name" required placeholder="orders-db" />
          </label>
          <label class="field">
            <span>协议 *</span>
            <CustomSelect v-model="form.protocol" :options="protocolOptions" />
          </label>
        </div>
        <label class="field">
          <span>端点 URI *</span>
          <input v-model="form.endpointUri" required :placeholder="currentExampleUri || 'jdbc:postgresql://host:5432/db'" />
        </label>
        <label class="field">
          <span>描述</span>
          <textarea v-model="form.description" placeholder="数据源描述"></textarea>
        </label>
        <label class="field">
          <span>属性 (JSON 字符串)</span>
          <textarea v-model="form.propertiesJson" placeholder='{"username":"app","password":"***"}'></textarea>
        </label>
        <p v-if="currentSyntaxHint" class="syntax-hint">{{ currentSyntaxHint }}</p>
      </form>
    </ModalDialog>

    <!-- 详情抽屉 -->
    <ModalDialog v-model:visible="showDetail" :title="detail?.name || '详情'" size="xxlarge"
                 @close="showDetail = false" :show-footer="false">
      <div v-if="detail" class="detail-tabs">
        <div class="tabs">
          <button :class="{ active: detailTab === 'info' }" @click="detailTab = 'info'">基本信息</button>
          <button :class="{ active: detailTab === 'schema' }" @click="detailTab = 'schema'">Schema</button>
          <button :class="{ active: detailTab === 'permission' }" @click="detailTab = 'permission'">权限策略</button>
        </div>
        <div v-if="detailTab === 'info'" class="tab-content">
          <dl class="info-grid">
            <dt>ID</dt><dd>{{ detail.id }}</dd>
            <dt>协议</dt><dd>{{ detail.protocol }}</dd>
            <dt>端点</dt><dd class="endpoint-cell">{{ detail.endpointUri }}</dd>
            <dt>状态</dt><dd>{{ detail.status }}</dd>
            <dt>启用</dt><dd>{{ detail.enabled ? '是' : '否' }}</dd>
            <dt v-if="detail.lastErrorMessage">最近错误</dt>
            <dd v-if="detail.lastErrorMessage" class="error-text">{{ detail.lastErrorMessage }}</dd>
            <dt>创建时间</dt><dd>{{ detail.createdAt ? formatDateTime(detail.createdAt) : '-' }}</dd>
            <dt>更新时间</dt><dd>{{ detail.updatedAt ? formatDateTime(detail.updatedAt) : '-' }}</dd>
          </dl>
        </div>
        <div v-else-if="detailTab === 'schema'" class="tab-content">
          <SchemaPanel :data-source-id="detail.id" />
        </div>
        <div v-else-if="detailTab === 'permission'" class="tab-content">
          <p class="muted">当前绑定：{{ detail.permissionPolicyId || '（未绑定）' }}</p>
        </div>
      </div>
    </ModalDialog>

    <!-- 测试结果弹窗 -->
    <ModalDialog v-model:visible="showTestResult" :title="'连接测试结果'" size="small"
                 @close="showTestResult = false" :show-footer="false">
      <div v-if="testResult" class="test-result">
        <p>
          <span :class="['tag', testResult.success ? 'tag-success' : 'tag-error']">
            {{ testResult.success ? '成功' : '失败' }}
          </span>
        </p>
        <p>{{ testResult.message }}</p>
        <p class="muted">耗时 {{ testResult.elapsedMs }} ms</p>
      </div>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { showConfirm } from '@/utils/confirm'
import { formatDateTime } from '@/common/format'
import {
  createAgentDataSource,
  deleteAgentDataSource,
  disableAgentDataSource,
  enableAgentDataSource,
  listAgentDataSources,
  testAgentDataSource,
  updateAgentDataSource,
} from '@/api/agent-data-source-api'
import { listAgentDataSourceComponents } from '@/api/agent-data-source-component-api'
import { useWorkspaceStore } from '@/store/workspace-store'
import type {
  AgentDataSource,
  AgentDataSourceDescriptor,
  AgentDataSourceTestResult,
} from '@/types/agent-data-source'
import CustomSelect from '@/components/CustomSelect.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'
import SchemaPanel from '@/components/datasource/SchemaPanel.vue'

const store = useWorkspaceStore()
const dataSources = ref<AgentDataSource[]>([])
const components = ref<AgentDataSourceDescriptor[]>([])
const error = ref('')
const showForm = ref(false)
const showDetail = ref(false)
const showTestResult = ref(false)
const testResult = ref<AgentDataSourceTestResult | null>(null)
const detail = ref<AgentDataSource | null>(null)
const detailTab = ref<'info' | 'schema' | 'permission'>('info')
const editingId = ref<string | null>(null)

const form = reactive({
  name: '',
  description: '',
  protocol: 'JDBC',
  endpointUri: '',
  propertiesJson: '',
})

const protocolOptions = computed(() => components.value.map(c => ({ value: c.protocol, label: `${c.displayName} (${c.protocol})` })))

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))
const currentExampleUri = computed(() => components.value.find((c) => c.protocol === form.protocol)?.exampleUri || '')
const currentSyntaxHint = computed(() => components.value.find((c) => c.protocol === form.protocol)?.syntaxHint || '')

async function loadAll() {
  if (!selectionReady.value) { dataSources.value = []; return }
  error.value = ''
  try {
    dataSources.value = await listAgentDataSources({
      tenantId: store.tenantId,
      workspaceId: store.workspaceId,
    })
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载失败'
  }
}

async function loadComponents() {
  if (components.value.length > 0) return
  try {
    components.value = await listAgentDataSourceComponents()
  } catch (reason) {
    console.error('加载协议描述符失败', reason)
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  showForm.value = true
}

function openEdit(ds: AgentDataSource) {
  editingId.value = ds.id
  form.name = ds.name
  form.description = ds.description || ''
  form.protocol = ds.protocol
  form.endpointUri = ds.endpointUri
  form.propertiesJson = ds.propertiesJson || ''
  showForm.value = true
}

function openDetail(ds: AgentDataSource) {
  detail.value = ds
  detailTab.value = 'info'
  showDetail.value = true
}

function resetForm() {
  form.name = ''
  form.description = ''
  form.protocol = 'JDBC'
  form.endpointUri = ''
  form.propertiesJson = ''
}

function cancelForm() {
  editingId.value = null
  showForm.value = false
}

async function submit() {
  if (!form.name.trim() || !form.endpointUri.trim()) return
  try {
    const data: Partial<AgentDataSource> = {
      name: form.name,
      description: form.description,
      protocol: form.protocol,
      endpointUri: form.endpointUri,
      propertiesJson: form.propertiesJson,
    }
    if (editingId.value) {
      await updateAgentDataSource(
        { tenantId: store.tenantId, workspaceId: store.workspaceId },
        editingId.value,
        data
      )
    } else {
      await createAgentDataSource(
        { tenantId: store.tenantId, workspaceId: store.workspaceId },
        data
      )
    }
    cancelForm()
    await loadAll()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '保存失败'
  }
}

async function testConnection(id: string) {
  try {
    testResult.value = await testAgentDataSource(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      id
    )
    showTestResult.value = true
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '测试失败'
  }
}

async function toggle(id: string, enable: boolean) {
  try {
    const selection = { tenantId: store.tenantId, workspaceId: store.workspaceId }
    if (enable) {
      await enableAgentDataSource(selection, id)
    } else {
      await disableAgentDataSource(selection, id)
    }
    await loadAll()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '切换失败'
  }
}

async function remove(id: string) {
  if (!await showConfirm('确定删除该数据源？相关 Schema 也会被清理。')) return
  try {
    await deleteAgentDataSource(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      id
    )
    await loadAll()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '删除失败'
  }
}

onMounted(() => { loadAll(); loadComponents() })
window.addEventListener('global-add', () => { if (selectionReady.value) openCreate() })
watch(() => [store.tenantId, store.workspaceId], loadAll)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-end; }
.error-text { color: var(--color-error, #ef4444); }
.muted { color: var(--color-text-light); font-size: 0.75rem; }
.endpoint-cell { max-width: 240px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tag { padding: 2px 8px; border-radius: 4px; background: var(--bg-hover); font-size: 0.75rem; }
.tag-info { background: rgba(58,123,213,0.12); color: var(--color-primary); }
.tag-success { background: rgba(34,197,94,0.14); color: var(--color-success); }
.tag-error { background: rgba(239,68,68,0.14); color: var(--color-error); }
.chip-row { display: flex; gap: 4px; flex-wrap: wrap; }
.field-grid { display: flex; flex-direction: column; gap: 0.75rem; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
.field { display: flex; flex-direction: column; gap: 0.25rem; }
.field input, .field textarea, .field select {
  padding: 6px 10px; border: 1px solid var(--color-border); border-radius: 6px; background: var(--bg-card-solid);
}
.syntax-hint {
  font-family: var(--font-mono, monospace); font-size: 0.8rem;
  background: var(--bg-hover); padding: 6px 10px; border-radius: 6px; color: var(--color-text-light);
}
.detail-tabs { display: flex; flex-direction: column; gap: 1rem; }
.tabs { display: flex; gap: 0.5rem; border-bottom: 1px solid var(--color-border); }
.tabs button {
  padding: 8px 16px; border: none; background: transparent;
  cursor: pointer; color: var(--color-text-light); border-bottom: 2px solid transparent;
}
.tabs button.active {
  color: var(--color-primary); border-bottom-color: var(--color-primary);
}
.tab-content { padding: 0.5rem 0; }
.info-grid { display: grid; grid-template-columns: max-content 1fr; gap: 8px 16px; }
.info-grid dt { font-weight: 600; color: var(--color-text-light); }
.info-grid dd { margin: 0; word-break: break-all; }
.test-result { display: flex; flex-direction: column; gap: 0.5rem; }
</style>
