import { ref, computed, watch } from 'vue'
import { defineStore } from 'pinia'

export interface ThemeOption {
  id: string
  name: string
  description: string
  icon: string
}

const AVAILABLE_THEMES: ThemeOption[] = [
  {
    id: 'aether',
    name: 'Aether',
    description: '明亮空灵 · 温暖通透',
    icon: 'sun',
  },
  {
    id: 'nocturne',
    name: 'Nocturne',
    description: '深邃优雅 · 夜曲沉静',
    icon: 'moon',
  },
  {
    id: 'verdant',
    name: 'Verdant',
    description: '自然绿意 · 生机盎然',
    icon: 'leaf',
  },
  {
    id: 'cipher',
    name: 'Cipher',
    description: '科技感 · 未来主义',
    icon: 'sparkles',
  },
]

export const useThemeStore = defineStore('theme', () => {
  const saved = localStorage.getItem('agenthub-theme')
  const currentTheme = ref<string>(saved && AVAILABLE_THEMES.some(t => t.id === saved) ? saved : 'aether')

  const themeConfig = computed<ThemeOption>(() =>
    AVAILABLE_THEMES.find(t => t.id === currentTheme.value) ?? AVAILABLE_THEMES[0]
  )

  const themes = computed(() => AVAILABLE_THEMES)

  function applyTheme(themeId: string) {
    document.documentElement.setAttribute('data-theme', themeId)
    currentTheme.value = themeId
    localStorage.setItem('agenthub-theme', themeId)
  }

  function cycleTheme() {
    const idx = AVAILABLE_THEMES.findIndex(t => t.id === currentTheme.value)
    const next = AVAILABLE_THEMES[(idx + 1) % AVAILABLE_THEMES.length]
    applyTheme(next.id)
    return next
  }

  // Initialize theme on store creation
  applyTheme(currentTheme.value)

  return {
    currentTheme,
    themeConfig,
    themes,
    applyTheme,
    cycleTheme,
  }
})
