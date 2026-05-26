<template>
  <div class="floating-add-button-container" :style="{ bottom: `${bottom}px` }">
    <button class="floating-add-btn" @click="handleAdd">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 5v14M5 12h14"/>
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  bottom: number
}>()

const emit = defineEmits<{
  add: []
}>()

const handleAdd = () => {
  emit('add')
}
</script>

<style scoped>
.floating-add-button-container {
  position: fixed;
  right: 24px;
  z-index: 1000;
}

.floating-add-btn {
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

.floating-add-btn svg {
  width: 20px;
  height: 20px;
}

.floating-add-btn:hover {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 8px 24px color-mix(in srgb, var(--color-primary) 50%, transparent);
}

.floating-add-btn:active {
  transform: translateY(-2px) scale(1.05);
}
</style>
