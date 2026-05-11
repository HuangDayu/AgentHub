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
              <span class="option-label">{{ option.label }}</span>
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
  color: #27415d;
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
  background: white;
  border: 1px solid rgba(38, 66, 102, 0.15);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-height: 40px;
}

.select-trigger:hover {
  border-color: rgba(58, 138, 214, 0.4);
  background: rgba(58, 138, 214, 0.02);
}

.custom-select.is-open .select-trigger {
  border-color: #3a8ad6;
  box-shadow: 0 0 0 3px rgba(58, 138, 214, 0.1);
}

.custom-select.disabled .select-trigger {
  background: rgba(0, 0, 0, 0.03);
  cursor: not-allowed;
  opacity: 0.6;
}

.custom-select.has-error .select-trigger {
  border-color: #c94a35;
}

.select-value {
  flex: 1;
  font-size: 14px;
  color: #1a1e29;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.select-value.placeholder {
  color: rgba(38, 66, 102, 0.4);
}

.select-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  color: rgba(38, 66, 102, 0.5);
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
  background: white;
  border: 1px solid rgba(38, 66, 102, 0.1);
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  overflow: hidden;
}

.dropdown-search {
  padding: 8px;
  border-bottom: 1px solid rgba(38, 66, 102, 0.08);
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid rgba(38, 66, 102, 0.15);
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: #3a8ad6;
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
}

.dropdown-option:hover:not(.disabled) {
  background: rgba(58, 138, 214, 0.06);
}

.dropdown-option.selected {
  background: rgba(58, 138, 214, 0.1);
  color: #3a8ad6;
}

.dropdown-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.option-label {
  font-size: 14px;
  flex: 1;
}

.option-check {
  display: flex;
  align-items: center;
  color: #3a8ad6;
  margin-left: 8px;
}

.dropdown-empty {
  padding: 20px;
  text-align: center;
  color: rgba(38, 66, 102, 0.4);
  font-size: 14px;
}

.select-error {
  font-size: 12px;
  color: #c94a35;
  margin-top: 2px;
}

.select-hint {
  font-size: 12px;
  color: rgba(38, 66, 102, 0.5);
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
