import { runtimeConfig } from '@/common/runtime-config'
import { requestJson } from './http'

export interface Notification {
  id: string
  title: string
  content: string
  channel?: string
  status: 'unread' | 'read'
  createdAt: string
}

export interface NotificationListResponse {
  items: Notification[]
  total: number
}

export function listNotifications(_page = 1, _pageSize = 20) {
  return requestJson<NotificationListResponse>('/api/v1/user/notifications', {
    baseUrl: runtimeConfig.userApiBase,
  })
}

export function markNotificationRead(notificationId: string) {
  return requestJson<void>(`/api/v1/user/notifications/${notificationId}/read`, {
    baseUrl: runtimeConfig.userApiBase,
    method: 'PUT',
  })
}

export function markAllRead() {
  return requestJson<void>('/api/v1/user/notifications/read-all', {
    baseUrl: runtimeConfig.userApiBase,
    method: 'PUT',
  })
}

