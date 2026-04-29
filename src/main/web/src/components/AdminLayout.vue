<template>
  <div class="shell">
    <header class="console-header">
      <div class="header-left">
        <h1>管理控制台</h1>
      </div>
      <div class="header-right">
        <div class="user-info">
          <div class="avatar">{{ userInitial }}</div>
          <span>{{ userName }}</span>
        </div>
        <button class="ghost" type="button" @click="logout">退出登录</button>
      </div>
    </header>
    <nav class="nav">
      <RouterLink to="/admin">总览</RouterLink>
      <RouterLink to="/admin/tenants">租户</RouterLink>
      <RouterLink to="/admin/billing">计费</RouterLink>
      <RouterLink to="/admin/audit">审计</RouterLink>
      <RouterLink to="/admin/connectors">连接器</RouterLink>
      <RouterLink to="/admin/policies">策略</RouterLink>
      <RouterLink to="/admin/tools">工具</RouterLink>
    </nav>
    <RouterView />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'

const router = useRouter()

const userName = ref('用户')
const userInitial = computed(() => userName.value.charAt(0).toUpperCase())

onMounted(() => {
  userName.value = localStorage.getItem('things_knowledge_username') ?? '用户'
})

function logout() {
  localStorage.removeItem('things_knowledge_access_token')
  localStorage.removeItem('things_knowledge_refresh_token')
  localStorage.removeItem('tenant_console_access_token')
  localStorage.removeItem('user_console_access_token')
  router.push('/login')
}
</script>

<style scoped>
.console-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(26, 30, 41, 0.08);
  box-shadow: 0 4px 12px rgba(32, 44, 68, 0.06);
  backdrop-filter: blur(12px);
  border-radius: 10px;
}
.header-left h1 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #1a1e29;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 0.85rem;
  color: #475569;
}
.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #264266, #3a8ad6);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 600;
}
</style>
