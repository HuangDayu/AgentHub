<template>
  <ModalDialog
    v-model:visible="visible"
    :title="skill?.name || '技能详情'"
    @close="$emit('close')"
    :show-footer="false"
    size="xxlarge"
  >
    <div v-if="skill" class="skill-detail">
      <div class="detail-header">
        <div class="info-row">
          <div class="info-item">
            <label>编码</label>
            <span>{{ skill.skillCode }}</span>
          </div>
          <div class="info-item">
            <label>类型</label>
            <span class="badge" :class="skillTypeClass">{{ skillTypeLabel }}</span>
          </div>
          <div class="info-item">
            <label>来源</label>
            <span>{{ sourceLabel }}</span>
          </div>
          <div class="info-item">
            <label>文件数</label>
            <span>{{ skill.fileCount || 0 }}</span>
          </div>
          <div class="info-item">
            <label>总大小</label>
            <span>{{ formatSize(skill.totalSize) }}</span>
          </div>
          <div class="info-item">
            <label>最后同步</label>
            <span>{{ skill.lastSyncAt ? formatDate(skill.lastSyncAt) : '未同步' }}</span>
          </div>
        </div>
      </div>

      <div class="detail-body">
        <!-- 左侧：文件树 -->
        <div class="file-tree-panel">
          <div class="panel-header">
            <h3>文件列表</h3>
            <CustomButton type="ghost" size="small" @click="refreshFiles" :loading="loadingFiles">
              刷新
            </CustomButton>
          </div>
          <div v-if="loadingFiles" class="loading-state">
            <p>加载中...</p>
          </div>
          <div v-else-if="flatTree.length === 0" class="empty-state">
            <p>暂无文件</p>
          </div>
          <div v-else class="file-tree">
            <template v-for="node in flatTree" :key="node._key">
              <div
                class="tree-item"
                :class="{
                  active: selectedFilePath === node.path,
                  'is-directory': node.isDirectory
                }"
                :style="{ paddingLeft: `${node._depth * 16 + 8}px` }"
                @click="onNodeClick(node)"
              >
                <span
                  v-if="node.isDirectory"
                  class="tree-arrow"
                  :class="{ expanded: expandedDirs.has(node.path) }"
                >
                  <svg viewBox="0 0 16 16" width="12" height="12" fill="currentColor">
                    <path d="M6 4l4 4-4 4z"/>
                  </svg>
                </span>
                <span v-else class="tree-arrow-placeholder"></span>
                <span class="tree-icon">{{ node.isDirectory ? (expandedDirs.has(node.path) ? '📂' : '📁') : getFileIcon(node.name) }}</span>
                <span class="tree-name">{{ node.name }}</span>
                <span v-if="!node.isDirectory" class="tree-size">{{ formatSize(node.size) }}</span>
              </div>
            </template>
          </div>
        </div>

        <!-- 右侧：文件内容 -->
        <div class="file-content-panel">
          <div v-if="!selectedFilePath" class="content-placeholder">
            <p>选择文件查看内容</p>
          </div>
          <template v-else>
            <div class="panel-header">
              <h3>{{ selectedFilePath.split('/').pop() }}</h3>
              <CustomButton type="ghost" size="small" @click="selectedFilePath = ''">
                关闭
              </CustomButton>
            </div>
            <div v-if="loadingContent" class="loading-state">
              <p>加载中...</p>
            </div>
            <div v-else class="file-content-scroll">
              <!-- Markdown 渲染 -->
              <div v-if="isMarkdown" class="file-content markdown-body">
                <MarkdownRenderer :content="fileContent" />
              </div>
              <!-- JSON 渲染 -->
              <div v-else-if="isJson" class="file-content code-block">
                <pre><code>{{ formattedJson }}</code></pre>
              </div>
              <!-- 代码渲染 -->
              <div v-else-if="isCode" class="file-content code-block">
                <pre><code :class="`lang-${codeLang}`">{{ fileContent }}</code></pre>
              </div>
              <!-- 纯文本 -->
              <div v-else class="file-content plain-text">
                <pre>{{ fileContent }}</pre>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </ModalDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useWorkspaceStore } from '@/store/workspace-store'
import { getSkillFiles, getSkillFileContent } from '@/api/skill-api'
import type { Skill, SkillFile } from '@/types/memory'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomButton from '@/components/CustomButton.vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

interface TreeNode {
  name: string
  path: string
  isDirectory: boolean
  size: number
  lastModified: string
  children: TreeNode[]
  _depth: number
  _key: string
}

const props = defineProps<{
  skill: Skill | null
}>()

const emit = defineEmits<{
  close: []
}>()

