<template>
  <section class="strategy-management glass-float">
    <div class="page-header">
      <div>
      <h2>策略配置</h2>
      <p class="muted">管理检索策略、工具策略、模型策略、护栏策略和权限策略</p>
      </div>
    </div>

    <div class="tabs">
      <button :class="{ active: activeTab === 'retrieval' }" @click="activeTab = 'retrieval'">检索策略</button>
      <button :class="{ active: activeTab === 'tool' }" @click="activeTab = 'tool'">工具策略</button>
      <button :class="{ active: activeTab === 'model' }" @click="activeTab = 'model'">模型策略</button>
      <button :class="{ active: activeTab === 'guardrail' }" @click="activeTab = 'guardrail'">护栏策略</button>
      <button :class="{ active: activeTab === 'permission' }" @click="activeTab = 'permission'">权限策略</button>
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
    <div v-else-if="activeTab === 'permission'" class="tab-content">
      <PermissionStrategyPanel :key="componentKey" />
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
import PermissionStrategyPanel from '@/components/strategy/PermissionStrategyPanel.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'

const store = useWorkspaceStore()
type StrategyTab = 'retrieval' | 'tool' | 'model' | 'guardrail' | 'permission'
const activeTab = ref<StrategyTab>('retrieval')

// 使用workspaceId作为key，当workspaceId变化时强制重新渲染子组件
const componentKey = computed(() => store.workspaceId)

// 监听全局新增事件，根据当前标签页触发对应的子组件新增
import { onMounted } from 'vue'

onMounted(() => {
  window.addEventListener('global-add', () => {
    // 根据当前标签页触发对应的子组件新增事件
    window.dispatchEvent(new CustomEvent(`strategy-${activeTab.value}-add`))
  })
})
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
  border: 1px solid var(--color-border);
  background: var(--bg-card-solid);
  color: var(--color-text);
  cursor: pointer;
}

.tabs button.active {
  background: var(--color-primary); color: var(--color-text-inverse);
  border-color: var(--color-primary);
}

.tab-content {
  background: var(--bg-card-solid);
  padding: 1.5rem;
  border-radius: 4px;
}
</style>
