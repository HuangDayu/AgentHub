<template>
  <div class="custom-select-wrapper" :class="{ disabled: disabled, 'has-error': error }">
    <label v-if="label" class="select-label">{{ label }}</label>
    <div
      class="custom-select"
      :class="{ 'is-open': isOpen, 'has-value': modelValue != null && modelValue !== '' }"
      v-click-outside="closeDropdown"
    >
<input
  ref="inputRef"
  v-model="inputValue"
  type="text"
  class="select-input"
  :placeholder="placeholder"
  :disabled="disabled"
  @focus="onFocus"
  @click="onInputClick"
  @input="onInput"
  @keydown.down.prevent="highlightNext"
  @keydown.up.prevent="highlightPrev"
  @keydown.enter.prevent="selectHighlighted"
  @keydown.esc="closeDropdown"
/>
      <span class="select-arrow" :class="{ rotated: isOpen }">
        <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
          <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </span>

      <transition name="dropdown">
        <div v-if="isOpen" class="select-dropdown">
          <div
            v-for="(option, index) in filteredOptions"
            :key="option.value"
            class="dropdown-option"
            :class="{
              selected: option.value === modelValue,
              highlighted: index === highlightedIndex,
              disabled: option.disabled
            }"
            @click="selectOption(option)"
            @mouseenter="highlightedIndex = index"
          >
            <div class="option-content">
              <span class="option-label">{{ option.label }}</span>
              <span v-if="option.description" class="option-desc">{{ option.description }}</span>
            </div>
            <span v-if="option.value === modelValue" class="option-check">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M3.5 8L6.5 11L12.5 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
          </div>
          <div v-if="filteredOptions.length === 0" class="dropdown-empty">
            {{ emptyText }}
          </div>
        </div>
      </transition>
    </div>
    <span v-if="error" class="select-error">{{ error }}</span>
    <span v-if="hint && !error" class="select-hint">{{ hint }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'

interface Option {
  value: string | number
  label: string
  description?: string
  disabled?: boolean
}

interface Props {
  modelValue: string | number
  options: Option[]
  label?: string
  placeholder?: string
  disabled?: boolean
  error?: string
  hint?: string
  emptyText?: string
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择',
  disabled: false,
  emptyText: '暂无数据'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  change: [value: string | number, option: Option]
}>()

const inputRef = ref<HTMLInputElement>()
const isOpen = ref(false)
const inputValue = ref('')
const highlightedIndex = ref(-1)
const selectedGuard = ref(false)

const selectedOption = computed(() => {
  return props.options.find(opt => opt.value === props.modelValue)
})

const filteredOptions = computed(() => {
  const query = inputValue.value.trim().toLowerCase()
  if (!query) return props.options
  return props.options.filter(opt =>
    opt.label.toLowerCase().includes(query)
  )
})

watch(isOpen, (open) => {
  if (open) {
    inputValue.value = ''
    highlightedIndex.value = 0
  } else {
    const opt = selectedOption.value
    inputValue.value = opt ? opt.label : ''
  }
})

watch(() => props.modelValue, () => {
  if (!isOpen.value) {
    const opt = selectedOption.value
    inputValue.value = opt ? opt.label : ''
  }
}, { immediate: true })

function onFocus() {
  if (props.disabled || selectedGuard.value) return
  isOpen.value = true
}

function onInputClick() {
  if (props.disabled || selectedGuard.value) return
  isOpen.value = true
}

function onInput() {
  if (!isOpen.value) isOpen.value = true
  highlightedIndex.value = -1
}

function closeDropdown() {
  isOpen.value = false
}

function highlightNext() {
  if (filteredOptions.value.length === 0) return
  highlightedIndex.value = Math.min(highlightedIndex.value + 1, filteredOptions.value.length - 1)
  scrollToHighlighted()
}

function highlightPrev() {
  if (filteredOptions.value.length === 0) return
  highlightedIndex.value = Math.max(highlightedIndex.value - 1, 0)
  scrollToHighlighted()
}

function selectHighlighted() {
  if (highlightedIndex.value >= 0 && highlightedIndex.value < filteredOptions.value.length) {
    selectOption(filteredOptions.value[highlightedIndex.value])
  } else if (filteredOptions.value.length === 1) {
    selectOption(filteredOptions.value[0])
  }
}

function scrollToHighlighted() {
  nextTick(() => {
    const el = document.querySelector('.dropdown-option.highlighted')
    if (el) el.scrollIntoView({ block: 'nearest' })
  })
}

function selectOption(option: Option) {
  if (option.disabled) return
  emit('update:modelValue', option.value)
  emit('change', option.value, option)
  closeDropdown()
  selectedGuard.value = true
  setTimeout(() => { selectedGuard.value = false }, 200)
}

const vClickOutside = {
  mounted(el: any, binding: any) {
    el._clickOutside = (event: Event) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value()
      }
    }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el: any) {
    document.removeEventListener('click', el._clickOutside)
  }
}
</script>

<style scoped>
.custom-select-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.select-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-primary-dark);
}

.custom-select {
  position: relative;
  width: 100%;
}

.select-input {
  width: 100%;
  padding: 10px 36px 10px 14px;
  background: var(--bg-input, #ffffff);
  border: 1px solid var(--color-border-strong);
  border-radius: 10px;
  font-size: 14px;
  color: var(--color-text);
  outline: none;
  cursor: text;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.select-input:focus {
  border-color: var(--color-primary, #3a7bd5);
  box-shadow: 0 0 0 3px var(--color-primary-subtle, rgba(58, 123, 213, 0.1));
}

.select-input::placeholder {
  color: var(--color-text-light);
}

.custom-select.disabled .select-input {
  background: var(--bg-stripe, rgba(0, 0, 0, 0.03));
  cursor: not-allowed;
  opacity: 0.6;
}

.custom-select-wrapper.has-error .select-input {
  border-color: var(--color-error);
}

.select-arrow {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  color: var(--color-text-light);
  pointer-events: none;
  transition: transform 0.2s;
}

.select-arrow.rotated {
  transform: translateY(-50%) rotate(180deg);
}

.select-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: var(--bg-card-solid, #ffffff);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  box-shadow: var(--shadow-lg, 0 8px 24px rgba(0, 0, 0, 0.12));
  z-index: 1000;
  max-height: 240px;
  overflow-y: auto;
  padding: 4px 0;
}

.dropdown-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.15s;
  color: var(--color-text);
  font-size: 14px;
}

.dropdown-option:hover:not(.disabled),
.dropdown-option.highlighted {
  background: var(--color-primary-subtle, rgba(58, 123, 213, 0.06));
}

.dropdown-option.selected {
  background: var(--color-primary-subtle, rgba(58, 123, 213, 0.1));
  color: var(--color-primary, #3a7bd5);
}

.dropdown-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.option-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.option-label {
  font-size: 14px;
  font-weight: 500;
}

.option-desc {
  font-size: 11px;
  color: var(--color-text-light, #6a6a7a);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-option.selected .option-desc {
  color: var(--color-primary-light, #5f9cf0);
}

.option-check {
  display: flex;
  align-items: center;
  color: var(--color-primary, #3a7bd5);
  margin-left: 8px;
}

.dropdown-empty {
  padding: 20px;
  text-align: center;
  color: var(--color-text-light);
  font-size: 14px;
}

.select-error {
  font-size: 12px;
  color: var(--color-error);
}

.select-hint {
  font-size: 12px;
  color: var(--color-text-light);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
