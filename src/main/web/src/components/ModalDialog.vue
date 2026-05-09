<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click="handleOverlayClick">
        <div class="modal-container" :class="sizeClass" @click.stop>
          <div class="modal-header">
            <h3>{{ title }}</h3>
            <button v-if="showClose" class="modal-close" type="button" @click="handleClose">×</button>
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
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-container {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.4);
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  animation: modalSlideIn 0.3s ease;
}

.modal-small { width: 90%; max-width: 400px; }
.modal-medium { width: 90%; max-width: 600px; }
.modal-large { width: 90%; max-width: 800px; }
.modal-xlarge { width: 90%; max-width: 1000px; }

@keyframes modalSlideIn {
  from {
    transform: translateY(-30px) scale(0.95);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(38, 66, 102, 0.1);
  flex-shrink: 0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #27415d;
}

.modal-close {
  background: rgba(0, 0, 0, 0.05);
  border: none;
  font-size: 24px;
  color: #666;
  cursor: pointer;
  padding: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s;
}

.modal-close:hover {
  background: rgba(0, 0, 0, 0.1);
  transform: scale(1.1);
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
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.25);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid rgba(38, 66, 102, 0.1);
  flex-shrink: 0;
}

.btn-primary,
.btn-secondary {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background: white;
  color: #27415d;
  border: 1px solid rgba(38, 66, 102, 0.2);
}

.btn-secondary:hover {
  background: rgba(58, 138, 214, 0.05);
  border-color: rgba(58, 138, 214, 0.4);
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
  animation: modalSlideIn 0.3s ease;
}

.modal-leave-active .modal-container {
  animation: modalSlideOut 0.3s ease;
}

@keyframes modalSlideOut {
  from {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
  to {
    transform: translateY(-30px) scale(0.95);
    opacity: 0;
  }
}
</style>
