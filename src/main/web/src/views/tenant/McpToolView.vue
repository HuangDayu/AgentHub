<template>
  <section class="grid">
    <div class="page-header">
      <div>
        <h2>MCP工具管理</h2>
        <p class="muted">管理Model Context Protocol工具配置</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    <article v-if="!selectionReady" class="empty-state">请先在"租户空间"页选择租户与工作区。</article>
    <template v-else>
      <article v-show="showCreateForm || editingId" class="panel stack">
        <div class="page-header">
          <h3 style="margin: 0">{{ editingId ? '编辑MCP工具' : '创建MCP工具' }}</h3>
          <button class="ghost" type="button" @click="cancelForm">取消</button>
        </div>
        <form class="field-grid" @submit.prevent="submitConfig">
          <label class="field">
            <span>名称 *</span>
            <input v-model="form.name" required placeholder="filesystem-mcp" />
          </label>
          <label class="field">
            <span>服务器类型</span>
            <select v-model="form.serverType">
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
            <span>描述</span>
            <textarea v-model="form.description" placeholder="工具描述"></textarea>
          </label>
          <label class="field">
            <span>启用状态</span>
            <select v-model="form.enabled">
              <option :value="true">启用</option>
              <option :value="false">禁用</option>
            </select>
          </label>
          <button class="primary" type="submit">{{ editingId ? '更新' : '创建' }}</button>
        </form>
      </article>

      <article class="table-card">
        <div class="page-header">
          <h3 style="margin: 0">工具列表</h3>
          <button class="primary" type="button" @click="showCreateForm = true">新建工具</button>
        </div>
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
                  <button class="ghost" type="button" @click="startEdit(httpTool)">编辑</button>
                  <button class="ghost" type="button" @click="handleDelete(httpTool.id)">删除</button>
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
  enabled: true,
})

const selectionReady = computed(() => Boolean(store.tenantId && store.workspaceId))

onMounted(loadTools)
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
  form.enabled = httpTool.enabled
}

function cancelForm() {
  editingId.value = null
  showCreateForm.value = false
  form.name = ''
  form.description = ''
  form.serverUrl = ''
  form.serverType = 'STDIO'
  form.command = ''
  form.enabled = true
}

async function submitConfig() {
  if (!selectionReady.value) return
  await execute(async () => {
    if (editingId.value) {
      await updateMcpTool({ tenantId: store.tenantId, workspaceId: store.workspaceId }, editingId.value, form)
    } else {
      await createMcpTool({ tenantId: store.tenantId, workspaceId: store.workspaceId }, form)
    }
    cancelForm()
    await loadTools()
  })
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
