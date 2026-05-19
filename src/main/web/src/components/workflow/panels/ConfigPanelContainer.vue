<template>
  <div class="config-panel-container">
    <div v-if="!selectedNode" class="empty-state">
      <div class="empty-icon">📋</div>
      <p>请选择一个节点进行配置</p>
    </div>

    <template v-else>
      <LLMPanel
        v-if="selectedNode.type === 'llm'"
        :node-data="selectedNode"
        @save="handleSave"
        @cancel="handleCancel"
      />

      <ApiPanel
        v-else-if="selectedNode.type === 'api'"
        :node-data="selectedNode"
        @save="handleSave"
        @cancel="handleCancel"
      />

      <ConditionPanel
        v-else-if="selectedNode.type === 'condition'"
        :node-data="selectedNode"
        @save="handleSave"
        @cancel="handleCancel"
      />

      <LoopPanel
        v-else-if="selectedNode.type === 'loop'"
        :node-data="selectedNode"
        @save="handleSave"
        @cancel="handleCancel"
      />

      <ParallelPanel
        v-else-if="selectedNode.type === 'parallel'"
        :node-data="selectedNode"
        @save="handleSave"
        @cancel="handleCancel"
      />

      <ToolPanel
        v-else-if="selectedNode.type === 'tool'"
        :node-data="selectedNode"
        @save="handleSave"
        @cancel="handleCancel"
      />

      <VariablePanel
        v-else-if="selectedNode.type === 'variable'"
        :node-data="selectedNode"
        @save="handleSave"
        @cancel="handleCancel"
      />

      <CodePanel
        v-else-if="selectedNode.type === 'code'"
        :node-data="selectedNode"
        @save="handleSave"
        @cancel="handleCancel"
        @test="handleTest"
      />

      <RetrievalPanel
        v-else-if="selectedNode.type === 'retrieval'"
        :node-data="selectedNode"
        @save="handleSave"
        @cancel="handleCancel"
        @test="handleTest"
      />

      <DefaultPanel
        v-else
        :node-data="selectedNode"
        @save="handleSave"
        @cancel="handleCancel"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useWorkflowStore } from '@/stores/workflow-store'
import LLMPanel from './LLMPanel.vue'
import ApiPanel from './ApiPanel.vue'
import ConditionPanel from './ConditionPanel.vue'
import LoopPanel from './LoopPanel.vue'
import ParallelPanel from './ParallelPanel.vue'
import ToolPanel from './ToolPanel.vue'
import VariablePanel from './VariablePanel.vue'
import CodePanel from './CodePanel.vue'
import RetrievalPanel from './RetrievalPanel.vue'
import DefaultPanel from './DefaultPanel.vue'
const workflowStore = useWorkflowStore()

const selectedNode = computed(() => workflowStore.selectedNode)
const availableVariables = computed(() => {
  // 从workflowStore获取可用变量列表
  return workflowStore.availableVariables.map(v => v.name)
})

const emit = defineEmits<{
  'save': [nodeId: string, config: any]
  'cancel': []
  'test': [nodeId: string, config: any]
}>()

function handleSave(config: any) {
  if (selectedNode.value) {
    workflowStore.updateNode(selectedNode.value.id, {
      data: {
        ...selectedNode.value.data,
        config
      }
    })
    emit('save', selectedNode.value.id, config)
  }
}

function handleCancel() {
  workflowStore.selectNode(null)
  emit('cancel')
}

function handleTest(config: any) {
  if (selectedNode.value) {
    emit('test', selectedNode.value.id, config)
  }
}
</script>

<style scoped>
.config-panel-container {
  width: 100%;
  height: 100%;
  background: #f8f9fa;
  overflow: hidden;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 14px;
}
</style>
