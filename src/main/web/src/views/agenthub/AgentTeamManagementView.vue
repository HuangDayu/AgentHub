<template>
  <section class="team-management glass-float">
    <div class="page-header">
      <h2>Agent团队管理</h2>
      <p class="muted">管理多Agent协作团队</p>
    </div>

    <div class="toolbar">
      <button @click="showCreateDialog = true" class="btn-primary">新建团队</button>
    </div>

    <div class="team-list">
      <table v-if="teams.length > 0">
        <thead>
          <tr>
            <th>编码</th>
            <th>名称</th>
            <th>协调模式</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="team in teams" :key="team.id">
            <td>{{ team.teamCode }}</td>
            <td>{{ team.name }}</td>
            <td>{{ team.coordinationMode }}</td>
            <td>
              <span :class="getStatusClass(team.status)">{{ team.status }}</span>
            </td>
            <td>{{ formatDate(team.createdAt) }}</td>
            <td>
              <button @click="editTeam(team)" class="btn-small">编辑</button>
              <button @click="toggleTeam(team)" class="btn-small">
                {{ team.status === 'ACTIVE' ? '停用' : '激活' }}
              </button>
              <button @click="deleteTeamHandler(team.id)" class="btn-small btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">
        <p>暂无团队数据</p>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <div v-if="showCreateDialog || showEditDialog" class="dialog-overlay">
      <div class="dialog">
        <h3>{{ showEditDialog ? '编辑团队' : '新建团队' }}</h3>
        <form @submit.prevent="showEditDialog ? updateTeamHandler() : createTeamHandler()">
          <div class="form-group">
            <label>团队编码</label>
            <input v-model="form.teamCode" :disabled="showEditDialog" required />
          </div>
          <div class="form-group">
            <label>名称</label>
            <input v-model="form.name" required />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="form.description" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>协调模式</label>
            <select v-model="form.coordinationMode" required>
              <option value="SEQUENTIAL">顺序执行</option>
              <option value="PARALLEL">并行执行</option>
              <option value="HIERARCHICAL">层级协调</option>
              <option value="CONSENSUS">共识决策</option>
            </select>
          </div>
          <div class="form-group">
            <label>成员配置 (JSON)</label>
            <textarea v-model="form.memberConfig" rows="5"></textarea>
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
import { listTeams, createTeam, updateTeam, activateTeam, deactivateTeam, deleteTeam } from '@/api/team-api'
import type { AgentTeam } from '@/types/memory'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const teams = ref<AgentTeam[]>([])
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingTeamId = ref('')

const form = ref({
  teamCode: '',
  name: '',
  description: '',
  coordinationMode: 'SEQUENTIAL',
  memberConfig: '{}'
})

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

onMounted(async () => {

// 监听全局新增事件
onMounted(() => {
  window.addEventListener('global-add', () => {
    showCreateForm.value = true
  })
})
  await loadTeams()
})

async function loadTeams() {
  try {
    teams.value = await listTeams(selection())
  } catch (e) {
    console.error('Failed to load teams', e)
  }
}

async function createTeamHandler() {
  try {
    await createTeam(selection(), form.value.teamCode, form.value.name, form.value.description, form.value.coordinationMode, form.value.memberConfig)
    await loadTeams()
    closeDialog()
  } catch (e) {
    console.error('Failed to create team', e)
  }
}

async function updateTeamHandler() {
  try {
    await updateTeam(selection(), editingTeamId.value, form.value.name, form.value.description, form.value.coordinationMode, form.value.memberConfig)
    await loadTeams()
    closeDialog()
  } catch (e) {
    console.error('Failed to update team', e)
  }
}

async function toggleTeam(team: AgentTeam) {
  try {
    if (team.status === 'ACTIVE') {
      await deactivateTeam(selection(), team.id)
    } else {
      await activateTeam(selection(), team.id)
    }
    await loadTeams()
  } catch (e) {
    console.error('Failed to toggle team', e)
  }
}

async function deleteTeamHandler(id: string) {
  if (confirm('确定删除此团队？')) {
    try {
      await deleteTeam(selection(), id)
      await loadTeams()
    } catch (e) {
      console.error('Failed to delete team', e)
    }
  }
}

function editTeam(team: AgentTeam) {
  editingTeamId.value = team.id
  form.value = {
    teamCode: team.teamCode,
    name: team.name,
    description: team.description,
    coordinationMode: team.coordinationMode,
    memberConfig: team.memberConfig
  }
  showEditDialog.value = true
}

function closeDialog() {
  showCreateDialog.value = false
  showEditDialog.value = false
  form.value = { teamCode: '', name: '', description: '', coordinationMode: 'SEQUENTIAL', memberConfig: '{}' }
}

function formatDate(date: string): string {
  return new Date(date).toLocaleString()
}

function getStatusClass(status: string): string {
  switch (status) {
    case 'ACTIVE': return 'status-active'
    case 'INACTIVE': return 'status-inactive'
    default: return 'status-draft'
  }
}
</script>

<style scoped>
.team-management {
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

.status-active {
  color: #28a745;
}

.status-inactive {
  color: #dc3545;
}

.status-draft {
  color: #6c757d;
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

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
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
