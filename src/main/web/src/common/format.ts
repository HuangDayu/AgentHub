// ── Currency & Date Formatting ──────────────────────────

export function formatCurrency(amountCents: number, currency = 'CNY') {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency,
  }).format(amountCents / 100)
}

export function formatDate(value?: string | null) {
  if (!value) {
    return '未记录'
  }
  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export function formatDateTime(value?: string | null) {
  if (!value) {
    return '未记录'
  }
  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

// ── Relative Time ──────────────────────────────────────

export function relativeTime(iso: string): string {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return iso
  return resolveRelativeTime(Date.now() - date.getTime(), iso)
}

function resolveRelativeTime(diffMs: number, iso: string): string {
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin} 分钟前`
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour} 小时前`
  const diffDay = Math.floor(diffHour / 24)
  if (diffDay < 7) return `${diffDay} 天前`
  return formatDateTime(iso)
}

// ── Utility ─────────────────────────────────────────────

export function normalizeVersions(raw: string) {
  return raw.split(',').map((item) => item.trim()).filter(Boolean)
}
