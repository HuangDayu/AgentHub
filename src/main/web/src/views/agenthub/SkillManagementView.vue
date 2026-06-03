<template>
  <section class="skill-management glass-float">
    <div class="page-header">
      <div>
        <h2>技能</h2>
        <p class="muted">管理Agent可调用的技能定义</p>
      </div>
      <div class="search-box">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.35-4.35" stroke-linecap="round"/>
        </svg>
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索技能名称、编码..."
          class="search-input"
          @input="onSearchInput"
        />
        <button v-if="searchKeyword" class="search-clear" @click="clearSearch">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
            <path d="M18 6 6 18M6 6l12 12" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>

    <div class="skill-list float-effect">
      <table v-if="skills.length > 0">
        <thead>
          <tr>
            <th>编码</th>
            <th>名称</th>
            <th>类型</th>
            <th>来源</th>
            <th>文件数</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="skill in skills" :key="skill.id">
            <td>{{ skill.skillCode }}</td>
            <td>{{ skill.name }}</td>
            <td>
              <span class="badge" :class="getSkillTypeClass(skill.skillType)">
                {{ getSkillTypeLabel(skill.skillType) }}
              </span>
            </td>
            <td>{{ getSourceLabel(skill.source) }}</td>
            <td>{{ skill.fileCount || 0 }}</td>
            <td>
              <span :class="skill.enabled ? 'status-enabled' : 'status-disabled'">
                {{ skill.enabled ? '已启用' : '已禁用' }}
              </span>
            </td>
            <td class="actions-cell">
              <CustomButton type="ghost" size="small" @click="viewSkill(skill)">详情</CustomButton>
              <CustomButton type="ghost" size="small" @click="editSkill(skill)">编辑</CustomButton>
              <CustomButton type="ghost" size="small" @click="toggleSkill(skill)">
                {{ skill.enabled ? '禁用' : '启用' }}
              </CustomButton>
              <CustomButton type="ghost" size="small" @click="deleteSkillHandler(skill.id)">删除</CustomButton>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">
        <p>暂无技能数据</p>
      </div>
    </div>

    <!-- 新建技能对话框 - Tab页 -->
    <ModalDialog
      v-model:visible="showCreateDialog"
      title="新建技能"
      @confirm="handleCreateConfirm"
      @close="closeCreateDialog"
      confirm-text="创建"
      size="medium"
    >
      <div class="skill-tabs">
        <div
          v-for="tab in createTabs"
          :key="tab.key"
          class="skill-tab-item"
          :class="{ active: activeCreateTab === tab.key }"
          @click="activeCreateTab = tab.key"
        >
          <span class="tab-icon" v-html="tab.icon"></span>
          <span class="tab-label">{{ tab.label }}</span>
          <span class="tab-desc">{{ tab.desc }}</span>
        </div>
      </div>

      <!-- 本地路径 -->
      <form v-if="activeCreateTab === 'local'" class="skill-create-form">
        <div class="form-section">
          <div class="form-section-header">
            <span class="section-icon">📁</span>
            <span>选择本地技能目录</span>
          </div>
          <div class="form-group">
            <label>路径 <span class="required">*</span></label>
            <input
              v-model="createForm.skillPath"
              placeholder="C:\Users\xxx\.agents\skills\my-skill"
              class="form-input"
            />
            <span class="form-hint">指向包含 SKILL.md 的技能根目录</span>
          </div>
        </div>
        <div class="form-section optional-section">
          <div class="form-section-header">
            <span class="section-icon">⚙️</span>
            <span>高级选项</span>
            <span class="optional-tag">可选</span>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>编码</label>
              <input
                v-model="createForm.skillCode"
                placeholder="自动从路径提取"
                class="form-input"
              />
              <span class="form-hint">留空则从目录名自动生成</span>
            </div>
            <div class="form-group">
              <label>名称</label>
              <input
                v-model="createForm.name"
                placeholder="自动从编码提取"
                class="form-input"
              />
              <span class="form-hint">留空则从编码自动生成</span>
            </div>
          </div>
        </div>
      </form>

      <!-- 网络压缩包 -->
      <form v-if="activeCreateTab === 'url'" class="skill-create-form">
        <div class="form-section">
          <div class="form-section-header">
            <span class="section-icon">🔗</span>
            <span>填写ZIP下载链接</span>
          </div>
          <div class="form-group">
            <label>ZIP URL <span class="required">*</span></label>
            <input
              v-model="urlForm.zipUrl"
              placeholder="https://example.com/skill.zip"
              class="form-input"
            />
            <span class="form-hint">支持任意可访问的ZIP文件下载地址</span>
          </div>
        </div>
        <div class="form-section optional-section">
          <div class="form-section-header">
            <span class="section-icon">⚙️</span>
            <span>高级选项</span>
            <span class="optional-tag">可选</span>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>编码</label>
              <input
                v-model="urlForm.skillCode"
                placeholder="自动从URL提取"
                class="form-input"
              />
              <span class="form-hint">留空则从文件名自动生成</span>
            </div>
            <div class="form-group">
              <label>名称</label>
              <input
                v-model="urlForm.name"
                placeholder="自动从编码提取"
                class="form-input"
              />
              <span class="form-hint">留空则从编码自动生成</span>
            </div>
          </div>
        </div>
      </form>

      <!-- 本地上传压缩包 -->
      <form v-if="activeCreateTab === 'upload'" class="skill-create-form">
        <div class="form-section">
          <div class="form-section-header">
            <span class="section-icon">📦</span>
            <span>上传ZIP文件</span>
          </div>
          <div class="form-group">
            <label>选择文件 <span class="required">*</span></label>
            <div
              class="upload-dropzone"
              :class="{ 'has-file': uploadForm.file, 'drag-over': isDragging }"
              @click="triggerFileInput"
              @dragover.prevent="isDragging = true"
              @dragleave="isDragging = false"
              @drop.prevent="handleDrop"
            >
              <input ref="fileInputRef" type="file" accept=".zip" @change="handleFileSelect" hidden />
              <template v-if="!uploadForm.file">
                <div class="upload-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" stroke-linecap="round" stroke-linejoin="round"/>
                    <polyline points="17 8 12 3 7 8" stroke-linecap="round" stroke-linejoin="round"/>
                    <line x1="12" y1="3" x2="12" y2="15" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <div class="upload-text">
                  <span class="upload-main">拖拽文件到此处，或 <span class="upload-link">点击选择</span></span>
                  <span class="upload-sub">仅支持 .zip 格式</span>
                </div>
              </template>
              <template v-else>
                <div class="upload-file-info">
                  <span class="file-icon">📄</span>
                  <div class="file-details">
                    <span class="file-name">{{ uploadForm.file.name }}</span>
                    <span class="file-size">{{ formatFileSize(uploadForm.file.size) }}</span>
                  </div>
                  <button type="button" class="file-remove-btn" @click.stop="uploadForm.file = null">✕</button>
                </div>
              </template>
            </div>
          </div>
        </div>
        <div class="form-section optional-section">
          <div class="form-section-header">
            <span class="section-icon">⚙️</span>
            <span>高级选项</span>
            <span class="optional-tag">可选</span>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>编码</label>
              <input
                v-model="uploadForm.skillCode"
                placeholder="自动生成"
                class="form-input"
              />
              <span class="form-hint">留空则使用时间戳自动生成</span>
            </div>
            <div class="form-group">
              <label>名称</label>
              <input
                v-model="uploadForm.name"
                placeholder="自动从编码提取"
                class="form-input"
              />
              <span class="form-hint">留空则从编码自动生成</span>
            </div>
          </div>
        </div>
      </form>

      <!-- 技能市场 -->
      <div v-if="activeCreateTab === 'market'" class="market-search">
        <div class="market-search-bar">
          <input
            v-model="marketSearchKeyword"
            type="text"
            placeholder="搜索技能市场..."
            class="form-input"
            @keyup.enter="handleMarketSearch"
          />
          <button class="btn-primary market-search-btn" @click="handleMarketSearch" :disabled="marketSearching">
            {{ marketSearching ? '搜索中...' : '搜索' }}
          </button>
        </div>

        <div v-if="Object.keys(marketResults).length > 0" class="market-results">
          <div v-for="(skills, marketId) in marketResults" :key="marketId" class="market-group">
            <h4 class="market-title">{{ marketId }}</h4>
            <div class="market-cards">
              <div
                v-for="skill in skills"
                :key="skill.skillId"
                class="market-card"
                @click="viewMarketSkill(skill)"
              >
                <div class="market-card-header">
                  <span class="market-card-name">{{ skill.name }}</span>
                  <span class="market-card-version">v{{ skill.version }}</span>
                </div>
                <p class="market-card-desc">{{ skill.description }}</p>
                <div class="market-card-meta">
                  <span>{{ skill.author }}</span>
                  <span>&#11015; {{ skill.downloadCount }}</span>
                  <span>&#9733; {{ skill.starCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="!marketSearching && marketSearchKeyword" class="empty-state">
          <p>未找到相关技能</p>
        </div>
        <div v-else class="empty-state">
          <p>输入关键词搜索技能市场</p>
        </div>
      </div>
    </ModalDialog>

    <!-- 市场技能详情弹窗 -->
    <ModalDialog
      v-model:visible="showMarketDetail"
      title="技能详情"
      size="large"
      :show-footer="true"
      confirm-text="安装"
      @confirm="handleInstallFromMarket"
      @close="showMarketDetail = false; selectedMarketSkill = null; marketDetail = null"
    >
      <div v-if="selectedMarketSkill" class="market-detail">
        <h3 class="market-detail-name">{{ selectedMarketSkill.name }}</h3>
        <p class="market-detail-meta">
          作者: {{ selectedMarketSkill.author }} | 版本: v{{ selectedMarketSkill.version }}
          | 下载: {{ selectedMarketSkill.downloadCount }} | 收藏: {{ selectedMarketSkill.starCount }}
        </p>
        <p class="market-detail-desc">{{ selectedMarketSkill.description }}</p>
        <div v-if="loadingMarketDetail" class="empty-state">
          <p>加载详情中...</p>
        </div>
        <div v-else-if="marketDetail?.readmeContent" class="market-detail-readme">
          <MarkdownRenderer :content="marketDetail.readmeContent" />
        </div>
      </div>
    </ModalDialog>

    <!-- 编辑技能对话框 -->
    <ModalDialog
      v-model:visible="showEditDialog"
      title="编辑技能"
      @confirm="updateSkillHandler"
      @close="closeEditDialog"
      confirm-text="更新"
    >
      <form>
        <div class="form-group">
          <label>技能编码</label>
          <input :value="editForm.skillCode" disabled />
        </div>
        <div class="form-group">
          <label>名称</label>
          <input v-model="editForm.name" required />
        </div>
        <div class="form-group">
          <label>描述</label>
          <textarea v-model="editForm.description" rows="3"></textarea>
        </div>
        <div class="form-group">
          <label>本地路径</label>
          <input v-model="editForm.skillPath" />
        </div>
      </form>
    </ModalDialog>

    <!-- 技能详情对话框 -->
    <SkillDetailModal
      v-model:visible="showDetailDialog"
      :skill="selectedSkill"
      @close="closeDetailDialog"
    />
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { showConfirm } from '@/utils/confirm'
import { useWorkspaceStore } from '@/store/workspace-store'
import {
  listSkills,
  searchSkills,
  createSkill,
  createSkillFromUrl,
  createSkillFromUpload,
  updateSkill,
  enableSkill,
  disableSkill,
  deleteSkill,
  syncAllSkills,
  searchMarketSkills,
  getMarketSkillDetail,
  installMarketSkill
} from '@/api/skill-api'
import type { Skill, MarketSkillSummary, MarketSkillDetail } from '@/types/memory'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import SkillDetailModal from './SkillDetailModal.vue'

