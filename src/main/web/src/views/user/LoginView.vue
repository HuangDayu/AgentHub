<template>
  <section class="login-page">
    <article class="login-card">
      <div>
        <p class="eyebrow">User Console</p>
        <h1>登录</h1>
        <p class="muted">请输入凭据以访问知识检索与 AI 对话平台。</p>
      </div>
      <form class="login-form" @submit.prevent="submitLogin">
        <label class="field">
          <span>用户名</span>
          <input v-model="username" placeholder="请输入用户名" autocomplete="username" />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="password" type="password" placeholder="••••••" autocomplete="current-password" />
        </label>
        <p v-if="error" class="status">{{ error }}</p>
        <button class="primary" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </article>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/user-auth-api'
import { useAuthStore } from '@/store/auth-store'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submitLogin() {
  if (!username.value.trim() || !password.value) {
    return
  }
  error.value = ''
  loading.value = true
  try {
    const tokens = await login(username.value.trim(), password.value)
    authStore.setTokens(tokens.accessToken, tokens.refreshToken)
    router.push('/')
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
}

.login-card {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(22, 33, 50, 0.08);
  box-shadow: 0 24px 40px rgba(40, 54, 79, 0.1);
  backdrop-filter: blur(14px);
  border-radius: 24px;
  padding: 36px;
  width: min(420px, calc(100vw - 48px));
  display: grid;
  gap: 24px;
}

.eyebrow {
  margin: 0 0 8px;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  color: #7a5f2b;
  font-size: 12px;
}

.login-card h1 {
  margin: 0;
}

.login-form {
  display: grid;
  gap: 16px;
}

.field {
  display: grid;
  gap: 8px;
}

.field input {
  width: 100%;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(38, 66, 102, 0.14);
  background: rgba(248, 250, 255, 0.92);
  font: inherit;
}

.field input:focus {
  outline: none;
  border-color: #3a8ad6;
  box-shadow: 0 0 0 3px rgba(58, 138, 214, 0.15);
}

.status {
  color: #8a3b2f;
  margin: 0;
}

.primary {
  border: none;
  border-radius: 14px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: #fff;
  cursor: pointer;
  font: inherit;
  font-weight: 500;
  transition: opacity 0.2s;
}

.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.muted {
  color: #5f6878;
}
</style>


