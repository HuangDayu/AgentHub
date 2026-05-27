import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

const STORAGE_KEY = 'agenthub_api_error_history'
const MAX_RECORDS = 100

export interface ApiErrorRecord {
  id: string
  timestamp: number
  url: string
  status: number
  message: string
  read: boolean
}

function loadFromStorage(): ApiErrorRecord[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveToStorage(records: ApiErrorRecord[]) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(records))
  } catch {
    // storage full or unavailable
  }
}

export const useErrorHistoryStore = defineStore('errorHistory', () => {
  const records = ref<ApiErrorRecord[]>(loadFromStorage())
  const toastMessage = ref<string | null>(null)

  // 未读数量
  const unreadCount = computed(() => records.value.filter(r => !r.read).length)

  // 所有记录按时间倒序
  const sortedRecords = computed(() =>
    [...records.value].sort((a, b) => b.timestamp - a.timestamp)
  )

  // 自动持久化到 localStorage
  watch(records, (val) => {
    saveToStorage(val)
  }, { deep: true })

  function addError(status: number, url: string, message: string) {
    const record: ApiErrorRecord = {
      id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      timestamp: Date.now(),
      url,
      status,
      message,
      read: false,
    }
    records.value.unshift(record)
    // 超出上限则删除最旧的
    if (records.value.length > MAX_RECORDS) {
      records.value = records.value.slice(0, MAX_RECORDS)
    }
    // 设置 toast 消息
    toastMessage.value = message
  }

  function markAllRead() {
    records.value.forEach(r => { r.read = true })
  }

  function markOneRead(id: string) {
    const record = records.value.find(r => r.id === id)
    if (record) record.read = true
  }

  function clearAll() {
    records.value = []
    toastMessage.value = null
  }

  function dismissToast() {
    toastMessage.value = null
  }

  return {
    records,
    unreadCount,
    sortedRecords,
    toastMessage,
    addError,
    markAllRead,
    markOneRead,
    clearAll,
    dismissToast,
  }
})
