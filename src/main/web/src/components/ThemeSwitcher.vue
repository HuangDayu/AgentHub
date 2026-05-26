<template>
  <div class="theme-switcher" :class="{ 'is-expanded': expanded }">
    <button class="theme-trigger" @click="expanded = !expanded" title="切换主题">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="trigger-icon">
        <template v-if="store.currentTheme === 'aether'">
          <circle cx="12" cy="12" r="5"/>
          <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
        </template>
        <template v-else-if="store.currentTheme === 'nocturne'">
          <path d="M21 12.79A9 9 0 1 1 11.21 3a7 7 0 0 0 9.79 9.79z"/>
        </template>
        <template v-else-if="store.currentTheme === 'verdant'">
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
        </template>
        <template v-else>
          <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
        </template>
      </svg>
    </button>

    <Transition name="theme-panel">
      <div v-if="expanded" class="theme-panel">
        <div class="panel-header">
          <h4>主题切换</h4>
          <button class="close-btn" @click="expanded = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>
        <div class="theme-options">
          <button
            v-for="theme in store.themes"
            :key="theme.id"
            :class="['theme-card', { active: store.currentTheme === theme.id }]"
            @click="selectTheme(theme.id)"
          >
            <div class="theme-preview" :class="`preview-${theme.id}`">
              <div class="preview-dot"></div>
              <div class="preview-bars">
                <span></span><span></span><span></span>
              </div>
            </div>
            <div class="theme-info">
              <span class="theme-name">{{ theme.name }}</span>
              <span class="theme-desc">{{ theme.description }}</span>
            </div>
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useThemeStore } from '@/stores/theme'

const store = useThemeStore()
const expanded = ref(false)

function selectTheme(id: string) {
  store.applyTheme(id)
}
</script>

<style scoped>
.theme-switcher {
  position: relative;
}

.theme-trigger {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid var(--color-border-strong, rgba(26, 30, 43, 0.12));
  background: var(--bg-card-solid, #ffffff);
  color: var(--color-text-muted, #5d667a);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s ease;
  padding: 0;
}

.theme-trigger:hover {
  border-color: var(--color-primary, #3a7bd5);
  color: var(--color-primary, #3a7bd5);
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow, 0 4px 20px rgba(58, 123, 213, 0.25));
}

.trigger-icon {
  width: 20px;
  height: 20px;
}

.theme-panel {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  width: 280px;
  background: var(--bg-card-solid, #ffffff);
  border: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
  border-radius: 16px;
  box-shadow: var(--shadow-xl, 0 20px 56px rgba(26, 30, 43, 0.12));
  padding: 16px;
  z-index: 1000;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
}

.panel-header h4 {
  font-family: var(--font-heading, inherit);
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-heading, #0f1729);
  margin: 0;
}

.close-btn {
  width: 28px;
  height: 28px;
  padding: 6px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: var(--color-text-light, #8a94a8);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: var(--bg-hover, rgba(58, 123, 213, 0.04));
  color: var(--color-text-muted, #5d667a);
}

.close-btn svg {
  width: 100%;
  height: 100%;
}

.theme-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.theme-card {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid transparent;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.theme-card:hover {
  background: var(--bg-hover, rgba(58, 123, 213, 0.04));
  border-color: var(--color-border, rgba(26, 30, 43, 0.06));
}

.theme-card.active {
  background: var(--color-primary-subtle, rgba(58, 123, 213, 0.08));
  border-color: var(--color-primary, #3a7bd5);
}

.theme-preview {
  width: 44px;
  height: 36px;
  border-radius: 8px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px;
  border: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
}

/* Preview: Aether (light warm) */
.preview-aether {
  background: linear-gradient(135deg, var(--bg-page), var(--color-warning-subtle));
}
.preview-aether .preview-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #3a7bd5;
}
.preview-aether .preview-bars {
  display: flex;
  gap: 3px;
}
.preview-aether .preview-bars span {
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: var(--color-warning);
}
.preview-aether .preview-bars span:nth-child(2) { height: 10px; }
.preview-aether .preview-bars span:nth-child(3) { height: 6px; }

/* Preview: Nocturne (dark) */
.preview-nocturne {
  background: linear-gradient(135deg, #0d111a, var(--bg-codeblock));
}
.preview-nocturne .preview-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6699ff;
}
.preview-nocturne .preview-bars {
  display: flex;
  gap: 3px;
}
.preview-nocturne .preview-bars span {
  width: 4px;
  border-radius: 2px;
  background: #8896a6;
}
.preview-nocturne .preview-bars span:nth-child(1) { height: 14px; }
.preview-nocturne .preview-bars span:nth-child(2) { height: 10px; }
.preview-nocturne .preview-bars span:nth-child(3) { height: 6px; }

/* Preview: Verdant (green) */
.preview-verdant {
  background: linear-gradient(135deg, var(--color-success-subtle), var(--bg-page));
}
.preview-verdant .preview-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-success);
}
.preview-verdant .preview-bars {
  display: flex;
  gap: 3px;
}
.preview-verdant .preview-bars span {
  width: 4px;
  border-radius: 2px;
  background: #b88a3c;
}
.preview-verdant .preview-bars span:nth-child(1) { height: 14px; }
.preview-verdant .preview-bars span:nth-child(2) { height: 10px; }
.preview-verdant .preview-bars span:nth-child(3) { height: 6px; }

/* Preview: Cipher (purple) */
.preview-cipher {
  background: linear-gradient(135deg, var(--color-purple-subtle), var(--color-purple-subtle));
}
.preview-cipher .preview-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #7c5cfc;
}
.preview-cipher .preview-bars {
  display: flex;
  gap: 3px;
}
.preview-cipher .preview-bars span {
  width: 4px;
  border-radius: 2px;
  background: var(--color-accent);
}
.preview-cipher .preview-bars span:nth-child(1) { height: 14px; }
.preview-cipher .preview-bars span:nth-child(2) { height: 10px; }
.preview-cipher .preview-bars span:nth-child(3) { height: 6px; }

.theme-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.theme-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-heading, #0f1729);
}

.theme-desc {
  font-size: 0.8rem;
  color: var(--color-text-muted, #5d667a);
}

/* Transition */
.theme-panel-enter-active,
.theme-panel-leave-active {
  transition: all 0.25s ease;
}

.theme-panel-enter-from,
.theme-panel-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.96);
}
</style>
