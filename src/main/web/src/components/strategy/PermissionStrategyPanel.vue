<template>
  <div class="permission-panel">
    <div class="panel-header">
      <h3>权限策略</h3>
      <CustomButton type="primary" @click="openCreate" :disabled="loading">创建策略</CustomButton>
    </div>

    <p v-if="error" class="error-text">{{ error }}</p>

    <article v-if="strategies.length > 0" class="table-card">
      <table>
        <thead>
          <tr>
            <th>名称</th>
            <th>允许角色</th>
            <th>允许操作</th>
            <th>限流 / 分</th>
            <th>审计</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in strategies" :key="p.id">
            <td><strong>{{ p.name }}</strong><div class="muted">{{ p.id }}</div></td>
            <td>
              <span v-for="r in p.allowedRoles" :key="r" class="tag tag-info">{{ r }}</span>
            </td>
            <td>
              <span v-for="o in p.allowedOperations" :key="o" class="tag">{{ o }}</span>
            </td>
            <td>{{ p.rateLimitPerMinute }}</td>
            <td>
              <span :class="['tag', p.auditLogEnabled ? 'tag-success' : 'tag-error']">
                {{ p.auditLogEnabled ? '开启' : '关闭' }}
              </span>
            </td>
            <td>
              <div class="chip-row">
                <CustomButton type="ghost" @click="openEdit(p)">编辑</CustomButton>
                <CustomButton type="ghost" @click="remove(p.id)">删除</CustomButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </article>
    <p v-else class="empty-hint">暂无策略。点击"创建策略"开始。</p>

    <ModalDialog v-model:visible="showForm" :title="editingId ? '编辑策略' : '创建策略'" size="medium"
                 @confirm="submit" @close="cancelForm" :confirm-text="editingId ? '更新' : '创建'">
      <form class="field-grid">
        <label class="field">
          <span>名称 *</span>
          <input v-model="form.name" required placeholder="策略名称" />
        </label>
        <label class="field">
          <span>描述</span>
          <textarea v-model="form.description" placeholder="策略描述"></textarea>
        </label>
        <label class="field">
          <span>允许角色 (逗号分隔)</span>
          <input v-model="form.allowedRolesText" placeholder="admin, user" />
        </label>
        <label class="field">
          <span>允许操作 (逗号分隔)</span>
          <input v-model="form.allowedOperationsText" placeholder="CREATE, READ, UPDATE, DELETE" />
        </label>
        <div class="grid-2">
          <label class="field">
            <span>限流 / 分</span>
            <input type="number" v-model.number="form.rateLimitPerMinute" />
          </label>
          <label class="field">
            <span>限流 / 时</span>
            <input type="number" v-model.number="form.rateLimitPerHour" />
          </label>
        </div>
        <label class="field">
          <span>危险 SQL 拦截</span>
          <CustomSelect v-model="form.dangerousSqlBlock" :options="booleanOptions" />
        </label>
        <label class="field">
          <span>审计日志</span>
          <CustomSelect v-model="form.auditLogEnabled" :options="booleanOptions" />
        </label>
      </form>
    </ModalDialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { showConfirm } from '@/utils/confirm'
import {
  deletePermissionStrategy,
  listPermissionStrategies,
  upsertPermissionStrategy,
} from '@/api/permission-strategy-api'
import { useWorkspaceStore } from '@/store/workspace-store'
import type { PermissionStrategy } from '@/types/permission-strategy'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'
import CustomSelect from '@/components/CustomSelect.vue'

const booleanOptions = [
  { value: true, label: '是' },
  { value: false, label: '否' },
]

const store = useWorkspaceStore()
const strategies = ref<PermissionStrategy[]>([])
const error = ref('')
const loading = ref(false)
const showForm = ref(false)
const editingId = ref<string | null>(null)
const form = reactive({
  name: '',
  description: '',
  allowedRolesText: '',
  allowedOperationsText: '',
  rateLimitPerMinute: 60,
  rateLimitPerHour: 1000,
  dangerousSqlBlock: true,
  auditLogEnabled: true,
})

async function load() {
  if (!store.tenantId || !store.workspaceId) return
  error.value = ''
  try {
    strategies.value = await listPermissionStrategies({
      tenantId: store.tenantId,
      workspaceId: store.workspaceId,
    })
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载失败'
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  showForm.value = true
}

function openEdit(p: PermissionStrategy) {
  editingId.value = p.id
  form.name = p.name
  form.description = p.description || ''
  form.allowedRolesText = p.allowedRoles.join(', ')
  form.allowedOperationsText = p.allowedOperations.join(', ')
  form.rateLimitPerMinute = p.rateLimitPerMinute
  form.rateLimitPerHour = p.rateLimitPerHour
  form.dangerousSqlBlock = p.dangerousSqlBlock
  form.auditLogEnabled = p.auditLogEnabled
  showForm.value = true
}

function resetForm() {
  form.name = ''
  form.description = ''
  form.allowedRolesText = ''
  form.allowedOperationsText = ''
  form.rateLimitPerMinute = 60
  form.rateLimitPerHour = 1000
  form.dangerousSqlBlock = true
  form.auditLogEnabled = true
}

function cancelForm() {
  editingId.value = null
  showForm.value = false
}

function splitCsv(value: string): string[] {
  return value.split(',').map((s) => s.trim()).filter((s) => s.length > 0)
}

async function submit() {
  if (!form.name.trim()) return
  loading.value = true
  try {
    const data: Partial<PermissionStrategy> = {
      name: form.name,
      description: form.description,
      allowedRoles: splitCsv(form.allowedRolesText),
      allowedOperations: splitCsv(form.allowedOperationsText),
      rateLimitPerMinute: form.rateLimitPerMinute,
      rateLimitPerHour: form.rateLimitPerHour,
      dangerousSqlBlock: form.dangerousSqlBlock,
      auditLogEnabled: form.auditLogEnabled,
    }
    if (editingId.value) {
      data.id = editingId.value
    }
    await upsertPermissionStrategy(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      data
    )
    cancelForm()
    await load()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '保存失败'
  } finally {
    loading.value = false
  }
}

async function remove(id: string) {
  if (!await showConfirm('确定删除该策略？')) return
  try {
    await deletePermissionStrategy(
      { tenantId: store.tenantId, workspaceId: store.workspaceId },
      id
    )
    await load()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '删除失败'
  }
}

onMounted(load)
watch(() => [store.tenantId, store.workspaceId], load)
</script>

<style scoped>
.permission-panel { display: flex; flex-direction: column; gap: 1rem; }
.panel-header { display: flex; justify-content: space-between; align-items: center; }
.panel-header h3 { margin: 0; }
.error-text { color: var(--color-error, #ef4444); }
.empty-hint { color: var(--color-text-light); padding: 1rem; }
.muted { color: var(--color-text-light); font-size: 0.75rem; }
.tag { padding: 2px 8px; border-radius: 4px; background: var(--bg-hover); font-size: 0.75rem; margin-right: 4px; }
.tag-info { background: rgba(58,123,213,0.12); color: var(--color-primary); }
.tag-success { background: rgba(34,197,94,0.14); color: var(--color-success); }
.tag-error { background: rgba(239,68,68,0.14); color: var(--color-error); }
.chip-row { display: flex; gap: 4px; }
.field-grid { display: flex; flex-direction: column; gap: 0.75rem; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
.field { display: flex; flex-direction: column; gap: 0.25rem; }
.field input, .field textarea {
  padding: 6px 10px; border: 1px solid var(--color-border); border-radius: 6px; background: var(--bg-card-solid);
}
</style>
