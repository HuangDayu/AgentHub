<template>
  <div class="error-history-panel">
    <!-- 工具栏 -->
    <div class="panel-toolbar">
      <span class="record-count">共 {{ store.records.length }} 条记录</span>
      <button
        v-if="store.records.length > 0"
        class="clear-btn"
        @click="handleClear"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="3 6 5 6 21 6"/>
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
        </svg>
        清除全部
      </button>
    </div>

    <!-- 空状态 -->
    <div v-if="store.sortedRecords.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
        <line x1="12" y1="9" x2="12" y2="13"/>
        <line x1="12" y1="17" x2="12.01" y2="17"/>
      </svg>
      <p>暂无接口异常记录</p>
    </div>

    <!-- 异常列表 -->
    <div v-else class="error-list">
      <div
        v-for="record in store.sortedRecords"
        :key="record.id"
        class="error-item"
        :class="{ expanded: expandedId === record.id }"
        @click="toggleExpand(record.id)"
      >
        <div class="error-item-header">
          <div class="error-status-group">
            <span class="status-badge" :class="statusClass(record.status)">
              {{ record.status || 'NET' }}
            </span>
            <span class="error-url" :title="record.url">{{ truncateUrl(record.url) }}</span>
          </div>
          <div class="error-meta">
            <span class="error-time">{{ formatTime(record.timestamp) }}</span>
            <span class="expand-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </span>
          </div>
        </div>
        <Transition name="expand">
          <div v-if="expandedId === record.id" class="error-detail">
            <div class="detail-row">
              <span class="detail-label">请求地址</span>
              <code class="detail-value">{{ record.url }}</code>
            </div>
            <div class="detail-row">
              <span class="detail-label">状态码</span>
              <code class="detail-value">{{ record.status || '网络错误' }}</code>
            </div>
            <div class="detail-row">
              <span class="detail-label">错误信息</span>
              <pre class="detail-value detail-message">{{ record.message }}</pre>
            </div>
            <div class="detail-row">
              <span class="detail-label">发生时间</span>
              <span class="detail-value">{{ formatFullTime(record.timestamp) }}</span>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useErrorHistoryStore } from '@/stores/error-history'

const store = useErrorHistoryStore()
const expandedId = ref<string | null>(null)

function toggleExpand(id: string) {
  expandedId.value = expandedId.value === id ? null : id
  store.markOneRead(id)
}

function statusClass(status: number): string {
  if (status === 0) return 'status-net'
  if (status >= 500) return 'status-5xx'
  if (status >= 400) return 'status-4xx'
  return 'status-other'
}

function truncateUrl(url: string): string {
  try {
    const u = new URL(url)
    const path = u.pathname.length > 40 ? u.pathname.slice(0, 37) + '...' : u.pathname
    return path + u.search
  } catch {
    return url.length > 50 ? url.slice(0, 47) + '...' : url
  }
}

function formatTime(ts: number): string {
  const d = new Date(ts)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return `${d.getMonth() + 1}/${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function formatFullTime(ts: number): string {
  const d = new Date(ts)
  return `${d.getFullYear()}/${pad(d.getMonth() + 1)}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function pad(n: number): string {
  return n < 10 ? '0' + n : String(n)
}

function handleClear() {
  if (store.records.length > 0) {
    store.clearAll()
  }
}
</script>

<style scoped>
.error-history-panel {
  min-height: 200px;
}

/* ── 工具栏 ── */
.panel-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
}

.record-count {
  font-size: 0.85rem;
  color: var(--color-text-light, #8a94a8);
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: transparent;
  border: 1px solid var(--color-error-subtle, rgba(212, 76, 58, 0.2));
  border-radius: 8px;
  color: var(--color-error, #d44c3a);
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-btn svg {
  width: 14px;
  height: 14px;
}

.clear-btn:hover {
  background: var(--color-error-subtle, rgba(212, 76, 58, 0.08));
  border-color: var(--color-error, #d44c3a);
}

/* ── 空状态 ── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  color: var(--color-text-light, #8a94a8);
}

.empty-state svg {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  opacity: 0.4;
}

.empty-state p {
  font-size: 0.9rem;
  margin: 0;
}

/* ── 异常列表 ── */
.error-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.error-item {
  background: var(--bg-card, rgba(255, 255, 255, 0.02));
  border: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
}

.error-item:hover {
  border-color: var(--color-error-subtle, rgba(212, 76, 58, 0.15));
  background: var(--bg-hover, rgba(0, 0, 0, 0.02));
}

.error-item.expanded {
  border-color: var(--color-error-subtle, rgba(212, 76, 58, 0.25));
}

/* ── 列表项头部 ── */
.error-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  gap: 12px;
}

.error-status-group {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.status-badge {
  flex-shrink: 0;
  min-width: 48px;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 700;
  text-align: center;
  font-family: var(--font-mono, monospace);
}

.status-5xx {
  background: var(--color-error-subtle, rgba(212, 76, 58, 0.1));
  color: var(--color-error-dark, #991b1b);
}

.status-4xx {
  background: var(--color-warning-subtle, rgba(212, 148, 47, 0.1));
  color: var(--color-warning-dark, #92400e);
}

.status-net {
  background: rgba(124, 92, 252, 0.08);
  color: var(--color-purple, #7c5cfc);
}

.status-other {
  background: var(--color-info-subtle, rgba(91, 127, 255, 0.08));
  color: var(--color-info-dark, #0369a1);
}

.error-url {
  font-size: 0.85rem;
  color: var(--color-text-muted, #5d667a);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: var(--font-mono, monospace);
}

.error-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.error-time {
  font-size: 0.75rem;
  color: var(--color-text-light, #8a94a8);
  white-space: nowrap;
}

.expand-icon {
  display: flex;
  width: 20px;
  height: 20px;
  color: var(--color-text-light, #8a94a8);
  transition: transform 0.2s;
}

.expand-icon svg {
  width: 100%;
  height: 100%;
}

.expanded .expand-icon {
  transform: rotate(180deg);
}

/* ── 详情展开区 ── */
.error-detail {
  padding: 0 16px 16px;
  border-top: 1px solid var(--color-border, rgba(26, 30, 43, 0.06));
  padding-top: 12px;
}

.detail-row {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
  font-size: 0.82rem;
}

.detail-label {
  flex-shrink: 0;
  width: 64px;
  color: var(--color-text-light, #8a94a8);
  font-weight: 500;
}

.detail-value {
  flex: 1;
  color: var(--color-text, #1a2633);
  word-break: break-all;
  margin: 0;
  min-width: 0;
}

code.detail-value {
  font-family: var(--font-mono, monospace);
  font-size: 0.78rem;
  background: var(--bg-stripe, rgba(0, 0, 0, 0.03));
  padding: 2px 8px;
  border-radius: 4px;
}

pre.detail-value {
  font-family: var(--font-mono, monospace);
  font-size: 0.78rem;
  background: var(--color-error-subtle, rgba(212, 76, 58, 0.04));
  padding: 8px 12px;
  border-radius: 8px;
  white-space: pre-wrap;
  line-height: 1.5;
  max-height: 120px;
  overflow-y: auto;
}

/* ── 展开折叠动画 ── */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}

.expand-enter-to {
  opacity: 1;
}

/* Scrollbar */
.error-list::-webkit-scrollbar {
  width: 6px;
}

.error-list::-webkit-scrollbar-track {
  background: transparent;
}

.error-list::-webkit-scrollbar-thumb {
  background: var(--color-border-strong, rgba(0, 0, 0, 0.1));
  border-radius: 3px;
}
</style>
