import React from 'react'
import { api, apiErrorMessage } from '../api/client'
import { formatUtc, isoToUtcLocalInput, utcLocalInputToIso, utcNowLocalInput } from '../utils/utc'

export default function ProductsPage() {
  const [products, setProducts] = React.useState([])
  const [categories, setCategories] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState(null)
  const [editingId, setEditingId] = React.useState(null)
  const [draft, setDraft] = React.useState({
    productId: '',
    productName: '',
    manufacturingName: '',
    productCategoryId: null,
    inputTimeLocal: utcNowLocalInput()
  })

  async function refreshAll() {
    setLoading(true)
    try {
      const [pRes, cRes] = await Promise.all([api.get('/products'), api.get('/product-categories')])
      setProducts(pRes.data || [])
      setCategories(cRes.data || [])
      setError(null)
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  React.useEffect(() => { refreshAll() }, [])

  function startNew() {
    if (!categories.length) {
      setError('请先创建至少一个品类')
      return
    }
    setEditingId('new')
    setDraft({
      productId: '',
      productName: '',
      manufacturingName: '',
      productCategoryId: categories[0]?.id ?? null,
      inputTimeLocal: utcNowLocalInput()
    })
    setError(null)
  }

  function startEdit(row) {
    setEditingId(row.id)
    setDraft({
      productId: row.productId || '',
      productName: row.productName || '',
      manufacturingName: row.manufacturingName || '',
      productCategoryId: row.productCategoryId,
      inputTimeLocal: isoToUtcLocalInput(row.inputTime)
    })
    setError(null)
  }

  function cancelEdit() {
    setEditingId(null)
    setDraft({ productId: '', productName: '', manufacturingName: '', productCategoryId: null, inputTimeLocal: utcNowLocalInput() })
    setError(null)
  }

  async function onSave() {
    if (!draft.productId?.trim() || !draft.productName?.trim() || !draft.productCategoryId) {
      setError('商品编码、名称、品类都必填')
      return
    }
    setLoading(true)
    setError(null)
    try {
      const payload = {
        productId: draft.productId.trim(),
        productName: draft.productName.trim(),
        manufacturingName: (draft.manufacturingName || '').trim(),
        productCategoryId: Number(draft.productCategoryId),
        inputTime: utcLocalInputToIso(draft.inputTimeLocal)
      }
      if (editingId === 'new') await api.post('/products', payload)
      else await api.put(`/products/${editingId}`, payload)
      cancelEdit()
      await refreshAll()
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  async function onDelete(id) {
    if (!window.confirm('确定删除该商品？')) return
    setLoading(true)
    try {
      await api.delete(`/products/${id}`)
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
        <h3 className="mb-0">商品</h3>
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
              <label className="form-label mb-1">商品编码</label>
              <input className="form-control" autoFocus value={draft.productId} onChange={(e) => setDraft({ ...draft, productId: e.target.value })} />
            </div>
            <div className="col-md-2">
              <label className="form-label mb-1">商品名称</label>
              <input className="form-control" value={draft.productName} onChange={(e) => setDraft({ ...draft, productName: e.target.value })} />
            </div>
            <div className="col-md-2">
              <label className="form-label mb-1">品类</label>
              <select className="form-select" value={draft.productCategoryId ?? ''} onChange={(e) => setDraft({ ...draft, productCategoryId: Number(e.target.value) })}>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.productCategoryName}</option>)}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label mb-1">厂商</label>
              <input className="form-control" value={draft.manufacturingName} onChange={(e) => setDraft({ ...draft, manufacturingName: e.target.value })} />
            </div>
            <div className="col-md-2">
              <label className="form-label mb-1">输入时间 (UTC)</label>
              <input type="datetime-local" className="form-control" value={draft.inputTimeLocal} onChange={(e) => setDraft({ ...draft, inputTimeLocal: e.target.value })} />
            </div>
            <div className="col-md-2 d-flex gap-2">
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
              <th>商品编码</th>
              <th>商品名称</th>
              <th>品类</th>
              <th>厂商</th>
              <th style={{ width: 180 }}>输入时间 (UTC)</th>
              <th style={{ width: 150 }}>操作</th>
            </tr>
          </thead>
          <tbody>
            {products.map((r) => (
              <tr key={r.id} className={editingId === r.id ? 'table-active' : undefined}>
                <td>{r.id}</td>
                <td>{r.productId}</td>
                <td>{r.productName}</td>
                <td>{r.productCategoryName}</td>
                <td>{r.manufacturingName || '-'}</td>
                <td className="small">{formatUtc(r.inputTime)}</td>
                <td>
                  <button className="btn btn-outline-primary btn-sm me-2" onClick={() => startEdit(r)} disabled={loading || editingId !== null}>编辑</button>
                  <button className="btn btn-outline-danger btn-sm" onClick={() => onDelete(r.id)} disabled={loading || editingId !== null}>删除</button>
                </td>
              </tr>
            ))}
            {!loading && products.length === 0 ? (
              <tr><td colSpan={7} className="text-center text-muted">暂无数据，点右上角「新增」</td></tr>
            ) : null}
          </tbody>
        </table>
      </div>
    </div>
  )
}
