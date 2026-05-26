<template>
  <section class="workflow-editor-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <p>加载工作流中...</p>
    </div>

    <!-- 编辑器 -->
    <template v-else>
      <!-- 空状态：工作流不存在或加载失败 -->
      <div v-if="!workflow && !creating" class="empty-state">
        <p>工作流不存在或已被删除</p>
        <button class="btn btn-primary" @click="goBack">返回列表</button>
      </div>
      <!-- 正常显示编辑器 -->
      <WorkflowEditor
        v-else
        :workflow-id="workflowId"
        :workflow-name="workflow?.name"
        :initial-graph="initialGraph"
        :read-only="readOnly"
        @save="handleSave"
      />
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useWorkflowStore } from '@/stores/workflow-store'
import { useWorkspaceStore } from '@/store/workspace-store'
import { 
  getWorkflow, 
  updateWorkflow, 
  createWorkflow,
  listWorkflows,
} from '@/api/workflow-api'
import type { Workflow, WorkflowGraph } from '@/types/workflow'
import WorkflowEditor from '@/components/WorkflowEditor.vue'

const route = useRoute()
const router = useRouter()
const workflowStore = useWorkflowStore()
const workspaceStore = useWorkspaceStore()

const loading = ref(true)
const creating = ref(false)
const workflow = ref<Workflow | null>(null)
const initialGraph = ref<WorkflowGraph | null>(null)
const readOnly = ref(false)

const workflowId = computed(() => route.params.id as string)
const selection = computed(() => ({
  tenantId: workspaceStore.tenantId,
  workspaceId: workspaceStore.workspaceId,
}))

onMounted(async () => {
  if (!workflowId.value || workflowId.value === 'new') {
    await initNewWorkflow()
  } else {
    await loadWorkflow()
  }
  loading.value = false
})

async function initNewWorkflow() {
  creating.value = true
  workflow.value = null
  initialGraph.value = { nodes: [], edges: [] }

  // 重置store
  workflowStore.reset()
  workflowStore.setWorkflowInfo('new', '新建工作流', '')
}

async function loadWorkflow() {
  try {
    const wf = await getWorkflow(selection.value, workflowId.value)
    workflow.value = wf
    readOnly.value = wf.status === 'PUBLISHED'

    // 解析graph定义
    let graph: WorkflowGraph = { nodes: [], edges: [] }
    if (wf.graphDefinition) {
      try {
        const parsed = JSON.parse(wf.graphDefinition)
        graph = {
          nodes: parsed.nodes || [],
          edges: parsed.edges || []
        }
      } catch {
        graph = { nodes: [], edges: [] }
      }
    }

    initialGraph.value = graph

    // 设置store
    workflowStore.reset()
    workflowStore.setWorkflowInfo(wf.id, wf.name, wf.description)
    workflowStore.setGraph(graph)
  } catch (err) {
    console.error('加载工作流失败:', err)
    workflow.value = null
  }
}

async function handleSave(graphData: WorkflowGraph) {
  if (creating.value) {
    // 新建工作流
    try {
      const newWf = await createWorkflow(
        selection.value,
        `wf_${Date.now()}`,
        workflowStore.workflowName || '未命名工作流',
        workflowStore.workflowDesc,
        JSON.stringify(graphData)
      )
      workflow.value = newWf
      creating.value = false
      workflowStore.setWorkflowInfo(newWf.id, newWf.name, newWf.description)
      
      // 更新URL
      router.replace(`/agenthub/workflows/${newWf.id}`)
    } catch (err) {
      console.error('创建工作流失败:', err)
    }
  } else if (workflow.value) {
    // 更新已有工作流
    try {
      const updated = await updateWorkflow(
        selection.value,
        workflow.value.id,
        workflowStore.workflowName || workflow.value.name,
        workflowStore.workflowDesc || workflow.value.description,
        JSON.stringify(graphData)
      )
      workflow.value = updated
      workflowStore.markAsSaved()
    } catch (err) {
      console.error('保存工作流失败:', err)
    }
  }
}

function goBack() {
  router.push('/agenthub/workflows')
}
</script>

<style scoped>
.workflow-editor-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-stripe);
}

.loading-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top: 3px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  color: #999;
}

.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-primary { background: var(--color-primary); color: var(--color-text-inverse); }
</style>
