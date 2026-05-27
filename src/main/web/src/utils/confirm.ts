import { ref } from 'vue'

interface ConfirmState {
  visible: boolean
  title: string
  message: string
  resolve: ((value: boolean) => void) | null
}

const state = ref<ConfirmState>({
  visible: false,
  title: '确认操作',
  message: '',
  resolve: null,
})

/**
 * 显示一个 Promise 化的确认弹窗
 * @param message 确认消息
 * @param title 弹窗标题（默认 "确认操作"）
 * @returns Promise<boolean> — true 确认，false 取消
 *
 * @example
 * if (await showConfirm('确定要删除吗？')) {
 *   // 执行删除
 * }
 */
export function showConfirm(message: string, title = '确认操作'): Promise<boolean> {
  return new Promise((resolve) => {
    state.value = {
      visible: true,
      title,
      message,
      resolve,
    }
  })
}

export function confirmState() {
  return state
}

export function confirmOk() {
  const s = state.value
  if (s.resolve) {
    s.resolve(true)
  }
  s.visible = false
  s.resolve = null
}

export function confirmCancel() {
  const s = state.value
  if (s.resolve) {
    s.resolve(false)
  }
  s.visible = false
  s.resolve = null
}
