<template>
  <!-- 右下角 Toast 通知 -->
  <Teleport to="body">
    <Transition name="toast-slide">
      <div v-if="store.toastMessage" class="error-toast" @click="openPanel">
        <div class="toast-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <div class="toast-body">
          <div class="toast-title">接口异常</div>
          <div class="toast-msg">{{ store.toastMessage }}</div>
        </div>
        <button class="toast-close" @click.stop="store.dismissToast()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
        </button>
      </div>
    </Transition>
  </Teleport>

  <!-- 异常历史弹窗 -->
  <ModalDialog
    v-model:visible="showPanel"
    title="接口异常记录"
    size="large"
    :show-footer="false"
    @close="handleClose"
  >
    <ErrorHistoryPanel />
  </ModalDialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useErrorHistoryStore } from '@/stores/error-history'
import ModalDialog from '@/components/ModalDialog.vue'
import ErrorHistoryPanel from '@/components/ErrorHistoryPanel.vue'

const store = useErrorHistoryStore()
const showPanel = ref(false)

let toastTimer: ReturnType<typeof setTimeout> | null = null

// 监听 API 异常事件
function handleApiError(e: Event) {
  const detail = (e as CustomEvent).detail
  if (detail) {
    store.addError(detail.status, detail.url, detail.message)
  }
}

onMounted(() => {
  window.addEventListener('api-error', handleApiError)
})

onUnmounted(() => {
  window.removeEventListener('api-error', handleApiError)
  if (toastTimer) clearTimeout(toastTimer)
})

// toast 自动消失
watch(() => store.toastMessage, (val) => {
  if (toastTimer) clearTimeout(toastTimer)
  if (val) {
    toastTimer = setTimeout(() => {
      store.dismissToast()
    }, 5000)
  }
})

function openPanel() {
  store.dismissToast()
  store.markAllRead()
  showPanel.value = true
}

function handleClose() {
  showPanel.value = false
}
</script>

<style scoped>
/* ── Toast 通知 ── */
.error-toast {
  position: fixed;
  bottom: 32px;
  right: 24px;
  z-index: 9998;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 360px;
  max-width: calc(100vw - 48px);
  padding: 16px;
  background: var(--bg-card-solid, #ffffff);
  border: 1px solid var(--color-error-subtle, rgba(212, 76, 58, 0.2));
  border-radius: 14px;
  box-shadow: 0 8px 32px rgba(212, 76, 58, 0.15);
  cursor: pointer;
  transition: all 0.3s ease;
}

.error-toast:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(212, 76, 58, 0.2);
}

.toast-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-error-subtle, rgba(212, 76, 58, 0.1));
  border-radius: 50%;
  color: var(--color-error, #d44c3a);
}

.toast-icon svg {
  width: 16px;
  height: 16px;
}

.toast-body {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-error-dark, #991b1b);
  margin-bottom: 4px;
}

.toast-msg {
  font-size: 0.8rem;
  color: var(--color-text-muted, #5d667a);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-all;
}

.toast-close {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  padding: 4px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--color-text-light, #8a94a8);
  border-radius: 6px;
  transition: all 0.2s;
}

.toast-close:hover {
  background: var(--bg-hover, rgba(0,0,0,0.05));
  color: var(--color-text-muted, #5d667a);
}

.toast-close svg {
  width: 100%;
  height: 100%;
}

/* Toast 动画 */
.toast-slide-enter-active {
  animation: toastIn 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.toast-slide-leave-active {
  animation: toastOut 0.25s ease;
}

@keyframes toastIn {
  from { transform: translateX(120%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

@keyframes toastOut {
  from { transform: translateX(0); opacity: 1; }
  to { transform: translateX(120%); opacity: 0; }
}
</style>
