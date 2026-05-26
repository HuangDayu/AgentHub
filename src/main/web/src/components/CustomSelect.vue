<template>
  <div class="custom-select-wrapper" :class="{ disabled: disabled, 'has-error': error }">
    <label v-if="label" class="select-label">{{ label }}</label>
    <div 
      class="custom-select" 
      :class="{ 'is-open': isOpen, 'has-value': modelValue }"
      @click="toggleDropdown"
      v-click-outside="closeDropdown"
    >
      <div class="select-trigger">
        <span class="select-value" :class="{ placeholder: !selectedOption }">
          {{ selectedOption ? selectedOption.label : placeholder }}
        </span>
        <span class="select-arrow" :class="{ rotated: isOpen }">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </span>
      </div>
      
      <transition name="dropdown">
        <div v-if="isOpen" class="select-dropdown">
          <div class="dropdown-search" v-if="searchable">
            <input 
              v-model="searchQuery" 
              type="text" 
              class="search-input"
              :placeholder="searchPlaceholder"
              @click.stop
            />
          </div>
          <div class="dropdown-options">
            <div
              v-for="option in filteredOptions"
              :key="option.value"
              class="dropdown-option"
              :class="{
                selected: option.value === modelValue,
                disabled: option.disabled
              }"
              @click.stop="selectOption(option)"
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
        </div>
      </transition>
    </div>
    <span v-if="error" class="select-error">{{ error }}</span>
    <span v-if="hint && !error" class="select-hint">{{ hint }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

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
  searchable?: boolean
  searchPlaceholder?: string
  emptyText?: string
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择',
  disabled: false,
  searchable: false,
  searchPlaceholder: '搜索...',
  emptyText: '暂无数据'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  change: [value: string | number, option: Option]
}>()

const isOpen = ref(false)
const searchQuery = ref('')

const selectedOption = computed(() => {
  return props.options.find(opt => opt.value === props.modelValue)
})

const filteredOptions = computed(() => {
  if (!props.searchable || !searchQuery.value) {
    return props.options
  }
  const query = searchQuery.value.toLowerCase()
  return props.options.filter(opt => 
    opt.label.toLowerCase().includes(query)
  )
})

const toggleDropdown = () => {
  if (props.disabled) return
  isOpen.value = !isOpen.value
  if (!isOpen.value) {
    searchQuery.value = ''
  }
}

const closeDropdown = () => {
  isOpen.value = false
  searchQuery.value = ''
}

const selectOption = (option: Option) => {
  if (option.disabled) return
  emit('update:modelValue', option.value)
  emit('change', option.value, option)
  closeDropdown()
}

// 点击外部关闭下拉框
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
  width: 150px;
}

.select-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-primary-dark);
  margin-bottom: 2px;
}

.custom-select {
  position: relative;
  width: 100%;
}

.select-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: var(--bg-input, #ffffff);
  border: 1px solid var(--color-border-strong);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-height: 40px;
  color: var(--color-text);
}

.select-trigger:hover {
  border-color: var(--color-primary, #3a7bd5);
  background: var(--bg-hover, rgba(58, 123, 213, 0.02));
}

.custom-select.is-open .select-trigger {
  border-color: var(--color-primary, #3a7bd5);
  box-shadow: 0 0 0 3px var(--color-primary-subtle, rgba(58, 123, 213, 0.1));
}

.custom-select.disabled .select-trigger {
  background: var(--bg-stripe, rgba(0, 0, 0, 0.03));
  cursor: not-allowed;
  opacity: 0.6;
}

.custom-select.has-error .select-trigger {
  border-color: var(--color-error);
}

.select-value {
  flex: 1;
  font-size: 14px;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.select-value.placeholder {
  color: var(--color-text-light);
}

.select-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  color: var(--color-text-light, var(--color-text-muted));
  transition: transform 0.2s ease;
  margin-left: 8px;
}

.select-arrow.rotated {
  transform: rotate(180deg);
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
  overflow: hidden;
}

.dropdown-search {
  padding: 8px;
  border-bottom: 1px solid var(--color-border, rgba(38, 66, 102, 0.08));
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border-strong);
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  background: var(--bg-input, #ffffff);
  color: var(--color-text);
}

.search-input:focus {
  border-color: var(--color-primary, #3a7bd5);
}

.dropdown-options {
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
}

.dropdown-option:hover:not(.disabled) {
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
  margin-top: 2px;
}

.select-hint {
  font-size: 12px;
  color: var(--color-text-light, var(--color-text-muted));
  margin-top: 2px;
}

/* 下拉动画 */
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
