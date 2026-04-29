<template>
  <section class="strategy-management">
    <div class="page-header">
      <h2>策略配置管理</h2>
      <p class="muted">管理检索策略、工具策略、模型策略和护栏策略</p>
    </div>

    <div class="tabs">
      <button :class="{ active: activeTab === 'retrieval' }" @click="activeTab = 'retrieval'">检索策略</button>
      <button :class="{ active: activeTab === 'tool' }" @click="activeTab = 'tool'">工具策略</button>
      <button :class="{ active: activeTab === 'model' }" @click="activeTab = 'model'">模型策略</button>
      <button :class="{ active: activeTab === 'guardrail' }" @click="activeTab = 'guardrail'">护栏策略</button>
    </div>

    <div v-if="activeTab === 'retrieval'" class="tab-content">
      <RetrievalStrategyPanel :key="componentKey" />
    </div>
    <div v-else-if="activeTab === 'tool'" class="tab-content">
      <ToolStrategyPanel :key="componentKey" />
    </div>
    <div v-else-if="activeTab === 'model'" class="tab-content">
      <ModelStrategyPanel :key="componentKey" />
    </div>
    <div v-else-if="activeTab === 'guardrail'" class="tab-content">
      <GuardrailStrategyPanel :key="componentKey" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useWorkspaceStore } from '@/store/workspace-store'
import RetrievalStrategyPanel from '@/components/strategy/RetrievalStrategyPanel.vue'
import ToolStrategyPanel from '@/components/strategy/ToolStrategyPanel.vue'
import ModelStrategyPanel from '@/components/strategy/ModelStrategyPanel.vue'
import GuardrailStrategyPanel from '@/components/strategy/GuardrailStrategyPanel.vue'

const store = useWorkspaceStore()
const activeTab = ref<'retrieval' | 'tool' | 'model' | 'guardrail'>('retrieval')

// 使用workspaceId作为key，当workspaceId变化时强制重新渲染子组件
const componentKey = computed(() => store.workspaceId)
</script>

<style scoped>
.strategy-management {
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.tabs button {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
}

.tabs button.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.tab-content {
  background: white;
  padding: 1.5rem;
  border-radius: 4px;
}
</style>
