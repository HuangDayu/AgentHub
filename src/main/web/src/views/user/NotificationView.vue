<template>
  <section class="grid">
    <div class="page-header">
      <div>
        <h2>通知中心</h2>
        <p class="muted">查看系统通知和消息。</p>
      </div>
      <button
        v-if="hasUnread"
        class="secondary"
        type="button"
        @click="markAllAsRead"
      >
        全部已读
      </button>
    </div>

    <p v-if="error" class="status">{{ error }}</p>

    <template v-if="notifications.length > 0">
      <article
        v-for="notification in notifications"
        :key="notification.id"
        class="panel notification-item"
        :class="{ unread: notification.status === 'unread' }"
        @click="markAsRead(notification)"
      >
        <div class="notification-header">
          <strong>{{ notification.title }}</strong>
          <span class="muted">{{ formatTime(notification.createdAt) }}</span>
        </div>
        <p class="notification-content">{{ notification.content }}</p>
        <div class="notification-meta">
          <span v-if="notification.channel" class="tag">{{ notification.channel }}</span>
          <span
            class="tag"
            :class="notification.status === 'unread' ? 'warning' : 'success'"
          >
            {{ notification.status === 'unread' ? '未读' : '已读' }}
          </span>
        </div>
      </article>
    </template>

    <article v-else-if="!loading" class="empty-state">
      <p>暂无通知消息。</p>
    </article>

    <p v-if="loading" class="muted" style="text-align: center;">
      <span class="loading-spinner"></span> 加载中...
    </p>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  listNotifications,
  markNotificationRead,
  markAllRead,
  type Notification,
} from '@/api/notification-api'
import { relativeTime } from '@/common/format'

const notifications = ref<Notification[]>([])
const loading = ref(true)
const error = ref('')

const hasUnread = computed(() => notifications.value.some((n) => n.status === 'unread'))

onMounted(async () => {
  await loadNotifications()
})

async function loadNotifications() {
  loading.value = true
  try {
    const resp = await listNotifications()
    notifications.value = resp.items
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '加载通知失败'
  } finally {
    loading.value = false
  }
}

async function markAsRead(notification: Notification) {
  if (notification.status === 'read') return
  try {
    await markNotificationRead(notification.id)
    notification.status = 'read'
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '标记已读失败'
  }
}

async function markAllAsRead() {
  try {
    await markAllRead()
    notifications.value.forEach((n) => {
      n.status = 'read'
    })
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '标记全部已读失败'
  }
}

function formatTime(iso: string): string {
  return relativeTime(iso)
}
</script>

<style scoped>
.notification-item {
  cursor: pointer;
  transition: box-shadow 0.15s;
  display: grid;
  gap: 8px;
}

.notification-item:hover {
  box-shadow: 0 32px 48px rgba(40, 54, 79, 0.12);
}

.notification-item.unread {
  border-left: 3px solid #3a8ad6;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-header .muted {
  font-size: 13px;
  flex-shrink: 0;
}

.notification-content {
  margin: 0;
  font-size: 14px;
  color: #2a2a2a;
  line-height: 1.6;
}

.notification-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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


