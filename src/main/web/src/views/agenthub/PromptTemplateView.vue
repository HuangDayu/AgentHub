<template>
  <section class="grid glass-float">
    <div class="page-header">
      <div>
        <h2>提示词</h2>
        <p class="muted">管理提示词模板，支持变量替换</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <ModalDialog
      v-model:visible="showCreateForm" :title="editingId ? '编辑提示词' : '创建提示词'" @close="cancelForm"
      @confirm="submitConfig"
      :confirm-text="editingId ? '更新' : '创建'"
    >
        <form class="field-grid">
          <label class="field">
            <span>名称 *</span>
            <input v-model="form.name" required placeholder="system-prompt" />
          </label>
          <label class="field">
            <span>分类</span>
            <select v-model="form.category">
              <option value="SYSTEM">SYSTEM</option>
              <option value="USER">USER</option>
              <option value="ASSISTANT">ASSISTANT</option>
              <option value="GENERAL">GENERAL</option>
            </select>
          </label>
          <label class="field">
            <span>描述</span>
            <input v-model="form.description" placeholder="提示词描述" />
          </label>
          <div class="field full-width">
            <span>内容 *（支持Markdown）</span>
            <MarkdownEditor v-model="form.content" placeholder="You are a helpful assistant." />
          </div>
          <label class="field">
            <span>启用状态</span>
            <select v-model="form.active">
              <option :value="true">启用</option>
              <option :value="false">禁用</option>
            </select>
          </label>
        </form>
      </ModalDialog>

      <article class="table-card float-effect">
        <table>
          <thead>
            <tr>
              <th>名称</th>
              <th>分类</th>
              <th>内容预览</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tpl in templates" :key="tpl.id">
              <td>
                <strong>{{ tpl.name }}</strong>
                <div class="muted">{{ tpl.id }}</div>
              </td>
              <td><span class="tag">{{ tpl.category }}</span></td>
              <td class="content-preview">{{ truncate(tpl.content, 50) }}</td>
              <td>
                <span :class="['tag', tpl.active ? 'tag-success' : 'tag-error']">
                  {{ tpl.active ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDateTime(tpl.createdAt) }}</td>
              <td>
                <div class="chip-row">
                  <CustomButton type="ghost" @click="startEdit(tpl)">编辑</CustomButton>
                  <CustomButton type="ghost" @click="handleDelete(tpl.id)">删除</CustomButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { showConfirm } from '@/utils/confirm'
import { formatDateTime } from '@/common/format'
import { listPromptTemplates, createPromptTemplate, updatePromptTemplate, deletePromptTemplate, type PromptTemplate } from '@/api/prompt-api'
import { useWorkspaceStore } from '@/store/workspace-store'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const error = ref('')
const templates = ref<PromptTemplate[]>([])
const editingId = ref<string | null>(null)
const showCreateForm = ref(false)
const form = reactive({
  name: '',
  description: '',
  category: 'GENERAL',
  content: '',
  active: true,
})

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

onMounted(loadTemplates)

  // 监听全局新增事件
  window.addEventListener('global-add', () => {
    editingId.value = null
    showCreateForm.value = true
  })
watch(() => [store.tenantId, store.workspaceId], loadTemplates)

async function loadTemplates() {
  if (!selectionReady.value) { templates.value = []; return }
  await execute(async () => { templates.value = await listPromptTemplates({ tenantId: store.tenantId, workspaceId: store.workspaceId }) })
}

function startEdit(tpl: PromptTemplate) {
  editingId.value = tpl.id
  form.name = tpl.name
  form.description = tpl.description || ''
  form.category = tpl.category
  form.content = tpl.content
  form.active = tpl.active
  showCreateForm.value = true
}

function cancelForm() {
  editingId.value = null
  showCreateForm.value = false
  form.name = ''
  form.description = ''
  form.category = 'GENERAL'
  form.content = ''
  form.active = true
}

async function submitConfig() {
  if (!selectionReady.value) return
  await execute(async () => {
    if (editingId.value) {
      await updatePromptTemplate({ tenantId: store.tenantId, workspaceId: store.workspaceId }, editingId.value, form)
    } else {
      await createPromptTemplate({ tenantId: store.tenantId, workspaceId: store.workspaceId }, form)
    }
    cancelForm()
    await loadTemplates()
  })
}

async function handleDelete(id: string) {
  if (!selectionReady.value) return
  if (!await showConfirm('确定要删除这个提示词模板吗？')) return
  await execute(async () => {
    await deletePromptTemplate({ tenantId: store.tenantId, workspaceId: store.workspaceId }, id)
    await loadTemplates()
  })
}

function truncate(text: string, max: number) {
  return text && text.length > max ? text.slice(0, max) + '...' : text
}

async function execute(action: () => Promise<void>) {
  error.value = ''
  try { await action() } catch (reason) { error.value = reason instanceof Error ? reason.message : '请求失败' }
}
</script>

<style scoped>
.tag-success { background: rgba(34, 197, 94, 0.14); color: var(--color-success); }
.tag-error { background: rgba(239, 68, 68, 0.14); color: var(--color-error); }
.content-preview { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.full-width { grid-column: 1 / -1; }
</style>
