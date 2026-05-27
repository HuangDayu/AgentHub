<template>
  <section class="skill-management glass-float">
    <div class="page-header">
      <div>
      <h2>技能</h2>
      <p class="muted">管理Agent可调用的技能定义</p>
      </div>
    </div>

    <div class="toolbar">
      
    </div>

    <div class="skill-list float-effect">
      <table v-if="skills.length > 0">
        <thead>
          <tr>
            <th>编码</th>
            <th>名称</th>
            <th>类型</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="skill in skills" :key="skill.id">
            <td>{{ skill.skillCode }}</td>
            <td>{{ skill.name }}</td>
            <td>{{ skill.skillType }}</td>
            <td>
              <span :class="skill.enabled ? 'status-enabled' : 'status-disabled'">
                {{ skill.enabled ? '已启用' : '已禁用' }}
              </span>
            </td>
            <td>{{ formatDate(skill.createdAt) }}</td>
            <td>
              <CustomButton type="ghost" @click="editSkill(skill)">编辑</CustomButton>
              <CustomButton type="ghost" @click="toggleSkill(skill)">
                {{ skill.enabled ? '禁用' : '启用' }}
              </CustomButton>
              <CustomButton type="ghost" @click="deleteSkillHandler(skill.id)">删除</CustomButton>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">
        <p>暂无技能数据</p>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <ModalDialog
      v-model:visible="showCreateDialog"
      :title="showEditDialog ? '编辑技能' : '新建技能'"
      @confirm="showEditDialog ? updateSkillHandler() : createSkillHandler()"
      @close="closeDialog"
      :confirm-text="showEditDialog ? '更新' : '创建'"
    >
      <form>
<div class="form-group">
            <label>技能编码</label>
            <input v-model="form.skillCode" :disabled="showEditDialog" required />
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
            <label>技能类型</label>
            <select v-model="form.skillType" required>
              <option value="FUNCTION">函数</option>
              <option value="API">API调用</option>
              <option value="WORKFLOW">工作流</option>
            </select>
          </div>
          <div class="form-group">
            <label>定义 (JSON)</label>
            <textarea v-model="form.definition" rows="5"></textarea>
          </div>
      </form>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { showConfirm } from '@/utils/confirm'
import { useWorkspaceStore } from '@/store/workspace-store'
import { listSkills, createSkill, updateSkill, enableSkill, disableSkill, deleteSkill, syncSkills } from '@/api/skill-api'
import type { Skill } from '@/types/memory'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
const skills = ref<Skill[]>([])
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingSkillId = ref('')

const form = ref({
  skillCode: '',
  name: '',
  description: '',
  skillType: 'FUNCTION',
  definition: '{}'
})

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

// 监听全局新增事件
const handleGlobalAdd = () => {
  editingSkillId.value = ''
  showCreateDialog.value = true
}

const handleGlobalSync = () => {
  if (window.location.pathname.includes('skill')) {
    syncSkillsHandler()
  }
}

onMounted(() => {
  window.addEventListener('global-add', handleGlobalAdd)
  window.addEventListener('global-sync', handleGlobalSync)
  loadSkills()
})

onUnmounted(() => {
  window.removeEventListener('global-add', handleGlobalAdd)
  window.removeEventListener('global-sync', handleGlobalSync)
})

async function loadSkills() {
  try {
    skills.value = await listSkills(selection())
  } catch (e) {
    console.error('Failed to load skills', e)
  }
}

async function syncSkillsHandler() {
  try {
    await syncSkills(selection())
    await loadSkills()
  } catch (e) {
    console.error('Failed to sync skills', e)
  }
}

async function createSkillHandler() {
  try {
    await createSkill(selection(), form.value.skillCode, form.value.name, form.value.description, form.value.skillType, form.value.definition, '{}')
    await loadSkills()
    closeDialog()
  } catch (e) {
    console.error('Failed to create skill', e)
  }
}

async function updateSkillHandler() {
  try {
    await updateSkill(selection(), editingSkillId.value, form.value.name, form.value.description, form.value.definition, '{}')
    await loadSkills()
    closeDialog()
  } catch (e) {
    console.error('Failed to update skill', e)
  }
}

async function toggleSkill(skill: Skill) {
  try {
    if (skill.enabled) {
      await disableSkill(selection(), skill.id)
    } else {
      await enableSkill(selection(), skill.id)
    }
    await loadSkills()
  } catch (e) {
    console.error('Failed to toggle skill', e)
  }
}

async function deleteSkillHandler(id: string) {
  if (await showConfirm('确定删除此技能？')) {
    try {
      await deleteSkill(selection(), id)
      await loadSkills()
    } catch (e) {
      console.error('Failed to delete skill', e)
    }
  }
}

function editSkill(skill: Skill) {
  editingSkillId.value = skill.id
  form.value = {
    skillCode: skill.skillCode,
    name: skill.name,
    description: skill.description,
    skillType: skill.skillType,
    definition: skill.definition
  }
  showCreateDialog.value = true
  showEditDialog.value = true
}

function closeDialog() {
  showCreateDialog.value = false
  showEditDialog.value = false
  form.value = { skillCode: '', name: '', description: '', skillType: 'FUNCTION', definition: '{}' }
}

function formatDate(date: string): string {
  return new Date(date).toLocaleString()
}
</script>

<style scoped>
.skill-management {
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

.status-enabled {
  color: var(--color-success);
}

.status-disabled {
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
  background: var(--bg-card-solid);
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
  color: var(--color-text-muted);
}
</style>