const store = useWorkspaceStore()
const skills = ref<Skill[]>([])
const syncing = ref(false)
const selectionReady = computed(() => !!store.tenantId && !!store.workspaceId)
const searchKeyword = ref('')
let searchTimer: ReturnType<typeof setTimeout> | null = null

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showDetailDialog = ref(false)
const activeCreateTab = ref('local')
const fileInputRef = ref<HTMLInputElement | null>(null)

const createTabs = [
  {
    key: 'local',
    label: '本地路径',
    desc: '从本地文件系统导入',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18"><path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  },
  {
    key: 'url',
    label: '网络压缩包',
    desc: '从URL下载ZIP',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18"><path d="M10 13a5 5 0 007.54.54l3-3a5 5 0 00-7.07-7.07l-1.72 1.71" stroke-linecap="round" stroke-linejoin="round"/><path d="M14 11a5 5 0 00-7.54-.54l-3 3a5 5 0 007.07 7.07l1.71-1.71" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  },
  {
    key: 'upload',
    label: '本地上传',
    desc: '上传ZIP文件',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" stroke-linecap="round" stroke-linejoin="round"/><polyline points="17 8 12 3 7 8" stroke-linecap="round" stroke-linejoin="round"/><line x1="12" y1="3" x2="12" y2="15" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  },
  {
    key: 'market',
    label: '技能市场',
    desc: '从市场安装',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" stroke-linecap="round" stroke-linejoin="round"/><line x1="3" y1="6" x2="21" y2="6" stroke-linecap="round" stroke-linejoin="round"/><path d="M16 10a4 4 0 01-8 0" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  }
]

const isDragging = ref(false)

const createForm = ref({ skillCode: '', name: '', skillPath: '' })
const urlForm = ref({ skillCode: '', name: '', zipUrl: '' })
const uploadForm = ref({ skillCode: '', name: '', file: null as File | null })
const editForm = ref({ skillCode: '', name: '', description: '', skillPath: '' })
const editingSkillId = ref('')
const selectedSkill = ref<Skill | null>(null)

const marketSearchKeyword = ref('')
const marketResults = ref<Record<string, MarketSkillSummary[]>>({})
const marketSearching = ref(false)
const selectedMarketSkill = ref<MarketSkillSummary | null>(null)
const showMarketDetail = ref(false)
const marketDetail = ref<MarketSkillDetail | null>(null)
const loadingMarketDetail = ref(false)
const installingFromMarket = ref(false)

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

const handleGlobalAdd = () => { showCreateDialog.value = true }
const handleGlobalSync = () => {
  if (window.location.pathname.includes('skill')) {
    syncAllSkillsHandler()
  }
}

onMounted(() => {
  window.addEventListener('global-add', handleGlobalAdd)
  window.addEventListener('global-sync', handleGlobalSync)
  if (selectionReady.value) {
    loadSkills()
  }
})

watch(() => [store.tenantId, store.workspaceId], () => {
  if (selectionReady.value) {
    loadSkills()
  }
})

onUnmounted(() => {
  window.removeEventListener('global-add', handleGlobalAdd)
  window.removeEventListener('global-sync', handleGlobalSync)
})

async function loadSkills() {
  if (!selectionReady.value) return
  try {
    const keyword = searchKeyword.value.trim()
    if (keyword) {
      skills.value = await searchSkills(selection(), keyword)
    } else {
      skills.value = await listSkills(selection())
    }
  } catch (e) {
    console.error('Failed to load skills', e)
  }
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadSkills()
  }, 300)
}

function clearSearch() {
  searchKeyword.value = ''
  loadSkills()
}

async function syncAllSkillsHandler() {
  if (!selectionReady.value) return
  syncing.value = true
  try {
    await syncAllSkills(selection())
    await loadSkills()
  } catch (e) {
    console.error('Failed to sync skills', e)
  } finally {
    syncing.value = false
  }
}

async function handleCreateConfirm() {
  if (activeCreateTab.value === 'local') {
    await createLocalSkill()
  } else if (activeCreateTab.value === 'url') {
    await createFromUrlHandler()
  } else if (activeCreateTab.value === 'upload') {
    await createFromUploadHandler()
  }
}

async function createLocalSkill() {
  try {
    await createSkill(selection(), createForm.value.skillCode, createForm.value.name, '', createForm.value.skillPath)
    await loadSkills()
    closeCreateDialog()
  } catch (e) {
    console.error('Failed to create skill', e)
  }
}

async function createFromUrlHandler() {
  try {
    await createSkillFromUrl(selection(), urlForm.value.skillCode, urlForm.value.name, '', urlForm.value.zipUrl)
    await loadSkills()
    closeCreateDialog()
  } catch (e) {
    console.error('Failed to create skill from URL', e)
  }
}

async function createFromUploadHandler() {
  if (!uploadForm.value.file) return
  try {
    await createSkillFromUpload(selection(), uploadForm.value.skillCode, uploadForm.value.name, '', uploadForm.value.file)
    await loadSkills()
    closeCreateDialog()
  } catch (e) {
    console.error('Failed to create skill from upload', e)
  }
}

async function updateSkillHandler() {
  try {
    await updateSkill(selection(), editingSkillId.value, editForm.value.name, editForm.value.description, editForm.value.skillPath)
    await loadSkills()
    closeEditDialog()
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

function viewSkill(skill: Skill) {
  selectedSkill.value = skill
  showDetailDialog.value = true
}

function editSkill(skill: Skill) {
  editingSkillId.value = skill.id
  editForm.value = {
    skillCode: skill.skillCode,
    name: skill.name,
    description: skill.description,
    skillPath: skill.skillPath
  }
  showEditDialog.value = true
}

function triggerFileInput() {
  fileInputRef.value?.click()
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    uploadForm.value.file = target.files[0]
  }
}

function handleDrop(event: DragEvent) {
  isDragging.value = false
  const files = event.dataTransfer?.files
  if (files && files.length > 0 && files[0].name.endsWith('.zip')) {
    uploadForm.value.file = files[0]
  }
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

function getSkillTypeClass(skillType: string): string {
  switch (skillType) {
    case 'SYNCED': return 'badge-success'
    case 'UPLOADED': return 'badge-info'
    default: return 'badge-default'
  }
}

function getSkillTypeLabel(skillType: string): string {
  switch (skillType) {
    case 'SYNCED': return '同步'
    case 'UPLOADED': return '上传'
    default: return skillType || '未知'
  }
}

function getSourceLabel(source: string): string {
  switch (source) {
    case 'LOCAL': return '本地'
    case 'URL': return 'URL'
    case 'UPLOAD': return '上传'
    default: return source || '未知'
  }
}

function closeCreateDialog() {
  showCreateDialog.value = false
  activeCreateTab.value = 'local'
  createForm.value = { skillCode: '', name: '', skillPath: '' }
  urlForm.value = { skillCode: '', name: '', zipUrl: '' }
  uploadForm.value = { skillCode: '', name: '', file: null }
  marketSearchKeyword.value = ''
  marketResults.value = {}
  selectedMarketSkill.value = null
  showMarketDetail.value = false
  marketDetail.value = null
}

function closeEditDialog() {
  showEditDialog.value = false
  editForm.value = { skillCode: '', name: '', description: '', skillPath: '' }
}

function closeDetailDialog() {
  showDetailDialog.value = false
  selectedSkill.value = null
}

async function handleMarketSearch() {
  if (!marketSearchKeyword.value.trim()) return
  marketSearching.value = true
  try {
    marketResults.value = await searchMarketSkills(selection(), { keyword: marketSearchKeyword.value })
  } catch (e: any) {
    console.error(e.message || '搜索失败')
  } finally {
    marketSearching.value = false
  }
}

async function viewMarketSkill(skill: MarketSkillSummary) {
  selectedMarketSkill.value = skill
  showMarketDetail.value = true
  loadingMarketDetail.value = true
  try {
    marketDetail.value = await getMarketSkillDetail(selection(), skill.marketId, skill.skillId)
  } catch (e: any) {
    console.error(e.message || '获取详情失败')
  } finally {
    loadingMarketDetail.value = false
  }
}

async function handleInstallFromMarket() {
  if (!selectedMarketSkill.value) return
  installingFromMarket.value = true
  try {
    await installMarketSkill(selection(), { marketId: selectedMarketSkill.value.marketId, skillId: selectedMarketSkill.value.skillId })
    console.info('安装成功')
    showMarketDetail.value = false
    selectedMarketSkill.value = null
    marketDetail.value = null
    await loadSkills()
  } catch (e: any) {
    console.error(e.message || '安装失败')
  } finally {
    installingFromMarket.value = false
  }
}
</script>

<style scoped>
.skill-management {
  padding: 2rem;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  color: var(--color-text-muted);
  pointer-events: none;
}

.search-input {
  width: 240px;
  padding: 0.5rem 2rem 0.5rem 2rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-input);
  color: var(--color-text);
  font-size: 0.875rem;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(var(--color-primary-rgb), 0.15);
}

.search-input::placeholder {
  color: var(--color-text-muted);
}

.search-clear {
  position: absolute;
  right: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.15s;
}

.search-clear:hover {
  background: var(--bg-hover);
  color: var(--color-text);
}

.status-enabled {
  color: var(--color-success);
}

.status-disabled {
  color: var(--color-text-muted);
}

.badge {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.badge-success {
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
}

.badge-info {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.badge-default {
  background: var(--color-text-muted);
  color: var(--color-text);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

th {
  font-weight: 600;
  font-size: 0.75rem;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.actions-cell {
  display: flex;
  gap: 0.25rem;
}

/* Tab navigation */
.skill-tabs {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.skill-tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.375rem;
  padding: 1rem 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--bg-input);
}

.skill-tab-item:hover {
  border-color: var(--color-primary);
  background: rgba(59, 130, 246, 0.05);
  transform: translateY(-1px);
}

.skill-tab-item.active {
  border-color: var(--color-primary);
  background: rgba(59, 130, 246, 0.08);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.skill-tab-item .tab-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--bg-hover);
  color: var(--color-text-muted);
  transition: all 0.2s;
}

.skill-tab-item.active .tab-icon {
  background: var(--color-primary);
  color: white;
}

.skill-tab-item .tab-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text);
}

.skill-tab-item .tab-desc {
  font-size: 0.7rem;
  color: var(--color-text-muted);
}

/* Form layout */
.skill-create-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-section {
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 1.25rem;
}

.optional-section {
  background: transparent;
  border-style: dashed;
}

.form-section-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text);
}

.form-section-header .section-icon {
  font-size: 1rem;
}

.form-section-header .optional-tag {
  margin-left: auto;
  font-size: 0.7rem;
  font-weight: 500;
  padding: 0.125rem 0.5rem;
  background: var(--bg-hover);
  border-radius: 4px;
  color: var(--color-text-muted);
}

/* Form fields */
.form-group {
  margin-bottom: 0.75rem;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 0.375rem;
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--color-text);
}

