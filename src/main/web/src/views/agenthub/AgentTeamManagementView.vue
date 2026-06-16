<template>
  <section class="team-management glass-float">
    <div class="page-header">
      <h2>Agent团队</h2>
      <p class="muted">管理多Agent协作团队</p>
    </div>

    <div class="team-list float-effect">
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
              <CustomButton type="ghost" @click="editTeam(team)">编辑</CustomButton>
              <CustomButton type="ghost" @click="toggleTeam(team)">
                {{ team.status === 'ACTIVE' ? '停用' : '激活' }}
              </CustomButton>
              <CustomButton type="ghost" @click="deleteTeamHandler(team.id)">删除</CustomButton>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">
        <p>暂无团队数据</p>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <ModalDialog
      v-model:visible="showCreateDialog"
      :title="editingTeamId ? '编辑团队' : '新建团队'"
      @close="closeDialog"
      @confirm="editingTeamId ? updateTeamHandler() : createTeamHandler()"
      :confirm-text="editingTeamId ? '更新' : '创建'"
    >
      <form class="field-grid">
        <label class="field">
          <span>团队编码 *</span>
          <input v-model="form.teamCode" :disabled="!!editingTeamId" required />
        </label>
        <label class="field">
          <span>名称 *</span>
          <input v-model="form.name" required />
        </label>
        <label class="field">
          <span>描述</span>
          <textarea v-model="form.description" rows="3"></textarea>
        </label>
        <label class="field">
          <span>协调模式 *</span>
          <CustomSelect v-model="form.coordinationMode" :options="coordinationModeOptions" placeholder="选择协调模式" />
        </label>
        <label class="field">
          <span>成员配置 (JSON)</span>
          <textarea v-model="form.memberConfig" rows="5"></textarea>
        </label>
      </form>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showConfirm } from '@/utils/confirm'
import { useWorkspaceStore } from '@/store/workspace-store'
import { listTeams, createTeam, updateTeam, activateTeam, deactivateTeam, deleteTeam } from '@/api/team-api'
import type { AgentTeam } from '@/types/memory'
import CustomSelect from '@/components/CustomSelect.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const teams = ref<AgentTeam[]>([])
const showCreateDialog = ref(false)
const editingTeamId = ref('')

const form = ref({
  teamCode: '',
  name: '',
  description: '',
  coordinationMode: 'SEQUENTIAL',
  memberConfig: '{}'
})

const coordinationModeOptions = [
  { value: 'SEQUENTIAL', label: '顺序执行' },
  { value: 'PARALLEL', label: '并行执行' },
  { value: 'HIERARCHICAL', label: '层级协调' },
  { value: 'CONSENSUS', label: '共识决策' },
]

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

onMounted(async () => {
  // 监听全局新增事件
  window.addEventListener('global-add', () => {
    editingTeamId.value = ''
    resetForm()
    showCreateDialog.value = true
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
  try { await performToggleTeam(team) } catch (e) { console.error('Failed to toggle team', e) }
}

async function performToggleTeam(team: AgentTeam) {
  if (team.status === 'ACTIVE') { await deactivateTeam(selection(), team.id) } else { await activateTeam(selection(), team.id) }
  await loadTeams()
}

async function deleteTeamHandler(id: string) {
  if (await showConfirm('确定删除此团队？')) {
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
  form.value = { teamCode: team.teamCode, name: team.name, description: team.description, coordinationMode: team.coordinationMode, memberConfig: team.memberConfig }
  showCreateDialog.value = true
}

function resetForm() {
  form.value = { teamCode: '', name: '', description: '', coordinationMode: 'SEQUENTIAL', memberConfig: '{}' }
}

function closeDialog() {
  showCreateDialog.value = false
  editingTeamId.value = ''
  resetForm()
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

.btn-primary {
  padding: 0.5rem 1rem;
  background: var(--color-primary); color: var(--color-text-inverse);
  border: none;
  cursor: pointer;
}

.btn-secondary {
  padding: 0.5rem 1rem;
  background: var(--color-text-muted); color: var(--color-text-inverse);
  border: none;
  cursor: pointer;
}

.status-active {
  color: var(--color-success);
}

.status-inactive {
  color: var(--color-error);
}

.status-draft {
  color: var(--color-text-muted);
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

.empty-state {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-muted);
}
</style>
