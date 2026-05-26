<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click="handleOverlayClick">
        <div class="modal-container" :class="sizeClass" @click.stop>
          <div class="modal-header">
            <h3>{{ title }}</h3>
            <button v-if="showClose" class="modal-close" type="button" @click="handleClose">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <slot></slot>
          </div>
          <div v-if="showFooter" class="modal-footer">
            <slot name="footer">
              <button class="btn-secondary" type="button" @click="handleClose">取消</button>
              <button class="btn-primary" type="button" @click="handleConfirm" :disabled="confirmDisabled">
                {{ confirmText }}
              </button>
            </slot>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  visible: boolean
  title: string
  showClose?: boolean
  showFooter?: boolean
  confirmText?: string
  confirmDisabled?: boolean
  closeOnOverlay?: boolean
  size?: 'small' | 'medium' | 'large' | 'xlarge'
}

const props = withDefaults(defineProps<Props>(), {
  showClose: true,
  showFooter: true,
  confirmText: '确定',
  confirmDisabled: false,
  closeOnOverlay: true,
  size: 'medium'
})

const emit = defineEmits<{
  close: []
  confirm: []
}>()

const sizeClass = computed(() => `modal-${props.size}`)

const handleClose = () => {
  emit('close')
}

const handleConfirm = () => {
  emit('confirm')
}

const handleOverlayClick = () => {
  if (props.closeOnOverlay) {
    handleClose()
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--bg-overlay, rgba(0, 0, 0, 0.5));
  backdrop-filter: blur(8px) saturate(180%);
  -webkit-backdrop-filter: blur(8px) saturate(180%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-container {
  background: var(--bg-card-solid, #ffffff);
  border-radius: 20px;
  border: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
  box-shadow: var(--shadow-xl, 0 20px 56px rgba(26, 30, 43, 0.12));
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-small { width: 90%; max-width: 400px; }
.modal-medium { width: 90%; max-width: 600px; }
.modal-large { width: 90%; max-width: 800px; }
.modal-xlarge { width: 90%; max-width: 1000px; }

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
  flex-shrink: 0;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  font-family: var(--font-heading, inherit);
  color: var(--color-heading, #0f1729);
}

.modal-close {
  background: var(--bg-hover, rgba(0, 0, 0, 0.05));
  border: none;
  width: 36px;
  height: 36px;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  color: var(--color-text-light, #8a94a8);
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: var(--bg-active, rgba(0, 0, 0, 0.1));
  color: var(--color-text-muted, #5d667a);
  transform: scale(1.1);
}

.modal-close svg {
  width: 100%;
  height: 100%;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  min-height: 0;
}

.modal-body::-webkit-scrollbar {
  width: 8px;
}

.modal-body::-webkit-scrollbar-track {
  background: var(--bg-stripe, rgba(0, 0, 0, 0.05));
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb {
  background: var(--color-border-strong, rgba(0, 0, 0, 0.15));
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-light, rgba(0, 0, 0, 0.25));
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
  flex-shrink: 0;
}

.btn-primary,
.btn-secondary {
  padding: 10px 20px;
  border-radius: 10px;
  font-family: var(--font-body, inherit);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-primary-dark, #1e3a6f), var(--color-primary, #3a7bd5));
  color: var(--color-text-inverse, #f8faff);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow, 0 4px 20px rgba(58, 123, 213, 0.25));
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background: var(--bg-card-solid, #ffffff);
  color: var(--color-primary-dark, #1e3a6f);
  border: 1px solid var(--color-border-strong, rgba(26, 30, 43, 0.12));
}

.btn-secondary:hover {
  background: var(--bg-hover, rgba(58, 123, 213, 0.04));
  border-color: var(--color-primary, #3a7bd5);
}

/* Transition */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-container {
  animation: modalSlideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-leave-active .modal-container {
  animation: modalSlideOut 0.25s ease;
}

@keyframes modalSlideIn {
  from { transform: translateY(-20px) scale(0.96); opacity: 0; }
  to { transform: translateY(0) scale(1); opacity: 1; }
}

@keyframes modalSlideOut {
  from { transform: translateY(0) scale(1); opacity: 1; }
  to { transform: translateY(-20px) scale(0.96); opacity: 0; }
}
</style>