const visible = defineModel<boolean>('visible', { default: false })
const store = useWorkspaceStore()

const loadingFiles = ref(false)
const selectedFilePath = ref('')
const fileContent = ref('')
const loadingContent = ref(false)
const expandedDirs = ref<Set<string>>(new Set())
const treeRoot = ref<TreeNode | null>(null)
const fileIdMap = ref<Map<string, string>>(new Map())

const skillTypeClass = computed(() => {
  switch (props.skill?.skillType) {
    case 'SYNCED': return 'badge-success'
    case 'UPLOADED': return 'badge-info'
    default: return 'badge-default'
  }
})

const skillTypeLabel = computed(() => {
  switch (props.skill?.skillType) {
    case 'SYNCED': return '同步'
    case 'UPLOADED': return '上传'
    default: return props.skill?.skillType || '未知'
  }
})

const sourceLabel = computed(() => {
  switch (props.skill?.source) {
    case 'LOCAL': return '本地'
    case 'URL': return 'URL'
    case 'UPLOAD': return '上传'
    default: return props.skill?.source || '未知'
  }
})

const CODE_EXTS = new Set([
  'js', 'ts', 'jsx', 'tsx', 'vue', 'svelte',
  'py', 'rb', 'go', 'rs', 'java', 'kt', 'scala',
  'c', 'cpp', 'h', 'hpp', 'cs',
  'sh', 'bash', 'zsh', 'fish', 'ps1', 'bat', 'cmd',
  'sql', 'graphql', 'gql',
  'yaml', 'yml', 'toml', 'ini', 'conf', 'cfg',
  'xml', 'html', 'htm', 'css', 'scss', 'less', 'sass',
  'gradle', 'properties', 'env', 'dockerfile',
  'makefile', 'cmake', 'gitignore', 'editorconfig',
])

const LANG_MAP: Record<string, string> = {
  js: 'javascript', ts: 'typescript', jsx: 'javascript', tsx: 'typescript',
  vue: 'html', svelte: 'html',
  py: 'python', rb: 'ruby', kt: 'kotlin', rs: 'rust',
  sh: 'bash', bash: 'bash', zsh: 'bash', fish: 'bash',
  ps1: 'powershell', bat: 'batch', cmd: 'batch',
  yml: 'yaml', conf: 'ini', cfg: 'ini', properties: 'properties',
  htm: 'html', scss: 'css', less: 'css', sass: 'css',
  dockerfile: 'dockerfile', makefile: 'makefile',
}

function getExt(fileName: string): string {
  const dot = fileName.lastIndexOf('.')
  return dot > 0 ? fileName.slice(dot + 1).toLowerCase() : ''
}

const selectedFileExt = computed(() => selectedFilePath.value ? getExt(selectedFilePath.value.split('/').pop() || '') : '')

const isMarkdown = computed(() => selectedFileExt.value === 'md' || selectedFileExt.value === 'mdx')
const isJson = computed(() => selectedFileExt.value === 'json')
const isCode = computed(() => CODE_EXTS.has(selectedFileExt.value))
const codeLang = computed(() => LANG_MAP[selectedFileExt.value] || selectedFileExt.value)

const formattedJson = computed(() => {
  if (!fileContent.value) return ''
  try {
    return JSON.stringify(JSON.parse(fileContent.value), null, 2)
  } catch {
    return fileContent.value
  }
})

const flatTree = computed(() => {
  if (!treeRoot.value) return []
  const result: TreeNode[] = []
  function walk(node: TreeNode, depth: number) {
    if (!expandedDirs.value.has(node.path) && depth > 0) return
    if (node.children) {
      const sorted = [...node.children].sort((a, b) => {
        if (a.isDirectory !== b.isDirectory) return a.isDirectory ? -1 : 1
        return a.name.localeCompare(b.name)
      })
      sorted.forEach(child => {
        result.push({ ...child, _depth: depth, _key: `${depth}-${child.path}` })
        if (child.isDirectory) walk(child, depth + 1)
      })
    }
  }
  walk(treeRoot.value, 0)
  return result
})

watch(visible, async (isVisible) => {
  if (isVisible && props.skill) {
    await refreshFiles()
  }
})

watch(() => props.skill, async (newSkill) => {
  if (newSkill && visible.value) {
    await refreshFiles()
  }
})

const selection = () => ({
  tenantId: store.tenantId,
  workspaceId: store.workspaceId
})

