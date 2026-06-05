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
  const config = createEffectConfig()
  return {
    config,
    loadConfig: () => loadEffectConfig(config),
    toggleEffect: (effect: keyof EffectConfig) => toggleOneEffect(config, effect),
    resetToDefault: () => resetEffectConfig(config),
  }
}

function createEffectConfig(): Ref<EffectConfig> {
  return ref<EffectConfig>({ glass: true, float: true, animation: true })
}

function loadEffectConfig(config: Ref<EffectConfig>): void {
  const saved = localStorage.getItem('effect-settings')
  if (!saved) { applyEffects(config); return }
  try {
    config.value = { ...config.value, ...JSON.parse(saved) }
  } catch (e) {
    console.error('Failed to load effect settings:', e)
  }
  applyEffects(config)
}

function applyEffects(config: Ref<EffectConfig>): void {
  const root = document.documentElement
  toggleCssClass(root, 'glass-disabled', !config.value.glass)
  toggleCssClass(root, 'float-disabled', !config.value.float)
  toggleCssClass(root, 'animation-disabled', !config.value.animation)
  const allDisabled = !config.value.glass && !config.value.float && !config.value.animation
  toggleCssClass(root, 'effects-disabled', allDisabled)
}

function toggleCssClass(root: HTMLElement, name: string, on: boolean): void {
  root.classList.toggle(name, on)
}

function saveEffectConfig(config: Ref<EffectConfig>): void {
  localStorage.setItem('effect-settings', JSON.stringify(config.value))
  applyEffects(config)
}

function toggleOneEffect(config: Ref<EffectConfig>, effect: keyof EffectConfig): void {
  config.value[effect] = !config.value[effect]
  saveEffectConfig(config)
}

function resetEffectConfig(config: Ref<EffectConfig>): void {
  config.value = { glass: true, float: true, animation: true }
  saveEffectConfig(config)
}

/**
 * 使用表单弹窗
 */
export function useModalForm(config: ModalFormConfig) {
  return {
    open: () => { config.visible.value = true },
    close: () => closeModal(config),
    confirm: () => confirmModal(config),
  }
}

function closeModal(config: ModalFormConfig): void {
  config.visible.value = false
  config.onCancel?.()
}

async function confirmModal(config: ModalFormConfig): Promise<void> {
  setLoading(config, true)
  await tryConfirm(config)
}

async function tryConfirm(config: ModalFormConfig): Promise<void> {
  try {
    await config.onConfirm()
    closeModal(config)
  } catch (error) {
    console.error('Form submission failed:', error)
  } finally {
    setLoading(config, false)
  }
}

function setLoading(config: ModalFormConfig, value: boolean): void {
  if (config.loading) config.loading.value = value
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
  if (!saved) return true
  try { const config = JSON.parse(saved); return config[effect] ?? true } catch { return true }
}

/**
 * 为页面容器添加效果
 */
export function enhancePageContainer(container: HTMLElement | null) {
  if (!container) return
  applyCombinedEffects(container)
}

function applyCombinedEffects(container: HTMLElement) {
  const glass = isEffectEnabled('glass')
  const float = isEffectEnabled('float')
  if (glass && float) { addGlassFloatEffect(container) } else if (glass) { addGlassEffect(container) } else if (float) { addFloatEffect(container) }
}
