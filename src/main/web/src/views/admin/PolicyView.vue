<template>
  <section class="grid">
    <!-- 创建/编辑策略表单 -->
    <article class="panel">
      <h2>{{ editingId ? '编辑策略' : '创建策略' }}</h2>
      <form class="form-grid" @submit.prevent="submitForm">
        <label class="field">
          <span>策略名称</span>
          <input v-model="form.name" placeholder="数据访问策略" />
        </label>
        <label class="field">
          <span>策略描述</span>
          <input v-model="form.description" placeholder="控制租户的数据访问权限" />
        </label>
        <label class="field">
          <span>规则 (JSON)</span>
          <textarea v-model="form.rulesJson" rows="4" placeholder='{"effect":"allow","actions":["read"],"resources":["*"]}'></textarea>
        </label>
        <div class="toolbar">
          <button class="primary" type="submit">{{ editingId ? '保存修改' : '创建策略' }}</button>
          <button v-if="editingId" class="ghost" type="button" @click="resetForm">取消编辑</button>
          <button class="secondary" type="button" @click="loadPolicies">刷新列表</button>
        </div>
      </form>
      <p class="status" v-if="error">{{ error }}</p>
    </article>

    <!-- 策略列表 -->
    <article class="panel">
      <h2>策略列表</h2>
      <table class="table">
        <thead>
          <tr>
            <th>名称</th>
            <th>描述</th>
            <th>创建时间</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="policy in policies" :key="policy.id">
            <td>{{ policy.name }}</td>
            <td>{{ policy.description }}</td>
            <td>{{ formatDate(policy.createdAt) }}</td>
            <td>{{ formatDate(policy.updatedAt) }}</td>
            <td>
              <div class="chip-row">
                <button class="ghost" @click="startEdit(policy)">编辑</button>
                <button class="ghost danger" @click="removePolicy(policy.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <p class="muted" v-if="!policies.length">暂无策略</p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { createPolicy, deletePolicy, listPolicies, updatePolicy } from '@/api/admin-api'
import { formatDate } from '@/common/format'
import type { Policy } from '@/domain/types'

interface PolicyForm {
  name: string
  description: string
  rulesJson: string
}

const policies = ref<Policy[]>([])
const editingId = ref<string | null>(null)
const error = ref('')
const form = ref<PolicyForm>({ name: '', description: '', rulesJson: '{}' })

onMounted(loadPolicies)

async function loadPolicies() {
  await run(async () => {
    policies.value = await listPolicies()
  })
}

async function submitForm() {
  let rules: Record<string, unknown>
  try {
    rules = JSON.parse(form.value.rulesJson || '{}')
  } catch {
    error.value = '规则 JSON 格式不正确'
    return
  }
  await run(async () => {
    if (editingId.value) {
      await updatePolicy(editingId.value, form.value.name.trim(), form.value.description.trim(), rules)
    } else {
      await createPolicy(form.value.name.trim(), form.value.description.trim(), rules)
    }
    resetForm()
    await loadPolicies()
  })
}

function startEdit(policy: Policy) {
  editingId.value = policy.id
  form.value = {
    name: policy.name,
    description: policy.description,
    rulesJson: JSON.stringify(policy.rules ?? {}, null, 2),
  }
}

function resetForm() {
  editingId.value = null
  form.value = { name: '', description: '', rulesJson: '{}' }
}

async function removePolicy(id: string) {
  if (!confirm('确定删除此策略？')) return
  await run(async () => {
    await deletePolicy(id)
    await loadPolicies()
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

