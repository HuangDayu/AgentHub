<template>
  <button 
    :type="nativeType"
    :disabled="disabled || loading"
    :class="[
      'custom-button',
      `button-${type}`,
      `button-${size}`,
      {
        'is-loading': loading,
        'is-fullwidth': fullwidth,
        'is-circle': circle,
        'has-icon': icon || $slots.icon
      }
    ]"
    @click="handleClick"
  >
    <span v-if="loading" class="button-loader">
      <svg class="spinner" viewBox="0 0 50 50">
        <circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="4"/>
      </svg>
    </span>
    <span v-else-if="icon || $slots.icon" class="button-icon">
      <slot name="icon">
        <component v-if="icon" :is="icon" />
      </slot>
    </span>
    <span v-if="$slots.default" class="button-text">
      <slot></slot>
    </span>
  </button>
</template>

<script setup lang="ts">
import { type Component } from 'vue'

interface Props {
  type?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'success' | 'warning' | 'info'
  size?: 'small' | 'medium' | 'large'
  nativeType?: 'button' | 'submit' | 'reset'
  disabled?: boolean
  loading?: boolean
  fullwidth?: boolean
  circle?: boolean
  icon?: Component
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'medium',
  nativeType: 'button',
  disabled: false,
  loading: false,
  fullwidth: false,
  circle: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
.custom-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: 8px;
  font-family: inherit;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  outline: none;
  position: relative;
  white-space: nowrap;
  user-select: none;
}

/* 尺寸 */
.button-small {
  padding: 6px 12px;
  font-size: 13px;
  min-height: 32px;
}

.button-medium {
  padding: 10px 18px;
  font-size: 14px;
  min-height: 40px;
}

.button-large {
  padding: 12px 24px;
  font-size: 15px;
  min-height: 48px;
}

/* 类型样式 */
.button-primary {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  color: var(--color-text-inverse);
  border-color: transparent;
}

.button-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-glow);
}

.button-primary:active:not(:disabled) {
  transform: translateY(0);
}

.button-secondary {
  background: var(--bg-card-solid);
  color: var(--color-text);
  border-color: var(--color-border-strong);
}

.button-secondary:hover:not(:disabled) {
  background: var(--color-primary-subtle);
  border-color: var(--color-primary);
}

.button-secondary:active:not(:disabled) {
  background: var(--bg-active);
}

.button-ghost {
  background: transparent;
  color: var(--color-text);
  border-color: var(--color-border-strong);
}

.button-ghost:hover:not(:disabled) {
  background: var(--color-primary-subtle);
  border-color: var(--color-primary);
}

.button-ghost:active:not(:disabled) {
  background: var(--bg-active);
}

.button-danger {
  background: var(--bg-card-solid);
  color: var(--color-error);
  border-color: var(--color-error);
}

.button-danger:hover:not(:disabled) {
  background: var(--color-primary-subtle);
  border-color: var(--color-error);
}

.button-danger:active:not(:disabled) {
  background: var(--bg-active);
}

.button-success {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-success));
  color: var(--color-text-inverse);
  border-color: transparent;
}

.button-success:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(45, 157, 120, 0.3);
}

.button-warning {
  background: linear-gradient(135deg, var(--color-warning), var(--color-warning));
  color: var(--color-text-inverse);
  border-color: transparent;
}

.button-warning:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(232, 168, 56, 0.3);
}

.button-info {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-info));
  color: var(--color-text-inverse);
  border-color: transparent;
}

.button-info:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(91, 141, 239, 0.3);
}

/* 状态 */
.custom-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

.custom-button.is-loading {
  cursor: wait;
}

/* 全宽 */
.button.is-fullwidth {
  width: 100%;
}

/* 圆形 */
.button.is-circle {
  border-radius: 50%;
  padding: 10px;
}

.button-small.is-circle {
  padding: 6px;
}

.button-large.is-circle {
  padding: 12px;
}

/* 图标 */
.button-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.has-icon:not(.is-circle) .button-icon {
  margin-right: -4px;
}

/* 加载动画 */
.button-loader {
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  animation: rotate 1s linear infinite;
  width: 18px;
  height: 18px;
}

.spinner .path {
  stroke: currentColor;
  stroke-linecap: round;
  animation: dash 1.5s ease-in-out infinite;
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}

.button-text {
  display: inline-flex;
  align-items: center;
}
</style>
