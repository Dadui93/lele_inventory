import React from 'react'
import { api, apiErrorMessage } from '../api/client'
import { formatUtc, isoToUtcLocalInput, utcLocalInputToIso, utcNowLocalInput } from '../utils/utc'

export default function BuyingPlatformsPage() {
  const [rows, setRows] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState(null)
  const [editingId, setEditingId] = React.useState(null)
  const [draft, setDraft] = React.useState({ buyingPlatformId: '', buyPlatformName: '', inputTimeLocal: utcNowLocalInput() })

  async function refresh() {
    setLoading(true)
    try {
      const res = await api.get('/buying-platforms')
      setRows(res.data || [])
      setError(null)
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  React.useEffect(() => { refresh() }, [])

  function startNew() {
    setEditingId('new')
    setDraft({ buyingPlatformId: '', buyPlatformName: '', inputTimeLocal: utcNowLocalInput() })
    setError(null)
  }

  function startEdit(row) {
    setEditingId(row.id)
    setDraft({
      buyingPlatformId: row.buyingPlatformId || '',
      buyPlatformName: row.buyPlatformName || '',
      inputTimeLocal: isoToUtcLocalInput(row.inputTime)
    })
    setError(null)
  }

  function cancelEdit() {
    setEditingId(null)
    setDraft({ buyingPlatformId: '', buyPlatformName: '', inputTimeLocal: utcNowLocalInput() })
    setError(null)
  }

  async function onSave() {
    if (!draft.buyingPlatformId?.trim() || !draft.buyPlatformName?.trim()) {
      setError('平台编码和平台名称都必填')
      return
    }
    setLoading(true)
    setError(null)
    try {
      const payload = {
        buyingPlatformId: draft.buyingPlatformId.trim(),
        buyPlatformName: draft.buyPlatformName.trim(),
        inputTime: utcLocalInputToIso(draft.inputTimeLocal)
      }
      if (editingId === 'new') await api.post('/buying-platforms', payload)
      else await api.put(`/buying-platforms/${editingId}`, payload)
      cancelEdit()
      await refresh()
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  async function onDelete(id) {
    if (!window.confirm('确定删除该平台？')) return
    setLoading(true)
    try {
      await api.delete(`/buying-platforms/${id}`)
      await refresh()
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between mb-3">
        <h3 className="mb-0">购买平台</h3>
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
            <div className="col-md-3">
              <label className="form-label mb-1">平台编码</label>
              <input className="form-control" autoFocus value={draft.buyingPlatformId} onChange={(e) => setDraft({ ...draft, buyingPlatformId: e.target.value })} placeholder="例如 XIANYU" />
            </div>
            <div className="col-md-3">
              <label className="form-label mb-1">平台名称</label>
              <input className="form-control" value={draft.buyPlatformName} onChange={(e) => setDraft({ ...draft, buyPlatformName: e.target.value })} placeholder="例如 闲鱼" />
            </div>
            <div className="col-md-3">
              <label className="form-label mb-1">输入时间 (UTC)</label>
              <input type="datetime-local" className="form-control" value={draft.inputTimeLocal} onChange={(e) => setDraft({ ...draft, inputTimeLocal: e.target.value })} />
            </div>
            <div className="col-md-3 d-flex gap-2">
              <button className="btn btn-success" onClick={onSave} disabled={loading}>{loading ? '保存中...' : '保存'}</button>
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
              <th>平台编码</th>
              <th>平台名称</th>
              <th style={{ width: 180 }}>输入时间 (UTC)</th>
              <th style={{ width: 150 }}>操作</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id} className={editingId === r.id ? 'table-active' : undefined}>
                <td>{r.id}</td>
                <td>{r.buyingPlatformId}</td>
                <td>{r.buyPlatformName}</td>
                <td className="small">{formatUtc(r.inputTime)}</td>
                <td>
                  <button className="btn btn-outline-primary btn-sm me-2" onClick={() => startEdit(r)} disabled={loading || editingId !== null}>编辑</button>
                  <button className="btn btn-outline-danger btn-sm" onClick={() => onDelete(r.id)} disabled={loading || editingId !== null}>删除</button>
                </td>
              </tr>
            ))}
            {!loading && rows.length === 0 ? (
              <tr><td colSpan={5} className="text-center text-muted">暂无数据，点右上角「新增」</td></tr>
            ) : null}
          </tbody>
        </table>
      </div>
    </div>
  )
}
