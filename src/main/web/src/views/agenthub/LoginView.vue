<template>
  <section class="login-page">
    <article class="login-card scale-in">
      <div class="login-header">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <p class="eyebrow">Tenant Console</p>
        <h1>登录</h1>
        <p class="muted">请输入凭据以访问工作台。</p>
      </div>
      <form class="login-form" @submit.prevent="submitLogin">
        <label class="field">
          <span>用户名</span>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <input v-model="username" placeholder="admin" autocomplete="username" />
          </div>
        </label>
        <label class="field">
          <span>密码</span>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input v-model="password" type="password" placeholder="••••••" autocomplete="current-password" />
          </div>
        </label>
        <p v-if="error" class="status fade-in">{{ error }}</p>
        <button type="submit" class="primary" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span>{{ loading ? '登录中...' : '登录' }}</span>
        </button>
      </form>
      <div class="login-footer">
        <p class="muted">首次使用？请联系管理员获取账号</p>
      </div>
    </article>
    <div class="background-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submitLogin() {
  if (!hasLoginCredentials()) { error.value = '请输入用户名和密码'; return }
  loading.value = true; error.value = ''
  try { await auth.login(username.value, password.value); router.push('/') } catch (e: any) { error.value = e.message || '登录失败，请重试' } finally { loading.value = false }
}

function hasLoginCredentials(): boolean {
  return Boolean(username.value && password.value)
}
</script>

<style scoped>
.login-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.login-card {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid var(--color-border);
  box-shadow: 
    0 24px 40px rgba(40, 54, 79, 0.12),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  backdrop-filter: blur(20px);
  border-radius: 28px;
  padding: 40px;
  width: min(440px, calc(100vw - 48px));
  display: grid;
  gap: 28px;
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
}

.logo-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  padding: 12px;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  border-radius: 16px; color: var(--color-text-inverse);
  box-shadow: 0 8px 20px rgba(58, 138, 214, 0.3);
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.eyebrow {
  margin: 0 0 8px;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  color: var(--color-warning);
  font-size: 12px;
  font-weight: 600;
}

.login-card h1 {
  margin: 0 0 8px;
  font-size: 1.75rem;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-form {
  display: grid;
  gap: 20px;
}

.field {
  display: grid;
  gap: 8px;
}

.field span {
  font-weight: 600;
  color: var(--color-primary-dark);
  font-size: 0.9rem;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  width: 18px;
  height: 18px;
  color: var(--color-text-light);
  pointer-events: none;
  transition: color 0.25s ease;
}

.input-wrapper input {
  width: 100%;
  padding: 14px 14px 14px 42px;
  border-radius: 14px;
  border: 1px solid var(--color-border);
  background: var(--bg-card-solid);
  font: inherit;
  font-size: 0.95rem;
  transition: all 0.25s ease;
}

.input-wrapper input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: var(--bg-card-solid);
  box-shadow: 0 0 0 3px rgba(58, 123, 213, 0.15);
}

.input-wrapper input:focus + .input-icon,
.input-wrapper:focus-within .input-icon {
  color: var(--color-primary);
}

.status {
  color: var(--color-error);
  margin: 0;
  padding: 12px;
  background: rgba(201, 74, 53, 0.08);
  border-radius: 12px;
  font-size: 0.9rem;
  text-align: center;
}

.primary {
  border: none;
  border-radius: 14px;
  padding: 14px 20px;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  color: #fff;
  cursor: pointer;
  font: inherit;
  font-weight: 600;
  font-size: 1rem;
  box-shadow: 0 8px 20px rgba(58, 138, 214, 0.25);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(58, 138, 214, 0.35);
  filter: brightness(1.05);
}

.primary:active:not(:disabled) {
  transform: translateY(0);
}

.primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: var(--color-text-inverse);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.muted {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.login-footer {
  text-align: center;
  padding-top: 8px;
  border-top: 1px solid rgba(22, 33, 50, 0.06);
}

/* Background Decoration */
.background-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.4;
  animation: float 20s ease-in-out infinite;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(58, 123, 213, 0.2), transparent 70%);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(247, 203, 110, 0.25), transparent 70%);
  bottom: -50px;
  left: -50px;
  animation-delay: -7s;
}

.circle-3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(45, 157, 120, 0.2), transparent 70%);
  top: 50%;
  left: 20%;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

/* Animations */
@keyframes scale-in {
  from { 
    opacity: 0; 
    transform: scale(0.95) translateY(20px); 
  }
  to { 
    opacity: 1; 
    transform: scale(1) translateY(0); 
  }
}

.scale-in {
  animation: scale-in 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.fade-in {
  animation: fade-in 0.3s ease forwards;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
