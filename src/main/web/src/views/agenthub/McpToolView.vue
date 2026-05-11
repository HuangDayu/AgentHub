<template>
  <section class="grid glass-float">
    <div class="page-header">
      <div>
        <h2>MCP工具管理</h2>
        <p class="muted">管理Model Context Protocol工具配置</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <ModalDialog
      v-model:visible="showCreateForm" :title="editingId ? '编辑MCP工具' : '创建MCP工具'" @close="cancelForm"
      @confirm="submitConfig"
      :confirm-text="editingId ? '更新' : '创建'"
    >
        <form class="field-grid">
          <label class="field">
            <span>名称 *</span>
            <input v-model="form.name" required placeholder="工具名称" />
          </label>
          <label class="field">
            <span>服务器类型 *</span>
            <select v-model="form.serverType" required>
              <option value="STDIO">STDIO</option>
              <option value="HTTP">HTTP</option>
              <option value="SSE">SSE</option>
            </select>
          </label>
          <label class="field">
            <span>服务器URL *</span>
            <input v-model="form.serverUrl" required placeholder="/usr/local/bin/mcp-server" />
          </label>
          <label class="field">
            <span>启动命令</span>
            <input v-model="form.command" placeholder="node" />
          </label>
          <label class="field">
            <span>命令参数</span>
            <input v-model="form.argsInput" placeholder="参数用逗号分隔，如: --port,8080,--debug" />
          </label>
          <label class="field">
            <span>环境变量</span>
            <textarea v-model="form.envInput" placeholder="每行一个环境变量，格式: KEY=value"></textarea>
          </label>
          <label class="field">
            <span>描述</span>
            <textarea v-model="form.description" placeholder="工具描述"></textarea>
          </label>
          <label class="field">
            <span>异步模式</span>
            <select v-model="form.async">
              <option :value="true">异步</option>
              <option :value="false">同步</option>
            </select>
          </label>
          <label class="field">
            <span>启用状态</span>
            <select v-model="form.enabled">
              <option :value="true">启用</option>
              <option :value="false">禁用</option>
            </select>
          </label>
        </form>
      </ModalDialog>

      <article class="table-card">
        <table>
          <thead>
            <tr>
              <th>名称</th>
              <th>类型</th>
              <th>服务器URL</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="httpTool in tools" :key="httpTool.id">
              <td>
                <strong>{{ httpTool.name }}</strong>
                <div class="muted">{{ httpTool.id }}</div>
              </td>
              <td><span class="tag">{{ httpTool.serverType }}</span></td>
              <td>{{ httpTool.serverUrl }}</td>
              <td>
                <span :class="['tag', httpTool.enabled ? 'tag-success' : 'tag-error']">
                  {{ httpTool.enabled ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDateTime(httpTool.createdAt) }}</td>
              <td>
                <div class="chip-row">
                  <CustomButton type="ghost" @click="startEdit(httpTool)">编辑</CustomButton>
                  <CustomButton type="ghost" @click="handleDelete(httpTool.id)">删除</CustomButton>
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
import { formatDateTime } from '@/common/format'
import { listMcpTools, createMcpTool, updateMcpTool, deleteMcpTool, type McpTool } from '@/api/mcp-api'
import { useWorkspaceStore } from '@/store/workspace-store'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const error = ref('')
const tools = ref<McpTool[]>([])
const editingId = ref<string | null>(null)
const showCreateForm = ref(false)
const form = reactive({
  name: '',
  description: '',
  serverUrl: '',
  serverType: 'STDIO' as 'STDIO' | 'HTTP' | 'SSE',
  command: '',
  argsInput: '',
  envInput: '',
  async: false,
  enabled: true,
})

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

onMounted(loadTools)

  // 监听全局新增事件
  window.addEventListener('global-add', () => {
    editingId.value = null
    showCreateForm.value = true
  })
watch(() => [store.tenantId, store.workspaceId], loadTools)

async function loadTools() {
  if (!selectionReady.value) { tools.value = []; return }
  await execute(async () => { tools.value = await listMcpTools({ tenantId: store.tenantId, workspaceId: store.workspaceId }) })
}

function startEdit(httpTool: McpTool) {
  editingId.value = httpTool.id
  form.name = httpTool.name
  form.description = httpTool.description || ''
  form.serverUrl = httpTool.serverUrl
  form.serverType = httpTool.serverType
  form.command = httpTool.command || ''
  form.argsInput = httpTool.args?.join(',') || ''
  form.envInput = httpTool.env ? Object.entries(httpTool.env).map(([k, v]) => `${k}=${v}`).join('\n') : ''
  form.async = httpTool.async
  form.enabled = httpTool.enabled
  showCreateForm.value = true
}

function cancelForm() {
  editingId.value = null
  showCreateForm.value = false
  form.name = ''
  form.description = ''
  form.serverUrl = ''
  form.serverType = 'STDIO'
  form.command = ''
  form.argsInput = ''
  form.envInput = ''
  form.async = false
  form.enabled = true
}

async function submitConfig() {
  if (!selectionReady.value) return
  await execute(async () => {
    const data = {
      name: form.name,
      description: form.description,
      serverUrl: form.serverUrl,
      serverType: form.serverType,
      command: form.command,
      args: parseArgs(form.argsInput),
      env: parseEnv(form.envInput),
      async: form.async,
      enabled: form.enabled,
    }
    if (editingId.value) {
      await updateMcpTool({ tenantId: store.tenantId, workspaceId: store.workspaceId }, editingId.value, data)
    } else {
      await createMcpTool({ tenantId: store.tenantId, workspaceId: store.workspaceId }, data)
    }
    cancelForm()
    await loadTools()
  })
}

function parseArgs(input: string): string[] | undefined {
  if (!input.trim()) return undefined
  return input.split(',').map(s => s.trim()).filter(s => s)
}

function parseEnv(input: string): Record<string, string> | undefined {
  if (!input.trim()) return undefined
  const env: Record<string, string> = {}
  for (const line of input.split('\n')) {
    const trimmed = line.trim()
    if (!trimmed) continue
    const idx = trimmed.indexOf('=')
    if (idx > 0) {
      const key = trimmed.slice(0, idx).trim()
      const value = trimmed.slice(idx + 1).trim()
      if (key) env[key] = value
    }
  }
  return Object.keys(env).length > 0 ? env : undefined
}

async function handleDelete(toolId: string) {
  if (!selectionReady.value) return
  if (!confirm('确定要删除这个MCP工具吗？')) return
  await execute(async () => {
    await deleteMcpTool({ tenantId: store.tenantId, workspaceId: store.workspaceId }, toolId)
    await loadTools()
  })
}

async function execute(action: () => Promise<void>) {
  error.value = ''
  try { await action() } catch (reason) { error.value = reason instanceof Error ? reason.message : '请求失败' }
}
</script>

<style scoped>
.tag-success { background: rgba(34, 197, 94, 0.14); color: #16a34a; }
.tag-error { background: rgba(239, 68, 68, 0.14); color: #dc2626; }
</style>
