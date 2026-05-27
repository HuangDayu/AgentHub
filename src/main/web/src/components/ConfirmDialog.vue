<template>
  <Teleport to="body">
    <Transition name="confirm">
      <div v-if="state.visible" class="confirm-overlay" @click="handleOverlayClick">
        <div class="confirm-dialog" @click.stop>
          <div class="confirm-header">
            <h3>{{ state.title }}</h3>
            <button class="confirm-close" @click="cancel">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>
          <div class="confirm-body">
            <div class="confirm-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
            </div>
            <p class="confirm-message">{{ state.message }}</p>
          </div>
          <div class="confirm-footer">
            <button class="btn-cancel" @click="cancel">取消</button>
            <button class="btn-confirm" @click="ok">确定</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { confirmState, confirmOk, confirmCancel } from '@/utils/confirm'

const state = confirmState()

function ok() {
  confirmOk()
}

function cancel() {
  confirmCancel()
}

function handleOverlayClick() {
  cancel()
}
</script>

<style scoped>
.confirm-overlay {
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
  z-index: 99999;
}

.confirm-dialog {
  width: 90%;
  max-width: 400px;
  background: var(--bg-card-solid, #ffffff);
  border-radius: 20px;
  border: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
  box-shadow: var(--shadow-xl, 0 20px 56px rgba(26, 30, 43, 0.12));
  overflow: hidden;
}

.confirm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 0;
}

.confirm-header h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 600;
  font-family: var(--font-heading, inherit);
  color: var(--color-heading, #0f1729);
}

.confirm-close {
  width: 32px;
  height: 32px;
  padding: 6px;
  background: var(--bg-hover, rgba(0,0,0,0.05));
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: var(--color-text-light, #8a94a8);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-close:hover {
  background: var(--bg-active, rgba(0,0,0,0.1));
  color: var(--color-text-muted, #5d667a);
}

.confirm-close svg {
  width: 100%;
  height: 100%;
}

.confirm-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.confirm-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-warning-subtle, rgba(212, 148, 47, 0.1));
  border-radius: 50%;
  color: var(--color-warning, #d4942f);
  margin-bottom: 16px;
}

.confirm-icon svg {
  width: 24px;
  height: 24px;
}

.confirm-message {
  margin: 0;
  font-size: 0.95rem;
  color: var(--color-text, #1a2633);
  line-height: 1.6;
  word-break: break-word;
}

.confirm-footer {
  display: flex;
  gap: 12px;
  padding: 0 24px 20px;
  justify-content: flex-end;
}

.btn-cancel,
.btn-confirm {
  padding: 10px 24px;
  border-radius: 10px;
  font-family: var(--font-body, inherit);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  border: none;
}

.btn-cancel {
  background: var(--bg-card-solid, #ffffff);
  color: var(--color-primary-dark, #1e3a6f);
  border: 1px solid var(--color-border-strong, rgba(26, 30, 43, 0.12));
}

.btn-cancel:hover {
  background: var(--bg-hover, rgba(58, 123, 213, 0.04));
  border-color: var(--color-primary, #3a7bd5);
}

.btn-confirm {
  background: linear-gradient(135deg, var(--color-error-dark, #991b1b), var(--color-error, #d44c3a));
  color: var(--color-text-inverse, #f8faff);
}

.btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(212, 76, 58, 0.3);
}

/* Transition */
.confirm-enter-active,
.confirm-leave-active {
  transition: opacity 0.25s ease;
}

.confirm-enter-from,
.confirm-leave-to {
  opacity: 0;
}

.confirm-enter-active .confirm-dialog {
  animation: confirmSlideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.confirm-leave-active .confirm-dialog {
  animation: confirmSlideOut 0.2s ease;
}

@keyframes confirmSlideIn {
  from { transform: translateY(-20px) scale(0.95); opacity: 0; }
  to { transform: translateY(0) scale(1); opacity: 1; }
}

@keyframes confirmSlideOut {
  from { transform: translateY(0) scale(1); opacity: 1; }
  to { transform: translateY(-20px) scale(0.95); opacity: 0; }
}
</style>
