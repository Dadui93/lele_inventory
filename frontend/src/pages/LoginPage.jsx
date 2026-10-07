import React from 'react'
import { apiErrorMessage } from '../api/client'
import { login } from '../api/auth'

export default function LoginPage({ onLogin }) {
  const [username, setUsername] = React.useState('admin')
  const [password, setPassword] = React.useState('admin')
  const [error, setError] = React.useState(null)
  const [loading, setLoading] = React.useState(false)

  async function onSubmit(e) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login(username, password)
      onLogin?.(username)
    } catch (e2) {
      if (e2?.response?.status === 401) {
        setError(e2.response.data?.error || '用户名或密码错误')
      } else {
        setError(apiErrorMessage(e2, '登录失败'))
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container" style={{ maxWidth: 520, paddingTop: 40 }}>
      <h3>登录</h3>
      <form onSubmit={onSubmit} className="mt-3">
        <div className="mb-3">
          <label className="form-label">用户名</label>
          <input className="form-control" value={username} onChange={(e) => setUsername(e.target.value)} />
        </div>
        <div className="mb-3">
          <label className="form-label">密码</label>
          <input type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} />
        </div>
        {error ? <div className="alert alert-danger">{error}</div> : null}
        <button className="btn btn-primary" type="submit" disabled={loading}>
          {loading ? '登录中...' : '登录'}
        </button>
      </form>
      <div className="text-muted mt-3" style={{ fontSize: 12 }}>
        默认账号：<b>admin / admin</b>
      </div>
    </div>
  )
}
