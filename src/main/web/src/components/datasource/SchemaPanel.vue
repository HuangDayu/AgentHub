<template>
  <div class="schema-panel">
    <div class="panel-actions">
      <CustomButton type="primary" @click="introspect" :disabled="loading">自动发现</CustomButton>
      <CustomButton type="primary" @click="openAddTable">新增表</CustomButton>
    </div>

    <p v-if="error" class="error-text">{{ error }}</p>

    <article v-if="schema" class="table-card">
      <div class="schema-meta">
        <span>共 {{ schema.tables.length }} 张表</span>
        <span v-if="schema.introspected" class="tag tag-info">已发现</span>
        <span v-else class="tag">手动</span>
        <span v-if="schema.lastIntrospectedAt" class="muted">
          上次发现：{{ formatDateTime(schema.lastIntrospectedAt) }}
        </span>
      </div>
      <table v-if="schema.tables.length > 0">
        <thead>
          <tr>
            <th>表名</th>
            <th>显示名</th>
            <th>字段数</th>
            <th>允许操作</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in schema.tables" :key="t.id">
            <td><strong>{{ t.name }}</strong></td>
            <td>{{ t.displayName || '-' }}</td>
            <td>{{ t.columns.length }}</td>
            <td>
              <span v-for="op in t.allowedOperations" :key="op" class="tag tag-info">{{ op }}</span>
            </td>
            <td>
              <CustomButton type="ghost" @click="removeTable(t.id)">删除</CustomButton>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty-hint">暂无表。可点击"自动发现"扫描数据源，或"新增表"手动添加。</p>
    </article>
    <p v-else class="empty-hint">加载中…</p>

    <ModalDialog v-model:visible="showAddForm" title="新增表" size="medium"
                 @confirm="submitAddTable" @close="showAddForm = false" confirm-text="创建">
      <form class="field-grid">
        <label class="field">
          <span>表名 *</span>
          <input v-model="newTable.name" required placeholder="users" />
        </label>
        <label class="field">
          <span>显示名</span>
          <input v-model="newTable.displayName" placeholder="用户表" />
        </label>
        <label class="field">
          <span>描述</span>
          <textarea v-model="newTable.description" placeholder="表用途"></textarea>
        </label>
        <div class="columns-editor">
          <h4>字段</h4>
          <div v-for="(col, idx) in newTable.columns" :key="idx" class="column-row">
            <input v-model="col.name" placeholder="字段名" />
            <input v-model="col.type" placeholder="类型, 如 BIGINT" />
            <select v-model="col.isPrimary">
              <option :value="true">主键</option>
              <option :value="false">非主键</option>
            </select>
            <select v-model="col.nullable">
              <option :value="true">可空</option>
              <option :value="false">非空</option>
            </select>
            <input v-model="col.description" placeholder="描述" />
            <button type="button" @click="newTable.columns.splice(idx, 1)" class="btn-remove">×</button>
          </div>
          <button type="button" @click="addColumn" class="btn-add">+ 字段</button>
        </div>
      </form>
    </ModalDialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { addTable, deleteTable, getSchema, introspectSchema } from '@/api/data-source-schema-api'
import { formatDateTime } from '@/common/format'
import { showConfirm } from '@/utils/confirm'
import { useWorkspaceStore } from '@/store/workspace-store'
import type { DataSourceSchema, DataSourceTable } from '@/types/agent-data-source'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'

const props = defineProps<{ dataSourceId: string }>()
const store = useWorkspaceStore()
const schema = ref<DataSourceSchema | null>(null)
const error = ref('')
const loading = ref(false)
const showAddForm = ref(false)

const newTable = reactive({
  name: '',
  displayName: '',
  description: '',
  columns: [] as Array<{ name: string; type: string; isPrimary: boolean; nullable: boolean; description: string }>,
})

async function load() {
  if (!store.tenantId || !store.workspaceId) return
  error.value = ''
  try {
    schema.value = await getSchema(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      props.dataSourceId
    )
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载失败'
  }
}

async function introspect() {
  if (!store.tenantId) return
  loading.value = true
  try {
    schema.value = await introspectSchema(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      props.dataSourceId
    )
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '自动发现失败'
  } finally {
    loading.value = false
  }
}

function openAddTable() {
  newTable.name = ''
  newTable.displayName = ''
  newTable.description = ''
  newTable.columns = [{ name: 'id', type: 'BIGINT', isPrimary: true, nullable: false, description: '' }]
  showAddForm.value = true
}

function addColumn() {
  newTable.columns.push({ name: '', type: 'VARCHAR', isPrimary: false, nullable: true, description: '' })
}

async function submitAddTable() {
  if (!newTable.name.trim()) return
  try {
    schema.value = await addTable(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      props.dataSourceId,
      {
        name: newTable.name,
        displayName: newTable.displayName,
        description: newTable.description,
        columns: newTable.columns.filter((c) => c.name.trim()),
      } as Partial<DataSourceTable>
    )
    showAddForm.value = false
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '新增表失败'
  }
}

async function removeTable(tableId: string) {
  if (!await showConfirm('确定删除该表？')) return
  try {
    await deleteTable(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      props.dataSourceId,
      tableId
    )
    await load()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '删除表失败'
  }
}

onMounted(load)
watch(() => [props.dataSourceId, store.tenantId, store.workspaceId], load)
</script>

<style scoped>
.schema-panel { display: flex; flex-direction: column; gap: 1rem; }
.panel-actions { display: flex; gap: 0.5rem; }
.error-text { color: var(--color-error, #ef4444); }
.schema-meta { display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.5rem; }
.tag { padding: 2px 8px; border-radius: 4px; background: var(--bg-hover); font-size: 0.75rem; }
.tag-info { background: rgba(58,123,213,0.12); color: var(--color-primary); }
.empty-hint { color: var(--color-text-light); padding: 1rem; }
.muted { color: var(--color-text-light); }
.field-grid { display: flex; flex-direction: column; gap: 0.75rem; }
.field { display: flex; flex-direction: column; gap: 0.25rem; }
.field input, .field textarea { padding: 6px 10px; border: 1px solid var(--color-border); border-radius: 6px; }
.columns-editor { display: flex; flex-direction: column; gap: 0.5rem; }
.column-row { display: grid; grid-template-columns: 1fr 1fr 1fr 1fr 1.5fr 32px; gap: 4px; }
.column-row input, .column-row select { padding: 4px 6px; border: 1px solid var(--color-border); border-radius: 4px; }
.btn-remove { background: var(--color-error); color: white; border: none; border-radius: 4px; cursor: pointer; }
.btn-add { background: var(--bg-hover); border: 1px dashed var(--color-border); padding: 6px; border-radius: 4px; cursor: pointer; }
</style>
