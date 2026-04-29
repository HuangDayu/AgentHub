<template>
  <section class="grid">
    <div class="page-header">
      <div>
        <h2>租户与工作区管理</h2>
        <p class="muted">平台管理员管理所有租户、工作区和成员。</p>
      </div>
      <p class="status">{{ error }}</p>
    </div>
    <div class="grid two-up">
      <!-- 租户面板 -->
      <article class="panel stack">
        <div class="toolbar">
          <select v-model="selectedTenantId" @change="onTenantChange">
            <option disabled value="">选择租户</option>
            <option v-for="tenant in tenants" :key="tenant.id" :value="tenant.id">
              {{ tenant.name }}（{{ tenant.tenantCode }}）
            </option>
          </select>
          <button class="secondary" @click="loadTenants">刷新租户</button>
        </div>
        <form class="field-grid" @submit.prevent="submitTenant">
          <label class="field">
            <span>租户编码</span>
            <input v-model="tenantForm.tenantCode" placeholder="acme" />
          </label>
          <label class="field">
            <span>租户名称</span>
            <input v-model="tenantForm.name" placeholder="Acme Enterprise" />
          </label>
          <label class="field">
            <span>套餐</span>
            <input v-model="tenantForm.planCode" placeholder="free / pro / enterprise" />
          </label>
          <label class="field">
            <span>区域</span>
            <input v-model="tenantForm.region" placeholder="cn-east / us-west" />
          </label>
          <button class="primary" type="submit">创建租户</button>
        </form>
        <div class="table-card">
          <table>
            <thead>
              <tr>
                <th>编码</th>
                <th>名称</th>
                <th>套餐</th>
                <th>区域</th>
                <th>创建时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tenant in tenants" :key="tenant.id">
                <td>{{ tenant.tenantCode }}</td>
                <td>{{ tenant.name }}</td>
                <td><span class="tag">{{ tenant.planCode }}</span></td>
                <td>{{ tenant.region }}</td>
                <td>{{ formatDateTime(tenant.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
      <!-- 工作区面板 -->
      <article class="panel stack">
        <div class="toolbar">
          <select v-model="selectedWorkspaceId" :disabled="!selectedTenantId" @change="onWorkspaceChange">
            <option disabled value="">选择工作区</option>
            <option v-for="workspace in workspaces" :key="workspace.id" :value="workspace.id">
              {{ workspace.name }}
            </option>
          </select>
          <button class="secondary" :disabled="!selectedTenantId" @click="loadWorkspaces">刷新工作区</button>
        </div>
        <form class="field-grid" @submit.prevent="submitWorkspace">
          <label class="field">
            <span>新增工作区</span>
            <input v-model="workspaceName" placeholder="例如：运营主空间" />
          </label>
          <button class="primary" type="submit" :disabled="!selectedTenantId">创建工作区</button>
        </form>
        <div class="table-card">
          <table>
            <thead>
              <tr>
                <th>名称</th>
                <th>创建时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="workspace in workspaces" :key="workspace.id">
                <td>{{ workspace.name }}</td>
                <td>{{ formatDateTime(workspace.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </div>
    <!-- 成员面板 -->
    <article class="panel stack">
      <div class="page-header">
        <div>
          <h2>成员管理</h2>
          <p class="muted">当前工作区成员列表。邀请成员需要填写用户 ID、角色和范围。</p>
        </div>
      </div>
      <form class="field-grid" @submit.prevent="submitMember">
        <label class="field">
          <span>用户 ID</span>
          <input v-model="memberForm.userId" placeholder="usr-001" />
        </label>
        <label class="field">
          <span>角色</span>
          <select v-model="memberForm.roleCode">
            <option value="admin">管理员</option>
            <option value="editor">编辑者</option>
            <option value="viewer">浏览者</option>
          </select>
        </label>
        <label class="field">
          <span>范围</span>
          <select v-model="memberForm.scopeType">
            <option value="workspace">工作区级</option>
            <option value="tenant">租户级</option>
          </select>
        </label>
        <button class="primary" type="submit" :disabled="!selectedWorkspaceId">邀请成员</button>
      </form>
      <div v-if="members.length" class="table-card">
        <table>
          <thead>
            <tr>
              <th>用户 ID</th>
              <th>角色</th>
              <th>范围</th>
              <th>加入时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="member in members" :key="member.id">
              <td>{{ member.userId }}</td>
              <td><span class="tag">{{ member.roleCode }}</span></td>
              <td>{{ member.scopeType }}</td>
              <td>{{ formatDateTime(member.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="empty-state">选定工作区后即可开始绑定成员。</div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { createMember, createTenant, createWorkspace, listMembers, listTenants, listWorkspaces } from '@/api/tenant-api'
import { formatDateTime } from '@/common/format'
import type { Member, Tenant, Workspace } from '@/domain/types'
import { useWorkspaceStore } from '@/store/workspace-store'

const store = useWorkspaceStore()
const tenants = ref<Tenant[]>([])
const workspaces = ref<Workspace[]>([])
const members = ref<Member[]>([])
const selectedTenantId = ref('')
const selectedWorkspaceId = ref('')

const tenantForm = reactive({
  tenantCode: '',
  name: '',
  planCode: 'free',
  region: 'cn-east',
})
const workspaceName = ref('')
const memberForm = reactive({
  userId: '',
  roleCode: 'editor',
  scopeType: 'workspace',
})
const error = ref('')

onMounted(loadTenants)
watch(() => selectedWorkspaceId.value, loadMembers)
watch(() => selectedTenantId.value, (id) => store.selectTenant(id))
watch(() => selectedWorkspaceId.value, (id) => store.selectWorkspace(id))

function onTenantChange() {
  loadWorkspaces()
}

function onWorkspaceChange() {
  loadMembers()
}

async function loadTenants() {
  await execute(async () => {
    tenants.value = await listTenants()
    if (tenants.value.length && !tenants.value.some(t => t.id === selectedTenantId.value)) {
      selectedTenantId.value = tenants.value[0].id
    }
    await loadWorkspaces()
  })
}

async function loadWorkspaces() {
  if (!selectedTenantId.value) {
    workspaces.value = []
    selectedWorkspaceId.value = ''
    members.value = []
    return
  }
  await execute(async () => {
    workspaces.value = await listWorkspaces(selectedTenantId.value)
    if (workspaces.value.length && !workspaces.value.some(w => w.id === selectedWorkspaceId.value)) {
      selectedWorkspaceId.value = workspaces.value[0].id
    }
    await loadMembers()
  })
}

async function loadMembers() {
  if (!selectedWorkspaceId.value) {
    members.value = []
    return
  }
  await execute(async () => {
    members.value = await listMembers(selectedWorkspaceId.value)
  })
}

async function submitTenant() {
  if (!tenantForm.tenantCode.trim() || !tenantForm.name.trim()) {
    return
  }
  await execute(async () => {
    await createTenant({
      tenantCode: tenantForm.tenantCode.trim(),
      name: tenantForm.name.trim(),
      planCode: tenantForm.planCode.trim(),
      region: tenantForm.region.trim(),
    })
    tenantForm.tenantCode = ''
    tenantForm.name = ''
    tenantForm.planCode = 'free'
    tenantForm.region = 'cn-east'
    await loadTenants()
  })
}

async function submitWorkspace() {
  if (!selectedTenantId.value || !workspaceName.value.trim()) {
    return
  }
  await execute(async () => {
    await createWorkspace(selectedTenantId.value, workspaceName.value.trim())
    workspaceName.value = ''
    await loadWorkspaces()
  })
}

async function submitMember() {
  if (!selectedWorkspaceId.value || !memberForm.userId.trim()) {
    return
  }
  await execute(async () => {
    await createMember(selectedWorkspaceId.value, {
      userId: memberForm.userId.trim(),
      roleCode: memberForm.roleCode,
      scopeType: memberForm.scopeType,
    })
    memberForm.userId = ''
    await loadMembers()
  })
}

async function execute(action: () => Promise<void>) {
  error.value = ''
  try {
    await action()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '请求失败'
  }
}
</script>

