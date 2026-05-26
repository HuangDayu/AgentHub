<template>
  <div class="login-wrapper">
    <div class="login-bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    <div class="login-card">
      <div class="login-header">
        <div class="brand-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <h1>AgentHub</h1>
        <p class="subtitle">AI Agent 生命周期管理平台</p>
      </div>
      <form @submit.prevent="handleLogin">
        <div class="field">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            autocomplete="username"
            required
          />
        </div>
        <div class="field">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            required
          />
        </div>
        <p v-if="error" class="error-msg">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="15" y1="9" x2="9" y2="15"/>
            <line x1="9" y1="9" x2="15" y2="15"/>
          </svg>
          {{ error }}
        </p>
        <button type="submit" :disabled="loading" class="login-btn">
          <span v-if="loading" class="btn-loader"></span>
          <span v-else>{{ loading ? '登录中…' : '登 录' }}</span>
        </button>
      </form>
      <div class="demo-section">
        <p class="demo-label">演示账号（密码均为 <code>admin123</code>）</p>
        <div class="demo-list">
          <button class="demo-btn" @click="fillDemo('admin')">admin</button>
          <button class="demo-btn" @click="fillDemo('zhangsan')">zhangsan</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()

// Initialize theme on login page for first visit
const themeStore = useThemeStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const demoPasswords: Record<string, string> = {
  admin: 'admin123',
  zhangsan: 'user123',
}

function fillDemo(user: string) {
  username.value = user
  password.value = demoPasswords[user] ?? 'user123'
}

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const loginResp = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value }),
    })
    if (!loginResp.ok) {
      const text = await loginResp.text()
      throw new Error(text || '用户名或密码错误')
    }
    const tokens = await loginResp.json()

    const tokenKey = 'agenthub_access_token'
    localStorage.setItem(tokenKey, tokens.accessToken)
    localStorage.setItem('agenthub_refresh_token', tokens.refreshToken)
    localStorage.setItem('agenthub_username', username.value)

    router.push('/agenthub')
  } catch (err: any) {
    error.value = err.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page, #f5f1eb);
  background-image: var(--bg-gradient, none);
  position: relative;
  overflow: hidden;
}

.login-bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}

.shape-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  right: -150px;
  background: radial-gradient(circle, var(--color-primary, #3a7bd5), transparent 70%);
  animation: floatShape 12s ease-in-out infinite;
}

.shape-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  left: -100px;
  background: radial-gradient(circle, var(--color-accent, #f0c75e), transparent 70%);
  animation: floatShape 16s ease-in-out infinite reverse;
}

.shape-3 {
  width: 200px;
  height: 200px;
  top: 30%;
  left: 10%;
  background: radial-gradient(circle, var(--color-primary-light, #5f9cf0), transparent 70%);
  animation: floatShape 10s ease-in-out infinite 2s;
}

@keyframes floatShape {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 36px;
  background: var(--bg-card-solid, #ffffff);
  border: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
  border-radius: 20px;
  box-shadow: var(--shadow-xl, 0 20px 56px rgba(26, 30, 43, 0.12));
  position: relative;
  z-index: 1;
  animation: cardEntry 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes cardEntry {
  from { opacity: 0; transform: translateY(24px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.brand-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  padding: 14px;
  background: linear-gradient(135deg, var(--color-primary-dark, #1e3a6f), var(--color-primary, #3a7bd5));
  border-radius: 16px;
  color: var(--color-text-inverse, #f8faff);
  box-shadow: var(--shadow-glow, 0 4px 20px rgba(58, 123, 213, 0.25));
}

.brand-icon svg {
  width: 100%;
  height: 100%;
}

.login-header h1 {
  font-family: var(--font-heading, inherit);
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-heading, #0f1729);
  margin: 0 0 6px;
}

.subtitle {
  font-size: 0.9rem;
  color: var(--color-text-muted, #5d667a);
  margin: 0;
}

.field {
  margin-bottom: 20px;
}

.field label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text-muted, #5d667a);
  margin-bottom: 6px;
}

.field input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--color-border-strong, rgba(26, 30, 43, 0.12));
  border-radius: 12px;
  background: var(--bg-input, #ffffff);
  font-family: var(--font-body, inherit);
  font-size: 0.95rem;
  color: var(--color-text, #1a1e2b);
  transition: all 0.25s ease;
  outline: none;
}

.field input:focus {
  border-color: var(--color-primary, #3a7bd5);
  box-shadow: 0 0 0 3px var(--color-primary-subtle, rgba(58, 123, 213, 0.12));
}

.error-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-error, #d44c3a);
  font-size: 0.85rem;
  margin-bottom: 16px;
  padding: 10px 14px;
  background: rgba(212, 76, 58, 0.08);
  border-radius: 10px;
}

.error-msg svg {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, var(--color-primary-dark, #1e3a6f), var(--color-primary, #3a7bd5));
  color: var(--color-text-inverse, #f8faff);
  border: none;
  border-radius: 12px;
  font-family: var(--font-heading, inherit);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: var(--shadow-glow, 0 4px 20px rgba(58, 123, 213, 0.25));
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow, 0 6px 24px rgba(58, 123, 213, 0.35));
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.btn-loader {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: var(--color-text-inverse);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.demo-section {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
  text-align: center;
}

.demo-label {
  font-size: 0.8rem;
  color: var(--color-text-light, #8a94a8);
  margin-bottom: 12px;
}

.demo-label code {
  font-family: var(--font-mono, monospace);
  background: var(--bg-stripe, rgba(58, 123, 213, 0.02));
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.8rem;
  color: var(--color-text-muted, #5d667a);
}

.demo-list {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.demo-btn {
  padding: 8px 18px;
  border: 1px solid var(--color-border-strong, rgba(26, 30, 43, 0.12));
  border-radius: 10px;
  background: var(--bg-card-solid, #ffffff);
  color: var(--color-text-muted, #5d667a);
  font-family: var(--font-body, inherit);
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.demo-btn:hover {
  border-color: var(--color-primary, #3a7bd5);
  color: var(--color-primary, #3a7bd5);
  background: var(--color-primary-subtle, rgba(58, 123, 213, 0.08));
  transform: translateY(-2px);
}
</style>
