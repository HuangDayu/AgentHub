/**
 * 页面美化工具函数
 * 用于统一处理页面的毛玻璃效果、悬浮效果和表单弹窗化
 */

import { ref, type Ref } from 'vue'

/**
 * 效果配置接口
 */
export interface EffectConfig {
  glass: boolean
  float: boolean
  animation: boolean
}

/**
 * 表单弹窗配置接口
 */
export interface ModalFormConfig {
  visible: Ref<boolean>
  title: string
  loading?: Ref<boolean>
  confirmText?: string
  onConfirm: () => Promise<void> | void
  onCancel?: () => void
}

/**
 * 使用效果配置
 */
export function useEffects() {
  const config = ref<EffectConfig>({
    glass: true,
    float: true,
    animation: true
  })

  // 从 localStorage 加载配置
  const loadConfig = () => {
    const saved = localStorage.getItem('effect-settings')
    if (saved) {
      try {
        const parsed = JSON.parse(saved)
        config.value = { ...config.value, ...parsed }
      } catch (e) {
        console.error('Failed to load effect settings:', e)
      }
    }
    applyEffects()
  }

  // 应用效果到 DOM
  const applyEffects = () => {
    const root = document.documentElement
    
    // 毛玻璃效果
    root.classList.toggle('glass-disabled', !config.value.glass)
    
    // 悬浮效果
    root.classList.toggle('float-disabled', !config.value.float)
    
    // 动画效果
    root.classList.toggle('animation-disabled', !config.value.animation)
    
    // 综合效果开关
    const allDisabled = !config.value.glass && !config.value.float && !config.value.animation
    root.classList.toggle('effects-disabled', allDisabled)
  }

  // 保存配置
  const saveConfig = () => {
    localStorage.setItem('effect-settings', JSON.stringify(config.value))
    applyEffects()
  }

  // 切换效果
  const toggleEffect = (effect: keyof EffectConfig) => {
    config.value[effect] = !config.value[effect]
    saveConfig()
  }

  // 重置为默认值
  const resetToDefault = () => {
    config.value = { glass: true, float: true, animation: true }
    saveConfig()
  }

  return {
    config,
    loadConfig,
    toggleEffect,
    resetToDefault
  }
}

/**
 * 使用表单弹窗
 */
export function useModalForm(config: ModalFormConfig) {
  const open = () => {
    config.visible.value = true
  }

  const close = () => {
    config.visible.value = false
    if (config.onCancel) {
      config.onCancel()
    }
  }

  const confirm = async () => {
    if (config.loading) {
      config.loading.value = true
    }
    try {
      await config.onConfirm()
      close()
    } catch (error) {
      console.error('Form submission failed:', error)
    } finally {
      if (config.loading) {
        config.loading.value = false
      }
    }
  }

  return {
    open,
    close,
    confirm
  }
}

/**
 * 为元素添加毛玻璃效果类
 */
export function addGlassEffect(element: HTMLElement, intensity: 'light' | 'medium' | 'dark' = 'light') {
  element.classList.add(`glass-effect${intensity !== 'light' ? '-' + intensity : ''}`)
}

/**
 * 为元素添加悬浮效果类
 */
export function addFloatEffect(element: HTMLElement) {
  element.classList.add('float-effect')
}

/**
 * 为元素添加组合效果（毛玻璃 + 悬浮）
 */
export function addGlassFloatEffect(element: HTMLElement) {
  element.classList.add('glass-float')
}

/**
 * 移除所有效果类
 */
export function removeAllEffects(element: HTMLElement) {
  element.classList.remove(
    'glass-effect',
    'glass-effect-medium',
    'glass-effect-dark',
    'float-effect',
    'glass-float'
  )
}

/**
 * 检查效果是否启用
 */
export function isEffectEnabled(effect: keyof EffectConfig): boolean {
  const saved = localStorage.getItem('effect-settings')
  if (saved) {
    try {
      const config = JSON.parse(saved)
      return config[effect] ?? true
    } catch (e) {
      return true
    }
  }
  return true
}

/**
 * 为页面容器添加效果
 */
export function enhancePageContainer(container: HTMLElement | null) {
  if (!container) return
  
  // 检查效果是否启用
  const glassEnabled = isEffectEnabled('glass')
  const floatEnabled = isEffectEnabled('float')
  
  if (glassEnabled && floatEnabled) {
    addGlassFloatEffect(container)
  } else if (glassEnabled) {
    addGlassEffect(container)
  } else if (floatEnabled) {
    addFloatEffect(container)
  }
}
