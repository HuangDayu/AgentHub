<template>
  <section class="grid">
    <div class="page-header">
      <div>
        <h2>个人设置</h2>
        <p class="muted">管理你的个人信息。</p>
      </div>
    </div>

    <article class="panel">
      <h3>用户信息</h3>
      <p v-if="loadingUser" class="muted">
        <span class="loading-spinner"></span> 加载中...
      </p>
      <div v-else-if="userInfo" class="user-profile">
        <div class="profile-row">
          <span class="profile-label">用户名</span>
          <span>{{ userInfo.username }}</span>
        </div>
        <div v-if="userInfo.displayName" class="profile-row">
          <span class="profile-label">显示名称</span>
          <span>{{ userInfo.displayName }}</span>
        </div>
        <div v-if="userInfo.email" class="profile-row">
          <span class="profile-label">邮箱</span>
          <span>{{ userInfo.email }}</span>
        </div>
        <div v-if="userInfo.createdAt" class="profile-row">
          <span class="profile-label">注册时间</span>
          <span>{{ formatTime(userInfo.createdAt) }}</span>
        </div>
      </div>
    </article>

    <article class="panel">
      <h3>修改密码</h3>
      <form class="password-form" @submit.prevent="submitPassword">
        <label class="field">
          <span>当前密码</span>
          <input v-model="oldPassword" type="password" placeholder="••••••" autocomplete="current-password" />
        </label>
        <label class="field">
          <span>新密码</span>
          <input v-model="newPassword" type="password" placeholder="至少 6 位" autocomplete="new-password" />
        </label>
        <label class="field">
          <span>确认新密码</span>
          <input v-model="confirmPassword" type="password" placeholder="再次输入新密码" autocomplete="new-password" />
        </label>
        <p v-if="passwordError" class="status">{{ passwordError }}</p>
        <p v-if="passwordSuccess" class="success">{{ passwordSuccess }}</p>
        <button class="primary" type="submit" :disabled="changingPassword">
          {{ changingPassword ? '修改中...' : '修改密码' }}
        </button>
      </form>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getCurrentUser, changePassword, type UserInfo } from '@/api/user-auth-api'
import { formatDateTime } from '@/common/format'

const userInfo = ref<UserInfo | null>(null)
const loadingUser = ref(true)

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const changingPassword = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')

onMounted(async () => {
  try {
    userInfo.value = await getCurrentUser()
  } catch {
    // ignore
  } finally {
    loadingUser.value = false
  }
})

async function submitPassword() {
  passwordError.value = ''
  passwordSuccess.value = ''

  if (!oldPassword.value || !newPassword.value) {
    passwordError.value = '请填写所有密码字段'
    return
  }
  if (newPassword.value.length < 6) {
    passwordError.value = '新密码至少需要 6 位'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }

  changingPassword.value = true
  try {
    await changePassword(oldPassword.value, newPassword.value)
    passwordSuccess.value = '密码修改成功'
    oldPassword.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
  } catch (reason) {
    passwordError.value = reason instanceof Error ? reason.message : '修改密码失败'
  } finally {
    changingPassword.value = false
  }
}

function formatTime(iso: string): string {
  return formatDateTime(iso)
}
</script>

<style scoped>
.user-profile {
  display: grid;
  gap: 12px;
}

.profile-row {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(22, 33, 50, 0.06);
}

.profile-row:last-child {
  border-bottom: none;
}

.profile-label {
  width: 100px;
  flex-shrink: 0;
  color: #5f6878;
  font-size: 14px;
}

.password-form {
  display: grid;
  gap: 16px;
  max-width: 480px;
}

.success {
  color: #1a7a3a;
  margin: 0;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(22, 33, 50, 0.08);
  border-top-color: #3a8ad6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  vertical-align: middle;
  margin-right: 8px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>


