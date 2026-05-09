<template>
  <div class="floating-button-container">
    <button 
      class="floating-btn"
      @click="showDialog = true"
      @mouseenter="showTooltip = true"
      @mouseleave="showTooltip = false"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <circle cx="12" cy="12" r="4"/>
        <line x1="21.17" y1="8" x2="12" y2="8"/>
        <line x1="3.95" y1="6.06" x2="8.54" y2="14"/>
        <line x1="10.88" y1="21.94" x2="15.46" y2="14"/>
      </svg>
    </button>
<!--    <transition name="tooltip">-->
<!--      <div v-if="showTooltip" class="tooltip">视觉效果</div>-->
<!--    </transition>-->
  </div>

  <ModalDialog
    v-model:visible="showDialog"
    title="视觉效果设置"
    size="small"
    :show-footer="false"
    @close="showDialog = false"
  >
    <EffectToggle />
  </ModalDialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import EffectToggle from '@/components/EffectToggle.vue'

const showDialog = ref(false)
const showTooltip = ref(false)
</script>

<style scoped>
.floating-button-container {
  position: fixed;
  bottom: 84px;
  right: 24px;
  z-index: 1000;
}

.floating-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #264266, #3a8ad6);
  border: none;
  color: white;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(58, 138, 214, 0.4);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-btn svg {
  width: 20px;
  height: 20px;
}

.floating-btn:hover {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 8px 24px rgba(58, 138, 214, 0.5);
}

.floating-btn:active {
  transform: translateY(-2px) scale(1.05);
}

.tooltip {
  position: absolute;
  bottom: 100%;
  right: 0;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: rgba(26, 30, 41, 0.9);
  color: white;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  pointer-events: none;
}

.tooltip-enter-active,
.tooltip-leave-active {
  transition: all 0.2s ease;
}

.tooltip-enter-from,
.tooltip-leave-to {
  opacity: 0;
  transform: translateY(4px);
}
</style>
