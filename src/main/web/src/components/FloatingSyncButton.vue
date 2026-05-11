<template>
  <div class="floating-sync-button-container" :style="{ bottom: `${bottom}px` }">
    <button class="floating-sync-btn" @click="handleSync">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M23 4v6h-6"/>
        <path d="M1 20v-6h6"/>
        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  bottom: number
}>()

const emit = defineEmits<{
  sync: []
}>()

const handleSync = () => {
  emit('sync')
}
</script>

<style scoped>
.floating-sync-button-container {
  position: fixed;
  right: 24px;
  z-index: 1000;
}

.floating-sync-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6c757d, #7a8288);
  border: none;
  color: white;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(108, 117, 125, 0.4);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-sync-btn svg {
  width: 20px;
  height: 20px;
}

.floating-sync-btn:hover {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 8px 24px rgba(108, 117, 125, 0.5);
}

.floating-sync-btn:active {
  transform: translateY(-2px) scale(1.05);
}
</style>
