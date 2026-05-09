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
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: white;
  border-color: transparent;
}

.button-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #2d4d7a, #4a9ae8);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(58, 138, 214, 0.3);
}

.button-primary:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(58, 138, 214, 0.2);
}

.button-secondary {
  background: white;
  color: #27415d;
  border-color: rgba(38, 66, 102, 0.2);
}

.button-secondary:hover:not(:disabled) {
  background: rgba(58, 138, 214, 0.05);
  border-color: rgba(58, 138, 214, 0.4);
}

.button-secondary:active:not(:disabled) {
  background: rgba(58, 138, 214, 0.1);
}

.button-ghost {
  background: transparent;
  color: #27415d;
  border-color: rgba(38, 66, 102, 0.2);
}

.button-ghost:hover:not(:disabled) {
  background: rgba(38, 66, 102, 0.05);
  border-color: rgba(38, 66, 102, 0.3);
}

.button-ghost:active:not(:disabled) {
  background: rgba(38, 66, 102, 0.1);
}

.button-danger {
  background: white;
  color: #c94a35;
  border-color: rgba(201, 74, 53, 0.3);
}

.button-danger:hover:not(:disabled) {
  background: rgba(201, 74, 53, 0.05);
  border-color: rgba(201, 74, 53, 0.5);
}

.button-danger:active:not(:disabled) {
  background: rgba(201, 74, 53, 0.1);
}

.button-success {
  background: linear-gradient(135deg, #238b6a, #2d9d78);
  color: white;
  border-color: transparent;
}

.button-success:hover:not(:disabled) {
  background: linear-gradient(135deg, #2a9d7a, #35b08a);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(45, 157, 120, 0.3);
}

.button-warning {
  background: linear-gradient(135deg, #d4952f, #e8a838);
  color: white;
  border-color: transparent;
}

.button-warning:hover:not(:disabled) {
  background: linear-gradient(135deg, #e0a23a, #f0b848);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(232, 168, 56, 0.3);
}

.button-info {
  background: linear-gradient(135deg, #4a7de0, #5b8def);
  color: white;
  border-color: transparent;
}

.button-info:hover:not(:disabled) {
  background: linear-gradient(135deg, #588cf0, #6a9df5);
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
