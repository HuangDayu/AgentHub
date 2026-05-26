<template>
  <div class="floating-button-container" :style="{ bottom: `${bottom}px` }">
    <button
      class="floating-btn"
      @click="goHome"
      @mouseenter="showTooltip = true"
      @mouseleave="showTooltip = false"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M3 9l9-7 9 7v12a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1z"/>
        <polyline points="9 22 9 12 15 12 15 22"/>
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps<{
  bottom: number
}>()

const router = useRouter()
const showTooltip = ref(false)

const goHome = () => {
  router.push('/agenthub')
}
</script>

<style scoped>
.floating-button-container {
  position: fixed;
  right: 24px;
  z-index: 1000;
}

.floating-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  border: none; color: var(--color-text-inverse);
  cursor: pointer;
  box-shadow: var(--shadow-glow);
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
  box-shadow: 0 8px 24px color-mix(in srgb, var(--color-primary) 50%, transparent);
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
  background: rgba(26, 30, 41, 0.9); color: var(--color-text-inverse);
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