.form-group label .required {
  color: var(--color-error);
  margin-left: 0.125rem;
}

.form-input {
  width: 100%;
  padding: 0.625rem 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-input);
  color: var(--color-text);
  font-size: 0.875rem;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input::placeholder {
  color: var(--color-text-muted);
  opacity: 0.6;
}

.form-hint {
  display: block;
  margin-top: 0.375rem;
  font-size: 0.7rem;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

/* Upload dropzone */
.upload-dropzone {
  border: 2px dashed var(--border-color);
  border-radius: 10px;
  padding: 2rem 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s ease;
  background: var(--bg-input);
}

.upload-dropzone:hover {
  border-color: var(--color-primary);
  background: rgba(59, 130, 246, 0.03);
}

.upload-dropzone.drag-over {
  border-color: var(--color-primary);
  background: rgba(59, 130, 246, 0.06);
  transform: scale(1.01);
}

.upload-dropzone.has-file {
  border-style: solid;
  border-color: var(--color-success);
  background: rgba(34, 197, 94, 0.03);
  padding: 1rem 1.5rem;
}

.upload-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 0.75rem;
  color: var(--color-text-muted);
  opacity: 0.6;
}

.upload-dropzone:hover .upload-icon {
  opacity: 1;
  color: var(--color-primary);
}

