<template>
  <div class="floating-button-container">
    <!-- 主按钮 -->
    <button 
      class="floating-main-btn"
      @click="goToSettings"
      @mouseenter="showTooltip = true"
      @mouseleave="showTooltip = false"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="3"/>
        <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
      </svg>
    </button>

    <!-- 提示文字 -->
    <transition name="tooltip">
      <div v-if="showTooltip" class="tooltip">
        设置
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const showTooltip = ref(false)

function goToSettings() {
  router.push('/agenthub/settings')
}
</script>

<style scoped>
.floating-button-container {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 1000;
}

.floating-main-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #264266, #3a8ad6);
  border: none;
  color: white;
  cursor: pointer;
  box-shadow: 
    0 8px 20px rgba(58, 138, 214, 0.4),
    0 0 0 0 rgba(58, 138, 214, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-main-btn svg {
  width: 24px;
  height: 24px;
  transition: transform 0.3s ease;
}

.floating-main-btn:hover {
  transform: scale(1.1) rotate(90deg);
  box-shadow: 
    0 12px 28px rgba(58, 138, 214, 0.5),
    0 0 0 8px rgba(58, 138, 214, 0.1);
}

.floating-main-btn:active {
  transform: scale(0.95);
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
  font-size: 0.85rem;
  font-weight: 500;
  white-space: nowrap;
  pointer-events: none;
  backdrop-filter: blur(8px);
}

/* Transitions */
.tooltip-enter-active,
.tooltip-leave-active {
  transition: all 0.2s ease;
}

.tooltip-enter-from,
.tooltip-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

/* Responsive */
@media (max-width: 768px) {
  .floating-button-container {
    bottom: 20px;
    right: 20px;
  }
  
  .floating-main-btn {
    width: 48px;
    height: 48px;
  }
  
  .floating-main-btn svg {
    width: 20px;
    height: 20px;
  }
}
</style>
