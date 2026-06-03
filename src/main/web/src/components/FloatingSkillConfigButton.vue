<template>
  <div class="floating-config-button-container" :style="{ bottom: `${bottom}px` }">
    <button class="floating-config-btn" @click="showDialog = true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M19.439 7.85c-.049.322.059.648.289.878l1.568 1.568c.47.47.706 1.087.706 1.704s-.235 1.233-.706 1.704l-1.611 1.611a.98.98 0 0 1-.837.276c-.47-.07-.802-.48-.968-.925a2.501 2.501 0 1 0-3.214 3.214c.446.166.855.497.925.968a.979.979 0 0 1-.276.837l-1.611 1.611a2.404 2.404 0 0 1-1.704.706 2.402 2.402 0 0 1-1.704-.706l-1.568-1.568a1.026 1.026 0 0 0-.877-.29c-.493.074-.84.504-1.02.968a2.5 2.5 0 1 1-3.237-3.237c.464-.18.894-.527.967-1.02a1.026 1.026 0 0 0-.289-.877l-1.568-1.568A2.402 2.402 0 0 1 1.998 12c0-.617.236-1.234.706-1.704L4.23 8.77c.24-.24.581-.353.917-.303.515.077.877.528 1.073 1.01a2.5 2.5 0 1 0 3.259-3.259c-.482-.196-.933-.558-1.01-1.073-.05-.336.062-.676.303-.917l1.525-1.525A2.402 2.402 0 0 1 12 1.998c.617 0 1.234.236 1.704.706l1.568 1.568c.23.23.556.338.877.29.493-.074.84-.504 1.02-.968a2.5 2.5 0 1 1 3.237 3.237c-.464.18-.894.527-.967 1.02Z"/>
      </svg>
    </button>
  </div>

  <SkillConfigModal v-model:visible="showDialog" @close="showDialog = false" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SkillConfigModal from '@/views/agenthub/SkillConfigModal.vue'

const props = defineProps<{
  bottom: number
}>()

const showDialog = ref(false)
</script>

<style scoped>
.floating-config-button-container {
  position: fixed;
  right: 24px;
  z-index: 1000;
}

.floating-config-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  border: none;
  color: var(--color-text-inverse);
  cursor: pointer;
  box-shadow: var(--shadow-glow);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-config-btn svg {
  width: 20px;
  height: 20px;
}

.floating-config-btn:hover {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 8px 24px color-mix(in srgb, var(--color-primary) 50%, transparent);
}

.floating-config-btn:active {
  transform: translateY(-2px) scale(1.05);
}
</style>
