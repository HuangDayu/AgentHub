interface RequestOptions extends RequestInit {
  baseUrl: string
  bodyJson?: unknown
  headers?: Record<string, string>
}

function getDefaultBaseUrl(): string {
  if (typeof window !== 'undefined' && window.location && window.location.origin) {
    return window.location.origin
  }
  return 'http://localhost:8080'
}

function ensureValidBaseUrl(baseUrl: string): string {
  if (baseUrl && baseUrl.trim()) {
    return baseUrl.trim()
  }
  return getDefaultBaseUrl()
}

function buildUrl(baseUrl: string, path: string, params?: Record<string, string>) {
  const validBaseUrl = ensureValidBaseUrl(baseUrl)
  const url = new URL(path, validBaseUrl)
  if (params) {
    for (const [key, value] of Object.entries(params)) {
      url.searchParams.set(key, value)
    }
  }
  return url.toString()
}

function buildHeaders(options: RequestOptions) {
  const headers = new Headers(options.headers)
  if (options.bodyJson !== undefined) {
    headers.set('Content-Type', 'application/json')
  }
  return headers
}

function buildBody(bodyJson?: unknown) {
  return bodyJson === undefined ? undefined : JSON.stringify(bodyJson)
}

function injectAuthToken(headers: Headers) {
  try {
    const token = localStorage.getItem('agenthub_access_token')
    if (token) {
      headers.set('Authorization', `Bearer ${token}`)
    }
  } catch {
    // localStorage unavailable
  }
}

async function readError(response: Response) {
  const text = await response.text()
  return text || `请求失败：${response.status}`
}

const BASE_URL = ensureValidBaseUrl(import.meta.env.VITE_API_BASE_URL || '')

export async function get<T>(path: string, headers?: Record<string, string>): Promise<T> {
  return requestJson<T>(path, { baseUrl: BASE_URL, method: 'GET', headers })
}

export async function post<T>(path: string, bodyJson?: unknown, headers?: Record<string, string>): Promise<T> {
  return requestJson<T>(path, { baseUrl: BASE_URL, method: 'POST', bodyJson, headers })
}

export async function del<T>(path: string, headers?: Record<string, string>): Promise<T> {
  return requestJson<T>(path, { baseUrl: BASE_URL, method: 'DELETE', headers })
}

export async function patch<T>(path: string, bodyJson?: unknown, headers?: Record<string, string>): Promise<T> {
  return requestJson<T>(path, { baseUrl: BASE_URL, method: 'PATCH', bodyJson, headers })
}

export async function requestJson<T>(path: string, options: RequestOptions): Promise<T> {
  const url = buildUrl(options.baseUrl, path)
  const headers = buildHeaders(options)
  injectAuthToken(headers)
  const body = buildBody(options.bodyJson)

  const response = await fetch(url, {
    ...options,
    headers,
    body,
  })

  if (!response.ok) {
    throw new Error(await readError(response))
  }

  const text = await response.text()
  if (!text) {
    return {} as T
  }
  return JSON.parse(text) as T
}
