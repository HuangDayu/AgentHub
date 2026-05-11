// 主题配置

export type ThemeMode = 'light' | 'dark'

export interface ThemeConfig {
  mode: ThemeMode
  // 背景色
  bgColor: string
  // 文字颜色
  textColor: string
  // 卡片背景
  cardBg: string
  // 边框颜色
  borderColor: string
  // 阴影
  shadow: string
  // 毛玻璃透明度
  glassOpacity: number
  // 模糊度
  blur: string
}

export const LIGHT_THEME: ThemeConfig = {
  mode: 'light',
  bgColor: '#f5f7fa',
  textColor: '#1a1e29',
  cardBg: 'rgba(255, 255, 255, 0.95)',
  borderColor: 'rgba(38, 66, 102, 0.08)',
  shadow: '0 4px 16px rgba(0, 0, 0, 0.08)',
  glassOpacity: 0.95,
  blur: '10px'
}

export const DARK_THEME: ThemeConfig = {
  mode: 'dark',
  bgColor: '#1a1e29',
  textColor: '#ffffff',
  cardBg: 'rgba(30, 35, 45, 0.95)',
  borderColor: 'rgba(255, 255, 255, 0.1)',
  shadow: '0 4px 16px rgba(0, 0, 0, 0.3)',
  glassOpacity: 0.95,
  blur: '10px'
}

// 应用主题
export function applyTheme(theme: ThemeConfig) {
  const root = document.documentElement
  
  console.log('========================================')
  console.log(`🎨 Applying ${theme.mode.toUpperCase()} Theme`)
  console.log('========================================')
  console.log('Theme config:', JSON.stringify(theme, null, 2))
  
  // 设置主题模式
  root.setAttribute('data-theme', theme.mode)
  
  // 设置CSS变量
  root.style.setProperty('--bg-color', theme.bgColor)
  root.style.setProperty('--text-color', theme.textColor)
  root.style.setProperty('--card-bg', theme.cardBg)
  root.style.setProperty('--border-color', theme.borderColor)
  root.style.setProperty('--shadow-base', theme.shadow)
  root.style.setProperty('--glass-opacity', String(theme.glassOpacity))
  root.style.setProperty('--blur-amount', theme.blur)
  
  // 移除旧主题类
  root.classList.remove('theme-light', 'theme-dark')
  // 添加新主题类
  root.classList.add(`theme-${theme.mode}`)
  
  console.log('✅ Theme applied successfully')
  console.log('========================================')
  
  // 触发主题变更事件
  window.dispatchEvent(new CustomEvent('theme-changed', {
    detail: theme
  }))
}

// 获取保存的主题
export function getSavedTheme(): ThemeMode {
  const saved = localStorage.getItem('theme-mode')
  return (saved === 'dark' || saved === 'light') ? saved : 'light'
}

// 保存主题
export function saveTheme(mode: ThemeMode) {
  localStorage.setItem('theme-mode', mode)
}

// 初始化主题
export function initTheme() {
  const mode = getSavedTheme()
  const theme = mode === 'dark' ? DARK_THEME : LIGHT_THEME
  applyTheme(theme)
}

// 切换主题
export function toggleTheme(): ThemeMode {
  const currentMode = getSavedTheme()
  const newMode: ThemeMode = currentMode === 'light' ? 'dark' : 'light'
  const theme = newMode === 'dark' ? DARK_THEME : LIGHT_THEME
  
  saveTheme(newMode)
  applyTheme(theme)
  
  return newMode
}
