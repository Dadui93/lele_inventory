import axios from 'axios'

// 直连后端，避免 Vite 代理指向卡死的旧进程
const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8084/api'

export const api = axios.create({
  baseURL: API_BASE,
  withCredentials: true,
  timeout: 15000
})

export function apiErrorMessage(e, fallback = '请求失败') {
  if (e?.code === 'ECONNABORTED') return '请求超时：后端可能卡住或未启动'
  if (!e?.response) return `连不上后端（请确认已启动 ${API_BASE.replace(/\/api$/, '')}）`
  const data = e.response.data
  return (
    data?.error ||
    data?.message ||
    data?.detail ||
    (typeof data === 'string' && data.trim() ? data : null) ||
    `HTTP ${e.response.status}` ||
    fallback
  )
}

export async function safeRequest(promise) {
  try {
    const res = await promise
    return { ok: true, data: res.data }
  } catch (e) {
    return { ok: false, error: apiErrorMessage(e) }
  }
}
