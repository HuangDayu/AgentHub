<template>
  <div class="effect-toggle-panel">
    <div class="toggle-header">
      <span class="toggle-icon">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M10 2C5.58 2 2 5.58 2 10s3.58 8 8 8 8-3.58 8-8-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6s2.69-6 6-6 6 2.69 6 6-2.69 6-6 6z" fill="currentColor"/>
          <path d="M10 6c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4z" fill="currentColor"/>
        </svg>
      </span>
      <span class="toggle-title">主题设置</span>
    </div>
    
    <!-- 主题选择 -->
    <div class="theme-section">
      <div class="theme-row">
        <span class="theme-label">主题选择</span>
        <CustomSelect
          :model-value="themeStore.currentTheme"
          :options="themeOptions"
          @update:model-value="themeStore.applyTheme"
        />
      </div>
    </div>

    <div class="toggle-controls">
      <div class="toggle-item">
        <label class="toggle-label">
          <span class="label-text">毛玻璃效果</span>
          <div class="toggle-switch" :class="{ active: glassEnabled }" @click="toggleGlass">
            <span class="switch-slider"></span>
          </div>
        </label>
        <p class="toggle-hint">为卡片和面板添加半透明模糊背景</p>
      </div>
      
      <div class="toggle-item">
        <label class="toggle-label">
          <span class="label-text">悬浮效果</span>
          <div class="toggle-switch" :class="{ active: floatEnabled }" @click="toggleFloat">
            <span class="switch-slider"></span>
          </div>
        </label>
        <p class="toggle-hint">鼠标悬停时元素轻微上浮</p>
      </div>
      
      <div class="toggle-item">
        <label class="toggle-label">
          <span class="label-text">动画效果</span>
          <div class="toggle-switch" :class="{ active: animationEnabled }" @click="toggleAnimation">
            <span class="switch-slider"></span>
          </div>
        </label>
        <p class="toggle-hint">启用页面过渡和交互动画</p>
      </div>
    </div>
    
    <div class="toggle-actions">
      <button class="action-btn" @click="resetToDefault">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M2 8a6 6 0 1 1 12 0A6 6 0 0 1 2 8zm1.5 0a4.5 4.5 0 1 0 9 0 4.5 4.5 0 0 0-9 0z" fill="currentColor"/>
          <path d="M8 5v3l2 2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>恢复默认</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useThemeStore } from '@/stores/theme'
import CustomSelect from '@/components/CustomSelect.vue'

const themeStore = useThemeStore()

const themeOptions = computed(() =>
  themeStore.themes.map(t => ({ value: t.id, label: t.name, description: t.description }))
)

const glassEnabled = ref(true)
const floatEnabled = ref(true)
const animationEnabled = ref(true)

// 从 localStorage 加载设置
const loadSettings = () => {
  const saved = localStorage.getItem('effect-settings')
  if (saved) {
    try {
      const settings = JSON.parse(saved)
      glassEnabled.value = settings.glass ?? true
      floatEnabled.value = settings.float ?? true
      animationEnabled.value = settings.animation ?? true
    } catch (e) {
      console.error('Failed to load effect settings:', e)
    }
  }
  applyEffects()
}

// 保存设置到 localStorage
const saveSettings = () => {
  localStorage.setItem('effect-settings', JSON.stringify({
    glass: glassEnabled.value,
    float: floatEnabled.value,
    animation: animationEnabled.value
  }))
}

// 应用效果到 DOM
const applyEffects = () => {
  const root = document.documentElement
  
  // 毛玻璃效果
  if (glassEnabled.value) {
    root.classList.remove('glass-disabled')
  } else {
    root.classList.add('glass-disabled')
  }
  
  // 悬浮效果
  if (floatEnabled.value) {
    root.classList.remove('float-disabled')
  } else {
    root.classList.add('float-disabled')
  }
  
  // 动画效果
  if (animationEnabled.value) {
    root.classList.remove('animation-disabled')
  } else {
    root.classList.add('animation-disabled')
  }
  
  // 综合效果开关
  const allDisabled = !glassEnabled.value && !floatEnabled.value && !animationEnabled.value
  if (allDisabled) {
    root.classList.add('effects-disabled')
  } else {
    root.classList.remove('effects-disabled')
  }
  
  // 强制刷新页面以应用效果
  window.dispatchEvent(new CustomEvent('effects-changed', {
    detail: {
      glass: glassEnabled.value,
      float: floatEnabled.value,
      animation: animationEnabled.value
    }
  }))
}

// 切换毛玻璃效果
const toggleGlass = () => {
  glassEnabled.value = !glassEnabled.value
  saveSettings()
  applyEffects()
}

// 切换悬浮效果
const toggleFloat = () => {
  floatEnabled.value = !floatEnabled.value
  saveSettings()
  applyEffects()
}

// 切换动画效果
const toggleAnimation = () => {
  animationEnabled.value = !animationEnabled.value
  saveSettings()
  applyEffects()
}

// 恢复默认设置
const resetToDefault = () => {
  glassEnabled.value = true
  floatEnabled.value = true
  animationEnabled.value = true
  saveSettings()
  applyEffects()
}

// 监听变化
watch([glassEnabled, floatEnabled, animationEnabled], () => {
  saveSettings()
  applyEffects()
})

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.effect-toggle-panel {
  background: var(--bg-card, var(--bg-card-hover));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-lg, 0 8px 32px rgba(0, 0, 0, 0.1));
  border: 1px solid var(--color-border, rgba(255, 255, 255, 0.5));
}

.toggle-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.toggle-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  border-radius: 10px;
  color: var(--color-text-inverse);
}

.toggle-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-heading);
}

.toggle-controls {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.toggle-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toggle-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}

.label-text {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text);
}

.toggle-switch {
  position: relative;
  width: 48px;
  height: 26px;
  background: var(--color-border-strong);
  border-radius: 13px;
  cursor: pointer;
  transition: background 0.3s;
}

.toggle-switch.active {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
}

.switch-slider {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  background: var(--color-text-inverse, white);
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s;
}

.toggle-switch.active .switch-slider {
  transform: translateX(22px);
}

.toggle-hint {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 0;
}

/* Theme Section */
.theme-section {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border);
}

.theme-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.theme-label {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text);
  white-space: nowrap;
}

.toggle-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: transparent;
  border: 1px solid var(--color-border-strong);
  border-radius: 10px;
  color: var(--color-primary-dark);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: var(--color-primary-subtle, rgba(58, 123, 213, 0.05));
  border-color: var(--color-primary, rgba(58, 138, 214, 0.4));
  transform: translateY(-2px);
}

.action-btn svg {
  color: var(--color-text-muted, rgba(38, 66, 102, 0.6));
}
</style>
