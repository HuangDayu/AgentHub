<template>
  <section class="security-policy-management">
    <div class="page-header">
      <h2>安全策略管理</h2>
      <p class="muted">管理Agent的安全控制策略</p>
    </div>

    <div class="toolbar">
      <button @click="showCreateDialog = true" class="btn-primary">新建安全策略</button>
    </div>

    <div class="policy-list">
      <table v-if="policies.length > 0">
        <thead>
          <tr>
            <th>名称</th>
            <th>输入验证</th>
            <th>输出过滤</th>
            <th>限流</th>
            <th>内容审核</th>
            <th>PII检测</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="policy in policies" :key="policy.id">
            <td>{{ policy.name }}</td>
            <td>{{ policy.inputValidation ? '✓' : '✗' }}</td>
            <td>{{ policy.outputFiltering ? '✓' : '✗' }}</td>
            <td>{{ policy.rateLimitEnabled ? `${policy.rateLimitPerMinute}/min` : '关闭' }}</td>
            <td>{{ policy.contentModeration ? '✓' : '✗' }}</td>
            <td>{{ policy.piiDetection ? '✓' : '✗' }}</td>
            <td>{{ formatDate(policy.createdAt) }}</td>
            <td>
              <button @click="editPolicy(policy)" class="btn-small">编辑</button>
              <button @click="deletePolicyHandler(policy.id)" class="btn-small btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">
        <p>暂无安全策略数据</p>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <div v-if="showCreateDialog || showEditDialog" class="dialog-overlay">
      <div class="dialog">
        <h3>{{ showEditDialog ? '编辑安全策略' : '新建安全策略' }}</h3>
        <form @submit.prevent="showEditDialog ? updatePolicyHandler() : createPolicyHandler()">
          <div class="form-group">
            <label>名称</label>
            <input v-model="form.name" required />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="form.description" rows="3"></textarea>
          </div>
          <div class="form-group checkbox-group">
            <label>
              <input type="checkbox" v-model="form.inputValidation" />
              输入验证
            </label>
            <label>
              <input type="checkbox" v-model="form.outputFiltering" />
              输出过滤
            </label>
            <label>
              <input type="checkbox" v-model="form.contentModeration" />
              内容审核
            </label>
            <label>
              <input type="checkbox" v-model="form.piiDetection" />
              PII检测
            </label>
          </div>
          <div class="form-group">
            <label>
              <input type="checkbox" v-model="form.rateLimitEnabled" />
              启用限流
            </label>
            <input v-if="form.rateLimitEnabled" type="number" v-model.number="form.rateLimitPerMinute" min="1" placeholder="每分钟请求数" />
          </div>
          <div class="form-actions">
            <button type="button" @click="closeDialog" class="btn-secondary">取消</button>
            <button type="submit" class="btn-primary">{{ showEditDialog ? '更新' : '创建' }}</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useWorkspaceStore } from '@/store/workspace-store'
import { listSecurityPolicies, createSecurityPolicy, updateSecurityPolicy, deleteSecurityPolicy } from '@/api/security-policy-api'
import type { SecurityPolicy } from '@/types/memory'

const store = useWorkspaceStore()
const policies = ref<SecurityPolicy[]>([])
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingPolicyId = ref('')

const form = ref({
  name: '',
  description: '',
  inputValidation: true,
  outputFiltering: true,
  rateLimitEnabled: false,
  rateLimitPerMinute: 60,
  contentModeration: false,
  piiDetection: false
})

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

onMounted(async () => {
  await loadPolicies()
})

async function loadPolicies() {
  try {
    policies.value = await listSecurityPolicies(selection())
  } catch (e) {
    console.error('Failed to load policies', e)
  }
}

async function createPolicyHandler() {
  try {
    await createSecurityPolicy(selection(), form.value.name, form.value.description)
    await loadPolicies()
    closeDialog()
  } catch (e) {
    console.error('Failed to create policy', e)
  }
}

async function updatePolicyHandler() {
  try {
    await updateSecurityPolicy(selection(), editingPolicyId.value, form.value.name, form.value.description)
    await loadPolicies()
    closeDialog()
  } catch (e) {
    console.error('Failed to update policy', e)
  }
}

async function deletePolicyHandler(id: string) {
  if (confirm('确定删除此安全策略？')) {
    try {
      await deleteSecurityPolicy(selection(), id)
      await loadPolicies()
    } catch (e) {
      console.error('Failed to delete policy', e)
    }
  }
}

function editPolicy(policy: SecurityPolicy) {
  editingPolicyId.value = policy.id
  form.value = {
    name: policy.name,
    description: policy.description,
    inputValidation: policy.inputValidation,
    outputFiltering: policy.outputFiltering,
    rateLimitEnabled: policy.rateLimitEnabled,
    rateLimitPerMinute: policy.rateLimitPerMinute,
    contentModeration: policy.contentModeration,
    piiDetection: policy.piiDetection
  }
  showEditDialog.value = true
}

function closeDialog() {
  showCreateDialog.value = false
  showEditDialog.value = false
  form.value = {
    name: '',
    description: '',
    inputValidation: true,
    outputFiltering: true,
    rateLimitEnabled: false,
    rateLimitPerMinute: 60,
    contentModeration: false,
    piiDetection: false
  }
}

function formatDate(date: string): string {
  return new Date(date).toLocaleString()
}
</script>

<style scoped>
.security-policy-management {
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.toolbar {
  margin-bottom: 1.5rem;
}

.btn-primary {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: white;
  border: none;
  cursor: pointer;
}

.btn-secondary {
  padding: 0.5rem 1rem;
  background: #6c757d;
  color: white;
  border: none;
  cursor: pointer;
}

.btn-small {
  padding: 0.25rem 0.5rem;
  margin-right: 0.5rem;
  cursor: pointer;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  min-width: 400px;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
}

.form-group input[type="text"],
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
}
</style>
