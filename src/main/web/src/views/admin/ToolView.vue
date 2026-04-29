<template>
  <section class="grid">
    <!-- 创建/编辑工具表单 -->
    <article class="panel">
      <h2>{{ editingId ? '编辑工具' : '创建工具' }}</h2>
      <form class="form-grid" @submit.prevent="submitForm">
        <label class="field">
          <span>工具名称</span>
          <input v-model="form.name" placeholder="搜索引擎" />
        </label>
        <label class="field">
          <span>工具描述</span>
          <input v-model="form.description" placeholder="调用搜索引擎获取信息" />
        </label>
        <label class="field" style="grid-column: 1 / -1">
          <span>参数定义 (JSON Schema)</span>
          <textarea v-model="form.parametersJson" rows="4" placeholder='{"type":"object","properties":{"query":{"type":"string"}}}'></textarea>
        </label>
        <div class="toolbar">
          <button class="primary" type="submit">{{ editingId ? '保存修改' : '创建工具' }}</button>
          <button v-if="editingId" class="ghost" type="button" @click="resetForm">取消编辑</button>
          <button class="secondary" type="button" @click="loadTools">刷新列表</button>
        </div>
      </form>
      <p class="status" v-if="error">{{ error }}</p>
    </article>

    <!-- 调用工具面板 -->
    <article class="panel" v-if="invokingTool">
      <h2>调用工具 — {{ invokingTool.name }}</h2>
      <form class="form-grid" @submit.prevent="runInvoke">
        <label class="field" style="grid-column: 1 / -1">
          <span>输入参数 (JSON)</span>
          <textarea v-model="invokeInput" rows="3" placeholder='{"query":"things knowledge"}'></textarea>
        </label>
        <div class="toolbar">
          <button class="primary" type="submit">执行调用</button>
          <button class="ghost" type="button" @click="invokingTool = null; invokeResult = null">关闭</button>
        </div>
      </form>
      <div v-if="invokeResult" class="invoke-chunkResult">
        <h3>调用结果</h3>
        <pre>{{ JSON.stringify(invokeResult.output, null, 2) }}</pre>
        <p class="muted">调用时间：{{ formatDate(invokeResult.invokedAt) }}</p>
      </div>
    </article>

    <!-- 工具列表 -->
    <article class="panel">
      <h2>工具列表</h2>
      <table class="table">
        <thead>
          <tr>
            <th>名称</th>
            <th>描述</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tool in tools" :key="tool.id">
            <td>{{ tool.name }}</td>
            <td>{{ tool.description }}</td>
            <td><span class="tag" :class="tool.enabled ? 'tag-on' : 'tag-off'">{{ tool.enabled ? '启用' : '停用' }}</span></td>
            <td>{{ formatDate(tool.createdAt) }}</td>
            <td>
              <div class="chip-row">
                <button class="ghost" @click="startEdit(tool)">编辑</button>
                <button class="ghost" @click="startInvoke(tool)">调用</button>
                <button class="ghost danger" @click="removeTool(tool.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <p class="muted" v-if="!tools.length">暂无工具</p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { createTool, deleteTool, invokeTool, listTools, updateTool } from '@/api/admin-api'
import { formatDate } from '@/common/format'
import type { Tool, ToolInvocationResult } from '@/domain/types'

interface ToolForm {
  name: string
  description: string
  parametersJson: string
}

const tools = ref<Tool[]>([])
const editingId = ref<string | null>(null)
const error = ref('')
const form = ref<ToolForm>({ name: '', description: '', parametersJson: '{}' })

const invokingTool = ref<Tool | null>(null)
const invokeInput = ref('{}')
const invokeResult = ref<ToolInvocationResult | null>(null)

onMounted(loadTools)

async function loadTools() {
  await run(async () => {
    tools.value = await listTools()
  })
}

async function submitForm() {
  let parameters: Record<string, unknown>
  try {
    parameters = JSON.parse(form.value.parametersJson || '{}')
  } catch {
    error.value = '参数定义 JSON 格式不正确'
    return
  }
  await run(async () => {
    if (editingId.value) {
      const existing = tools.value.find((t) => t.id === editingId.value)
      await updateTool(
        editingId.value,
        form.value.name.trim(),
        form.value.description.trim(),
        parameters,
        existing?.enabled ?? true,
      )
    } else {
      await createTool(form.value.name.trim(), form.value.description.trim(), parameters)
    }
    resetForm()
    await loadTools()
  })
}

function startEdit(tool: Tool) {
  editingId.value = tool.id
  form.value = {
    name: tool.name,
    description: tool.description,
    parametersJson: JSON.stringify(tool.parameters ?? {}, null, 2),
  }
}

function resetForm() {
  editingId.value = null
  form.value = { name: '', description: '', parametersJson: '{}' }
}

function startInvoke(tool: Tool) {
  invokingTool.value = tool
  invokeInput.value = '{}'
  invokeResult.value = null
}

async function runInvoke() {
  if (!invokingTool.value) return
  let params: Record<string, unknown>
  try {
    params = JSON.parse(invokeInput.value || '{}')
  } catch {
    error.value = '输入参数 JSON 格式不正确'
    return
  }
  await run(async () => {
    if (!invokingTool.value) return
    invokeResult.value = await invokeTool(invokingTool.value.id, params)
  })
}

async function removeTool(id: string) {
  if (!confirm('确定删除此工具？')) return
  await run(async () => {
    await deleteTool(id)
    await loadTools()
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
.invoke-chunkResult {
  margin-top: 16px;
  padding: 16px;
  border-radius: 14px;
  background: rgba(39, 65, 93, 0.04);
}
.invoke-chunkResult pre {
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Cascadia Code', 'Consolas', monospace;
  font-size: 13px;
}
</style>

