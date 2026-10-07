import React from 'react'
import { Link, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { api } from './api/client'
import { logout } from './pages/auth'
import LoginPage from './pages/LoginPage.jsx'
import ProductCategoriesPage from './pages/ProductCategoriesPage.jsx'
import ProductsPage from './pages/ProductsPage.jsx'
import BuyingPlatformsPage from './pages/BuyingPlatformsPage.jsx'
import BuyersPage from './pages/BuyersPage.jsx'
import InboundOrdersPage from './pages/InboundOrdersPage.jsx'
import TransitionShipmentsPage from './pages/TransitionShipmentsPage.jsx'
import OutboundOrdersPage from './pages/OutboundOrdersPage.jsx'
import InventoryPage from './pages/InventoryPage.jsx'
import AuditLogsPage from './pages/AuditLogsPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'

function RequireAuth({ children, authed, authChecked }) {
  const location = useLocation()
  if (!authChecked) {
    return <div className="container pt-4 text-muted">正在校验登录状态...</div>
  }
  if (!authed) return <Navigate to="/login" replace state={{ from: location }} />
  return children
}

export default function App() {
  const [authed, setAuthed] = React.useState(false)
  const [authChecked, setAuthChecked] = React.useState(false)
  const [username, setUsername] = React.useState(null)
  const navigate = useNavigate()

  React.useEffect(() => {
    api.get('/auth/me').then((res) => {
      if (res?.data?.ok) {
        setAuthed(true)
        setUsername(res.data.username || 'admin')
      } else {
        setAuthed(false)
        setUsername(null)
      }
    }).catch(() => {
      setAuthed(false)
      setUsername(null)
    }).finally(() => {
      setAuthChecked(true)
    })
  }, [])

  React.useEffect(() => {
    const id = api.interceptors.response.use(
      (r) => r,
      (e) => {
        if (e?.response?.status === 401) {
          setAuthed(false)
          setUsername(null)
          navigate('/login', { replace: true })
        }
        return Promise.reject(e)
      }
    )
    return () => api.interceptors.response.eject(id)
  }, [navigate])

  return (
    <Routes>
      <Route path="/login" element={<LoginPage onLogin={(u) => {
        setAuthed(true)
        setAuthChecked(true)
        setUsername(u)
        navigate('/inventory', { replace: true })
      }} />} />

      <Route path="*" element={
        <RequireAuth authed={authed} authChecked={authChecked}>
          <div>
            <div className="border-bottom" style={{ padding: '10px 0' }}>
              <div className="container">
                <div className="d-flex align-items-center justify-content-between">
                  <div className="d-flex gap-2 flex-wrap">
                    <Link className="btn btn-light btn-sm" to="/inventory">库存</Link>
                    <Link className="btn btn-light btn-sm" to="/product-categories">品类</Link>
                    <Link className="btn btn-light btn-sm" to="/products">商品</Link>
                    <Link className="btn btn-light btn-sm" to="/buying-platforms">平台</Link>
                    <Link className="btn btn-light btn-sm" to="/buyers">客户</Link>
                    <Link className="btn btn-light btn-sm" to="/inbound-orders">入库</Link>
                    <Link className="btn btn-light btn-sm" to="/transition-shipments">转运</Link>
                    <Link className="btn btn-light btn-sm" to="/outbound-orders">出库</Link>
                    <Link className="btn btn-light btn-sm" to="/audit-logs">日志</Link>
                  </div>

                  <div className="d-flex align-items-center gap-2">
                    <span className="text-muted small">{username || 'admin'}</span>
                    <button className="btn btn-outline-danger btn-sm" onClick={async () => {
                      await logout()
                      setAuthed(false)
                      setUsername(null)
                      navigate('/login', { replace: true })
                    }}>
                      退出
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div className="container" style={{ paddingTop: 16 }}>
              <Routes>
                <Route path="/" element={<Navigate to="/inventory" replace />} />
                <Route path="/inventory" element={<InventoryPage />} />
                <Route path="/product-categories" element={<ProductCategoriesPage />} />
                <Route path="/products" element={<ProductsPage />} />
                <Route path="/buying-platforms" element={<BuyingPlatformsPage />} />
                <Route path="/buyers" element={<BuyersPage />} />
                <Route path="/inbound-orders" element={<InboundOrdersPage />} />
                <Route path="/transition-shipments" element={<TransitionShipmentsPage />} />
                <Route path="/outbound-orders" element={<OutboundOrdersPage />} />
                <Route path="/audit-logs" element={<AuditLogsPage />} />
                <Route path="*" element={<NotFoundPage />} />
              </Routes>
            </div>
          </div>
        </RequireAuth>
      } />
    </Routes>
  )
}