.upload-text {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.upload-main {
  font-size: 0.875rem;
  color: var(--color-text);
}

.upload-link {
  color: var(--color-primary);
  font-weight: 500;
}

.upload-sub {
  font-size: 0.7rem;
  color: var(--color-text-muted);
}

.upload-file-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.upload-file-info .file-icon {
  font-size: 1.5rem;
}

.upload-file-info .file-details {
  flex: 1;
  text-align: left;
}

.upload-file-info .file-name {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-file-info .file-size {
  font-size: 0.7rem;
  color: var(--color-text-muted);
}

.file-remove-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: var(--bg-hover);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.file-remove-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  color: var(--color-error);
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-muted);
}

/* Market search */
.market-search {
  margin-top: 8px;
}

.market-search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.market-search-bar .form-input {
  flex: 1;
}

.btn-primary {
  padding: 0.625rem 1rem;
  border: none;
  border-radius: 6px;
  background: var(--color-primary);
  color: white;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.market-search-btn {
  flex-shrink: 0;
}

.market-group {
  margin-bottom: 20px;
}

.market-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.market-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.market-card {
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--bg-input);
}

.market-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.market-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.market-card-name {
  font-weight: 600;
  color: var(--color-text);
}

.market-card-version {
  font-size: 12px;
  color: var(--color-text-muted);
}

.market-card-desc {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.market-card-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--color-text-muted);
}

/* Market detail */
.market-detail-name {
  margin: 0 0 8px;
  font-size: 1.25rem;
}

.market-detail-meta {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 12px;
}

.market-detail-desc {
  margin-bottom: 16px;
  line-height: 1.6;
}

.market-detail-readme {
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  background: var(--bg-input);
  border-radius: 6px;
}
</style>