async function refreshFiles() {
  if (!props.skill) return
  loadingFiles.value = true
  selectedFilePath.value = ''
  fileContent.value = ''
  treeRoot.value = null
  expandedDirs.value = new Set()
  fileIdMap.value = new Map()
  try {
    const treeJson = props.skill.skillFilesTree
    if (treeJson) {
      treeRoot.value = JSON.parse(treeJson)
    }
    const skillFiles = await getSkillFiles(selection(), props.skill.id)
    const idMap = new Map<string, string>()
    skillFiles.forEach(f => idMap.set(f.filePath.replace(/\\/g, '/'), f.id))
    fileIdMap.value = idMap
    await selectDefaultFile()
  } catch (e) {
    console.error('Failed to load files', e)
  } finally {
    loadingFiles.value = false
  }
}

function toggleDir(path: string) {
  const s = new Set(expandedDirs.value)
  if (s.has(path)) {
    s.delete(path)
  } else {
    s.add(path)
  }
  expandedDirs.value = s
}

async function selectDefaultFile() {
  const skillMd = flatTree.value.find(n =>
    !n.isDirectory && (n.name === 'SKILL.md' || n.name === 'skill.md')
  )
  if (skillMd) {
    await onNodeClick(skillMd)
    return
  }
  const firstFile = flatTree.value.find(n => !n.isDirectory)
  if (firstFile) {
    await onNodeClick(firstFile)
  }
}

function onNodeClick(node: TreeNode) {
  if (node.isDirectory) {
    toggleDir(node.path)
  } else {
    loadFileContent(node)
  }
}

async function loadFileContent(node: TreeNode) {
  const fileId = fileIdMap.value.get(node.path)
  if (!fileId || !props.skill) return
  selectedFilePath.value = node.path
  loadingContent.value = true
  try {
    fileContent.value = await getSkillFileContent(selection(), props.skill.id, fileId)
  } catch (e) {
    console.error('Failed to load file content', e)
    fileContent.value = '加载失败'
  } finally {
    loadingContent.value = false
  }
}

function getFileIcon(fileName: string): string {
  const ext = getExt(fileName)
  if (ext === 'md' || ext === 'mdx') return '📑'
  if (ext === 'json') return '📋'
  if (ext === 'yaml' || ext === 'yml') return '📝'
  if (ext === 'png' || ext === 'jpg' || ext === 'jpeg' || ext === 'gif' || ext === 'svg') return '🖼️'
  if (CODE_EXTS.has(ext)) return '💻'
  return '📄'
}

function formatSize(bytes: number): string {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(1)} ${units[unitIndex]}`
}

function formatDate(date: string): string {
  return new Date(date).toLocaleString()
}
</script>

<style scoped>
.skill-detail {
  height: 75vh;
  display: flex;
  flex-direction: column;
}

.detail-header {
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-item label {
  font-size: 0.7rem;
  color: var(--color-text-muted);
  text-transform: uppercase;
}

.info-item span {
  font-size: 0.85rem;
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

.detail-body {
  display: flex;
  gap: 1rem;
  flex: 1;
  min-height: 0;
}

.file-tree-panel {
  width: 35%;
  min-width: 200px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.file-content-panel {
  flex: 1;
  min-width: 0;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-hover);
  flex-shrink: 0;
}

.panel-header h3 {
  margin: 0;
  font-size: 0.85rem;
  font-weight: 600;
}

.file-tree {
  overflow-y: auto;
  flex: 1;
  padding: 0.25rem 0;
}

.tree-item {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 0.3rem 0.5rem;
  cursor: pointer;
  transition: background 0.15s;
  font-size: 0.8rem;
  user-select: none;
}

.tree-item:hover {
  background: var(--hover-bg);
}

.tree-item.active {
  background: rgba(59, 130, 246, 0.1);
  color: var(--color-primary);
}

.tree-item.is-directory {
  font-weight: 500;
}

.tree-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  transition: transform 0.15s;
  color: var(--color-text-muted);
}

.tree-arrow.expanded {
  transform: rotate(90deg);
}

.tree-arrow-placeholder {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.tree-icon {
  flex-shrink: 0;
  margin-right: 0.375rem;
}

.tree-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-size {
  font-size: 0.7rem;
  color: var(--color-text-muted);
  flex-shrink: 0;
  margin-left: 0.5rem;
}

.file-content-scroll {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.file-content {
  padding: 1rem;
  font-size: 0.8rem;
  line-height: 1.6;
  min-height: 100%;
  box-sizing: border-box;
}

.file-content.markdown-body {
  background: var(--bg-input);
}

.file-content.code-block {
  background: var(--bg-codeblock);
}

.file-content.code-block pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.file-content.code-block code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.8rem;
  color: var(--color-text);
}

.file-content.plain-text {
  background: var(--bg-input);
}

.file-content.plain-text pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.content-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--color-text-muted);
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 1.5rem;
  color: var(--color-text-muted);
}
</style>
