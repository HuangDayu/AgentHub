<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-header">
        <h1>Things Knowledge</h1>
        <p class="muted">多租户知识库平台</p>
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
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
      </form>
      <div class="demo-accounts">
        <p class="muted">演示账号（密码均为 <code>admin123</code>）</p>
        <div class="demo-list">
            <button class="demo-btn" @click="fillDemo('admin')">admin（平台）</button>
          <button class="demo-btn" @click="fillDemo('zhangsan')">zhangsan（租户）</button>
          <button class="demo-btn" @click="fillDemo('wangwu')">wangwu（用户）</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const demoPasswords: Record<string, string> = {
  admin: 'admin123',
  zhangsan: 'user123',
  lisi: 'user123',
  wangwu: 'user123',
}

function fillDemo(user: string) {
  username.value = user
  password.value = demoPasswords[user] ?? 'user123'
}

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    // Step 1: Login
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

    // Step 2: Get user info
    const meResp = await fetch('/api/v1/auth/me', {
      headers: { Authorization: `Bearer ${tokens.accessToken}` },
    })
    let roles: string[] = []
    if (meResp.ok) {
      const userInfo = await meResp.json()
      roles = userInfo.roles ?? []
    }

    // Step 3: Determine console from API roles
    const normalizedRoles = roles.map((r: string) => r.replace(/^ROLE_/, ''))
    let targetRoute = '/user'
    if (normalizedRoles.includes('OWNER')) {
      targetRoute = '/admin'
    } else if (normalizedRoles.includes('ADMIN')) {
      targetRoute = '/tenant'
    } else if (normalizedRoles.includes('USER')) {
      targetRoute = '/user'
    } else {
      targetRoute = '/user'
    }

    // Step 4: Store token and navigate based on role
    const tokenKey = 'things_knowledge_access_token'
    localStorage.setItem(tokenKey, tokens.accessToken)
    localStorage.setItem('things_knowledge_refresh_token', tokens.refreshToken)
    localStorage.setItem('things_knowledge_user_role', normalizedRoles[0] ?? 'VIEWER')
    localStorage.setItem('things_knowledge_username', username.value)

    // Also set per-console tokens for backward compatibility
    localStorage.setItem('tenant_console_access_token', tokens.accessToken)
    localStorage.setItem('user_console_access_token', tokens.accessToken)

    // Step 5: Navigate to appropriate console
    router.push(targetRoute)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #0f172a, #1e3a5f);
  padding: 24px;
}
.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  width: min(420px, 100%);
  box-shadow: 0 20px 60px rgba(0,0,0,.3);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-header h1 {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}
.field {
  margin-bottom: 20px;
}
.field label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}
.field input, .field select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: border-color .2s;
  outline: none;
}
.field input:focus, .field select:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59,130,246,.15);
}
button[type="submit"] {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #27415d, #4683d5);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: opacity .2s;
}
button[type="submit"]:hover { opacity: 0.9; }
button[type="submit"]:disabled { opacity: 0.5; cursor: not-allowed; }
.error {
  color: #dc2626;
  font-size: 0.85rem;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #fef2f2;
  border-radius: 6px;
}
.muted { color: #94a3b8; }
.demo-accounts {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
  text-align: center;
}
.demo-accounts p {
  font-size: 0.8rem;
  margin-bottom: 10px;
}
.demo-list {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}
.demo-btn {
  padding: 6px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #f8fafc;
  color: #475569;
  font-size: 0.8rem;
  cursor: pointer;
  transition: background .2s;
}
.demo-btn:hover {
  background: #e2e8f0;
}
</style>
