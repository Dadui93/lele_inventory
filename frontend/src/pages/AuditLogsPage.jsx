import React from 'react'
import { api, apiErrorMessage } from '../api/client'
import { formatUtc } from '../utils/utc'

const ACTIONS = ['', 'CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT']
const ENTITY_TYPES = [
  '',
  'PRODUCT_CATEGORY',
  'PRODUCT',
  'BUYING_PLATFORM',
  'BUYER',
  'INBOUND_ORDER',
  'TRANSITION_SHIPMENT',
  'OUTBOUND_ORDER',
  'ITEM',
  'AUTH'
]

const ACTION_LABEL = {
  CREATE: '创建',
  UPDATE: '更新',
  DELETE: '删除',
  LOGIN: '登录',
  LOGOUT: '退出'
}

const ENTITY_LABEL = {
  PRODUCT_CATEGORY: '品类',
  PRODUCT: '商品',
  BUYING_PLATFORM: '平台',
  BUYER: '客户',
  INBOUND_ORDER: '入库单',
  TRANSITION_SHIPMENT: '转运单',
  OUTBOUND_ORDER: '出库单',
  ITEM: 'Item',
  AUTH: '登录认证'
}

function prettyJson(s) {
  if (!s) return '-'
  try {
    return JSON.stringify(JSON.parse(s), null, 2)
  } catch {
    return s
  }
}

export default function AuditLogsPage() {
  const [rows, setRows] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState(null)
  const [action, setAction] = React.useState('')
  const [entityType, setEntityType] = React.useState('')
  const [q, setQ] = React.useState('')
  const [selected, setSelected] = React.useState(null)

  async function refresh() {
    setLoading(true)
    try {
      const res = await api.get('/audit-logs', {
        params: {
          action: action || undefined,
          entityType: entityType || undefined,
          q: q.trim() || undefined,
          limit: 300
        }
      })
      setRows(res.data || [])
      setError(null)
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  React.useEffect(() => {
    refresh()
  }, [])

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
        <div>
          <h3 className="mb-0">操作日志</h3>
          <div className="text-muted small mt-1">记录所有录入 / 编辑 / 删除 / 登录，便于事后追查</div>
        </div>
        <button className="btn btn-outline-secondary btn-sm" onClick={refresh} disabled={loading}>
          {loading ? '刷新中...' : '刷新'}
        </button>
      </div>

      {error ? <div className="alert alert-danger">{error}</div> : null}

      <div className="border rounded p-3 mb-3 bg-light">
        <div className="row g-2 align-items-end">
          <div className="col-md-3">
            <label className="form-label mb-1">操作类型</label>
            <select className="form-select" value={action} onChange={(e) => setAction(e.target.value)}>
              {ACTIONS.map((a) => (
                <option key={a || 'all'} value={a}>{a ? (ACTION_LABEL[a] || a) : '全部'}</option>
              ))}
            </select>
          </div>
          <div className="col-md-3">
            <label className="form-label mb-1">对象类型</label>
            <select className="form-select" value={entityType} onChange={(e) => setEntityType(e.target.value)}>
              {ENTITY_TYPES.map((t) => (
                <option key={t || 'all'} value={t}>{t ? (ENTITY_LABEL[t] || t) : '全部'}</option>
              ))}
            </select>
          </div>
          <div className="col-md-4">
            <label className="form-label mb-1">搜索（摘要/编码/用户）</label>
            <input className="form-control" value={q} onChange={(e) => setQ(e.target.value)} placeholder="例如 BEAUTY / admin / 入库" />
          </div>
          <div className="col-md-2">
            <button className="btn btn-primary w-100" onClick={refresh} disabled={loading}>查询</button>
          </div>
        </div>
      </div>

      <div className="table-responsive">
        <table className="table table-sm table-bordered table-hover align-middle">
          <thead className="table-light">
            <tr>
              <th style={{ width: 70 }}>ID</th>
              <th style={{ width: 170 }}>时间 (UTC)</th>
              <th style={{ width: 90 }}>用户</th>
              <th style={{ width: 70 }}>操作</th>
              <th style={{ width: 110 }}>对象</th>
              <th style={{ width: 70 }}>结果</th>
              <th>摘要</th>
              <th style={{ width: 80 }}></th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id} className={selected?.id === r.id ? 'table-active' : undefined}>
                <td>{r.id}</td>
                <td className="small">{formatUtc(r.occurredAt)}</td>
                <td>{r.username}</td>
                <td>{ACTION_LABEL[r.action] || r.action}</td>
                <td>{ENTITY_LABEL[r.entityType] || r.entityType}</td>
                <td>
                  {r.success
                    ? <span className="text-success">成功</span>
                    : <span className="text-danger">失败</span>}
                </td>
                <td>
                  <div>{r.summary}</div>
                  {r.businessKey ? <div className="text-muted small">业务键: {r.businessKey}</div> : null}
                  {r.entityId ? <div className="text-muted small">DB ID: {r.entityId}</div> : null}
                </td>
                <td>
                  <button className="btn btn-link btn-sm p-0" onClick={() => setSelected(r)}>详情</button>
                </td>
              </tr>
            ))}
            {!loading && rows.length === 0 ? (
              <tr><td colSpan={8} className="text-center text-muted">暂无日志</td></tr>
            ) : null}
          </tbody>
        </table>
      </div>

      {selected ? (
        <div className="border rounded p-3 mt-3">
          <div className="d-flex justify-content-between align-items-center mb-2">
            <h5 className="mb-0">日志详情 #{selected.id}</h5>
            <button className="btn btn-sm btn-outline-secondary" onClick={() => setSelected(null)}>关闭</button>
          </div>
          <div className="row g-3">
            <div className="col-md-6">
              <div className="small text-muted mb-1">变更前 (before)</div>
              <pre className="bg-light border rounded p-2 small" style={{ maxHeight: 320, overflow: 'auto' }}>
                {prettyJson(selected.beforeJson)}
              </pre>
            </div>
            <div className="col-md-6">
              <div className="small text-muted mb-1">变更后 (after)</div>
              <pre className="bg-light border rounded p-2 small" style={{ maxHeight: 320, overflow: 'auto' }}>
                {prettyJson(selected.afterJson)}
              </pre>
            </div>
            <div className="col-12 small text-muted">
              请求: {selected.requestPath || '-'}
              {!selected.success && selected.errorMessage ? ` | 错误: ${selected.errorMessage}` : ''}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  )
}
