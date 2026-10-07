import React from 'react'
import { api, apiErrorMessage } from '../api/client'
import { formatUtc, isoToUtcLocalInput, utcLocalInputToIso, utcNowLocalInput } from '../utils/utc'

export default function BuyersPage() {
  const [rows, setRows] = React.useState([])
  const [platforms, setPlatforms] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState(null)
  const [editingId, setEditingId] = React.useState(null)
  const [draft, setDraft] = React.useState({ buyerId: '', buyerName: '', buyingPlatformId: null, inputTimeLocal: utcNowLocalInput() })

  async function refreshAll() {
    setLoading(true)
    try {
      const [bRes, pRes] = await Promise.all([api.get('/buyers'), api.get('/buying-platforms')])
      setRows(bRes.data || [])
      setPlatforms(pRes.data || [])
      setError(null)
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  React.useEffect(() => { refreshAll() }, [])

  function startNew() {
    if (!platforms.length) {
      setError('请先创建至少一个购买平台')
      return
    }
    setEditingId('new')
    setDraft({ buyerId: '', buyerName: '', buyingPlatformId: platforms[0]?.id ?? null, inputTimeLocal: utcNowLocalInput() })
    setError(null)
  }

  function startEdit(row) {
    setEditingId(row.id)
    setDraft({
      buyerId: row.buyerId || '',
      buyerName: row.buyerName || '',
      buyingPlatformId: row.buyingPlatformId,
      inputTimeLocal: isoToUtcLocalInput(row.inputTime)
    })
    setError(null)
  }

  function cancelEdit() {
    setEditingId(null)
    setDraft({ buyerId: '', buyerName: '', buyingPlatformId: null, inputTimeLocal: utcNowLocalInput() })
    setError(null)
  }

  async function onSave() {
    if (!draft.buyerId?.trim() || !draft.buyerName?.trim() || !draft.buyingPlatformId) {
      setError('客户编码、名称、购买平台都必填')
      return
    }
    setLoading(true)
    setError(null)
    try {
      const payload = {
        buyerId: draft.buyerId.trim(),
        buyerName: draft.buyerName.trim(),
        buyingPlatformId: Number(draft.buyingPlatformId),
        inputTime: utcLocalInputToIso(draft.inputTimeLocal)
      }
      if (editingId === 'new') await api.post('/buyers', payload)
      else await api.put(`/buyers/${editingId}`, payload)
      cancelEdit()
      await refreshAll()
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  async function onDelete(id) {
    if (!window.confirm('确定删除该客户？')) return
    setLoading(true)
    try {
      await api.delete(`/buyers/${id}`)
      await refreshAll()
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between mb-3">
        <h3 className="mb-0">客户</h3>
        {editingId === null ? (
          <button className="btn btn-primary btn-sm" onClick={startNew} disabled={loading}>新增</button>
        ) : (
          <button className="btn btn-secondary btn-sm" onClick={cancelEdit} disabled={loading}>取消编辑</button>
        )}
      </div>

      {error ? <div className="alert alert-danger">{error}</div> : null}

      {editingId !== null ? (
        <div className="border rounded p-3 mb-3 bg-light">
          <div className="row g-2 align-items-end">
            <div className="col-md-2">
              <label className="form-label mb-1">客户编码</label>
              <input className="form-control" autoFocus value={draft.buyerId} onChange={(e) => setDraft({ ...draft, buyerId: e.target.value })} />
            </div>
            <div className="col-md-3">
              <label className="form-label mb-1">客户名称</label>
              <input className="form-control" value={draft.buyerName} onChange={(e) => setDraft({ ...draft, buyerName: e.target.value })} />
            </div>
            <div className="col-md-3">
              <label className="form-label mb-1">购买平台</label>
              <select className="form-select" value={draft.buyingPlatformId ?? ''} onChange={(e) => setDraft({ ...draft, buyingPlatformId: Number(e.target.value) })}>
                {platforms.map((p) => <option key={p.id} value={p.id}>{p.buyPlatformName}</option>)}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label mb-1">输入时间 (UTC)</label>
              <input type="datetime-local" className="form-control" value={draft.inputTimeLocal} onChange={(e) => setDraft({ ...draft, inputTimeLocal: e.target.value })} />
            </div>
            <div className="col-md-2 d-flex gap-2">
              <button className="btn btn-success" onClick={onSave} disabled={loading}>{loading ? '...' : '保存'}</button>
              <button className="btn btn-outline-secondary" onClick={cancelEdit} disabled={loading}>取消</button>
            </div>
          </div>
        </div>
      ) : null}

      <div className="table-responsive">
        <table className="table table-bordered align-middle" style={{ tableLayout: 'fixed', width: '100%' }}>
          <thead className="table-light">
            <tr>
              <th style={{ width: 70 }}>DB ID</th>
              <th>客户编码</th>
              <th>客户名称</th>
              <th>购买平台</th>
              <th style={{ width: 180 }}>输入时间 (UTC)</th>
              <th style={{ width: 150 }}>操作</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id} className={editingId === r.id ? 'table-active' : undefined}>
                <td>{r.id}</td>
                <td>{r.buyerId}</td>
                <td>{r.buyerName}</td>
                <td>{r.buyingPlatformName}</td>
                <td className="small">{formatUtc(r.inputTime)}</td>
                <td>
                  <button className="btn btn-outline-primary btn-sm me-2" onClick={() => startEdit(r)} disabled={loading || editingId !== null}>编辑</button>
                  <button className="btn btn-outline-danger btn-sm" onClick={() => onDelete(r.id)} disabled={loading || editingId !== null}>删除</button>
                </td>
              </tr>
            ))}
            {!loading && rows.length === 0 ? (
              <tr><td colSpan={6} className="text-center text-muted">暂无数据，点右上角「新增」</td></tr>
            ) : null}
          </tbody>
        </table>
      </div>
    </div>
  )
}
