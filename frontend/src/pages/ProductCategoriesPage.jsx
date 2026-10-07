import React from 'react'
import { api, apiErrorMessage } from '../api/client'
import { formatUtc, isoToUtcLocalInput, utcLocalInputToIso, utcNowLocalInput } from '../utils/utc'

export default function ProductCategoriesPage() {
  const [rows, setRows] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState(null)
  const [editingId, setEditingId] = React.useState(null)
  const [draft, setDraft] = React.useState({ productCategoryId: '', productCategoryName: '', inputTimeLocal: utcNowLocalInput() })

  async function refresh() {
    setLoading(true)
    try {
      const res = await api.get('/product-categories')
      setRows(res.data || [])
      setError(null)
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  React.useEffect(() => { refresh() }, [])

  async function onSave() {
    if (!draft.productCategoryId?.trim() || !draft.productCategoryName?.trim()) {
      setError('品类编码和品类名称都必填')
      return
    }
    setLoading(true)
    setError(null)
    try {
      const payload = {
        productCategoryId: draft.productCategoryId.trim(),
        productCategoryName: draft.productCategoryName.trim(),
        inputTime: utcLocalInputToIso(draft.inputTimeLocal)
      }
      if (editingId === 'new') await api.post('/product-categories', payload)
      else await api.put(`/product-categories/${editingId}`, payload)
      cancelEdit()
      await refresh()
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  async function onDelete(id) {
    if (!window.confirm('确定删除该品类？相关商品可能会受影响。')) return
    setLoading(true)
    try {
      await api.delete(`/product-categories/${id}`)
      await refresh()
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  function startNew() {
    setEditingId('new')
    setDraft({ productCategoryId: '', productCategoryName: '', inputTimeLocal: utcNowLocalInput() })
    setError(null)
  }

  function startEdit(row) {
    setEditingId(row.id)
    setDraft({
      productCategoryId: row.productCategoryId || '',
      productCategoryName: row.productCategoryName || '',
      inputTimeLocal: isoToUtcLocalInput(row.inputTime)
    })
    setError(null)
  }

  function cancelEdit() {
    setEditingId(null)
    setDraft({ productCategoryId: '', productCategoryName: '', inputTimeLocal: utcNowLocalInput() })
    setError(null)
  }

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between mb-3">
        <h3 className="mb-0">品类</h3>
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
              <label className="form-label mb-1">品类编码</label>
              <input className="form-control" autoFocus value={draft.productCategoryId} onChange={(e) => setDraft({ ...draft, productCategoryId: e.target.value })} placeholder="例如 BEAUTY" />
            </div>
            <div className="col-md-3">
              <label className="form-label mb-1">品类名称</label>
              <input className="form-control" value={draft.productCategoryName} onChange={(e) => setDraft({ ...draft, productCategoryName: e.target.value })} placeholder="例如 美妆" />
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
          <colgroup>
            <col style={{ width: 70 }} />
            <col style={{ width: '18%' }} />
            <col />
            <col style={{ width: 180 }} />
            <col style={{ width: 150 }} />
          </colgroup>
          <thead className="table-light">
            <tr>
              <th>DB ID</th>
              <th>品类编码</th>
              <th>品类名称</th>
              <th>输入时间 (UTC)</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id} className={editingId === r.id ? 'table-active' : undefined}>
                <td>{r.id}</td>
                <td style={{ wordBreak: 'break-all' }}>{r.productCategoryId}</td>
                <td>{r.productCategoryName}</td>
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
