import React from 'react'
import { api, apiErrorMessage } from '../api/client'
import { formatUtc, utcLocalInputToIso, utcNowLocalInput } from '../utils/utc'

export default function InboundOrdersPage() {
  const [orders, setOrders] = React.useState([])
  const [products, setProducts] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState(null)

  const [expanded, setExpanded] = React.useState({})
  const [itemsCache, setItemsCache] = React.useState({})

  const [formOpen, setFormOpen] = React.useState(true)
  const [form, setForm] = React.useState({
    inboundOrderId: '',
    inboundOrderDate: new Date().toISOString().slice(0, 10),
    inboundTrackingNo: '',
    inboundShipCarrier: '',
    inboundSourceName: '',
    isArrived: false,
    note: '',
    inputTimeLocal: utcNowLocalInput()
  })
  const [items, setItems] = React.useState([
    {
      productId: null,
      officialListingPrice: '',
      actualCost: '',
      itemSellingPrice: '',
      itemActualSellingPrice: '',
      note: ''
    }
  ])

  async function refreshOrders() {
    const res = await api.get('/inbound-orders')
    setOrders(res.data)
  }

  async function refreshProducts() {
    const res = await api.get('/products')
    setProducts(res.data)
  }

  React.useEffect(() => {
    refreshOrders()
    refreshProducts()
  }, [])

  async function toggleExpanded(orderDbId) {
    setExpanded((m) => ({ ...m, [orderDbId]: !m[orderDbId] }))
    if (!itemsCache[orderDbId]) {
      const res = await api.get(`/inbound-orders/${orderDbId}/items`)
      setItemsCache((c) => ({ ...c, [orderDbId]: res.data }))
    }
  }

  function addItemRow() {
    setItems((arr) => [
      ...arr,
      { productId: products[0]?.id ?? null, officialListingPrice: '', actualCost: '', itemSellingPrice: '', itemActualSellingPrice: '', note: '' }
    ])
  }

  function removeItemRow(idx) {
    setItems((arr) => arr.filter((_, i) => i !== idx))
  }

  async function onSubmit(e) {
    e.preventDefault()
    setError(null)
    if (!items.length) {
      setError('至少需要 1 条 item')
      return
    }
    if (!form.inboundOrderId || !form.inboundOrderDate) {
      setError('请填写入库单号和日期')
      return
    }

    const payload = {
      inboundOrderId: form.inboundOrderId,
      inboundOrderDate: form.inboundOrderDate,
      inboundTrackingNo: form.inboundTrackingNo || null,
      inboundShipCarrier: form.inboundShipCarrier || null,
      inboundSourceName: form.inboundSourceName || null,
      isArrived: !!form.isArrived,
      note: form.note || null,
      inputTime: utcLocalInputToIso(form.inputTimeLocal),
      items: items.map((it) => ({
        productId: it.productId,
        officialListingPrice: it.officialListingPrice === '' ? null : Number(it.officialListingPrice),
        actualCost: it.actualCost === '' ? null : Number(it.actualCost),
        itemSellingPrice: it.itemSellingPrice === '' ? null : Number(it.itemSellingPrice),
        itemActualSellingPrice: it.itemActualSellingPrice === '' ? null : Number(it.itemActualSellingPrice),
        note: it.note || null
      }))
    }

    setLoading(true)
    try {
      await api.post('/inbound-orders', payload)
      await refreshOrders()
      setForm({
        inboundOrderId: '',
        inboundOrderDate: new Date().toISOString().slice(0, 10),
        inboundTrackingNo: '',
        inboundShipCarrier: '',
        inboundSourceName: '',
        isArrived: false,
        note: '',
        inputTimeLocal: utcNowLocalInput()
      })
      setItems([{ productId: products[0]?.id ?? null, officialListingPrice: '', actualCost: '', itemSellingPrice: '', itemActualSellingPrice: '', note: '' }])
    } catch (e2) {
      setError(apiErrorMessage(e2, 'create failed'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between mb-2">
        <h3 className="mb-0">入库单</h3>
        <button className="btn btn-outline-primary btn-sm" onClick={() => setFormOpen((v) => !v)}>
          {formOpen ? '收起新增表单' : '展开新增表单'}
        </button>
      </div>

      {error ? <div className="alert alert-danger">{error}</div> : null}

      {formOpen ? (
        <div className="card mb-3">
          <div className="card-header">新增入库单（并录入 item）</div>
          <div className="card-body">
            <form onSubmit={onSubmit}>
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">入库单号（inbound_order_id）</label>
                  <input className="form-control" value={form.inboundOrderId} onChange={(e) => setForm({ ...form, inboundOrderId: e.target.value })} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label">日期（inbound_order_date）</label>
                  <input type="date" className="form-control" value={form.inboundOrderDate} onChange={(e) => setForm({ ...form, inboundOrderDate: e.target.value })} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label">追踪号（inbound_tracking_no）</label>
                  <input className="form-control" value={form.inboundTrackingNo} onChange={(e) => setForm({ ...form, inboundTrackingNo: e.target.value })} />
                </div>
                <div className="col-md-6">
                  <label className="form-label">承运商（inbound_ship_carrier）</label>
                  <input className="form-control" value={form.inboundShipCarrier} onChange={(e) => setForm({ ...form, inboundShipCarrier: e.target.value })} />
                </div>
                <div className="col-md-6">
                  <label className="form-label">来源（inbound_source_name）</label>
                  <input className="form-control" value={form.inboundSourceName} onChange={(e) => setForm({ ...form, inboundSourceName: e.target.value })} />
                </div>
                <div className="col-md-6">
                  <label className="form-label">是否到仓（is_arrived）</label>
                  <div className="form-check mt-2">
                    <input className="form-check-input" type="checkbox" checked={form.isArrived} onChange={(e) => setForm({ ...form, isArrived: e.target.checked })} id="inArrived" />
                    <label className="form-check-label" htmlFor="inArrived">是</label>
                  </div>
                </div>
                <div className="col-md-6">
                  <label className="form-label">输入时间（UTC）</label>
                  <input type="datetime-local" className="form-control" value={form.inputTimeLocal} onChange={(e) => setForm({ ...form, inputTimeLocal: e.target.value })} required />
                </div>
                <div className="col-12">
                  <label className="form-label">备注（note）</label>
                  <textarea className="form-control" rows="3" value={form.note} onChange={(e) => setForm({ ...form, note: e.target.value })} />
                </div>
              </div>

              <h5 className="mt-4">item 明细</h5>
              <div className="table-responsive">
                <table className="table table-bordered align-middle">
                  <thead className="table-light">
                    <tr>
                      <th>商品</th>
                      <th>官方挂牌价</th>
                      <th>实际成本</th>
                      <th>渠道成交价</th>
                      <th>实际卖出价</th>
                      <th>行备注</th>
                      <th style={{ width: 100 }}>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.map((it, idx) => (
                      <tr key={idx}>
                        <td style={{ minWidth: 260 }}>
                          <select className="form-select form-select-sm" value={it.productId ?? ''} onChange={(e) => {
                            const v = Number(e.target.value)
                            setItems((arr) => arr.map((x, i) => i === idx ? { ...x, productId: v } : x))
                          }}>
                            {products.map((p) => (
                              <option key={p.id} value={p.id}>{p.productCategoryName} / {p.productName}</option>
                            ))}
                          </select>
                        </td>
                        <td><input className="form-control form-control-sm" type="number" step="0.01" value={it.officialListingPrice} onChange={(e) => {
                          const v = e.target.value
                          setItems((arr) => arr.map((x, i) => i === idx ? { ...x, officialListingPrice: v } : x))
                        }} /></td>
                        <td><input className="form-control form-control-sm" type="number" step="0.01" value={it.actualCost} onChange={(e) => {
                          const v = e.target.value
                          setItems((arr) => arr.map((x, i) => i === idx ? { ...x, actualCost: v } : x))
                        }} /></td>
                        <td><input className="form-control form-control-sm" type="number" step="0.01" value={it.itemSellingPrice} onChange={(e) => {
                          const v = e.target.value
                          setItems((arr) => arr.map((x, i) => i === idx ? { ...x, itemSellingPrice: v } : x))
                        }} /></td>
                        <td><input className="form-control form-control-sm" type="number" step="0.01" value={it.itemActualSellingPrice} onChange={(e) => {
                          const v = e.target.value
                          setItems((arr) => arr.map((x, i) => i === idx ? { ...x, itemActualSellingPrice: v } : x))
                        }} /></td>
                        <td><input className="form-control form-control-sm" value={it.note} onChange={(e) => {
                          const v = e.target.value
                          setItems((arr) => arr.map((x, i) => i === idx ? { ...x, note: v } : x))
                        }} /></td>
                        <td>
                          <button type="button" className="btn btn-outline-danger btn-sm" onClick={() => removeItemRow(idx)} disabled={items.length <= 1}>-</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="d-flex justify-content-between align-items-center">
                <button type="button" className="btn btn-outline-primary btn-sm" onClick={addItemRow}>+ 添加 item</button>
                <button type="submit" className="btn btn-success" disabled={loading}>{loading ? '保存中...' : '保存入库单'}</button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      <div className="card">
        <div className="card-header">入库单列表</div>
        <div className="card-body">
          <div className="table-responsive">
            <table className="table table-striped table-bordered align-middle">
              <thead className="table-light">
                <tr>
                  <th>入库单号</th>
                  <th>日期</th>
                  <th>追踪号</th>
                  <th>承运商</th>
                  <th>来源</th>
                  <th>是否到仓</th>
                  <th>输入时间 (UTC)</th>
                  <th style={{ width: 160 }}>操作</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <React.Fragment key={o.id}>
                    <tr>
                      <td>{o.inboundOrderId}</td>
                      <td>{o.inboundOrderDate}</td>
                      <td>{o.inboundTrackingNo || '-'}</td>
                      <td>{o.inboundShipCarrier || '-'}</td>
                      <td>{o.inboundSourceName || '-'}</td>
                      <td>{o.arrived ? '是' : '否'}</td>
                      <td className="small">{formatUtc(o.inputTime)}</td>
                      <td>
                        <button className="btn btn-outline-primary btn-sm" onClick={() => toggleExpanded(o.id)}>
                          {expanded[o.id] ? '收起 items' : '查看 items'}
                        </button>
                      </td>
                    </tr>
                    {expanded[o.id] ? (
                      <tr>
                        <td colSpan={8}>
                          <ItemsInlineTable items={itemsCache[o.id] || []} />
                        </td>
                      </tr>
                    ) : null}
                  </React.Fragment>
                ))}
                {orders.length === 0 ? (
                  <tr><td colSpan={8} className="text-center text-muted">暂无数据</td></tr>
                ) : null}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}

function ItemsInlineTable({ items }) {
  return (
    <div className="table-responsive">
      <table className="table table-sm table-bordered align-middle mb-0">
        <thead className="table-light">
          <tr>
            <th>item ID</th>
            <th>品类</th>
            <th>商品</th>
            <th>官方挂牌价</th>
            <th>实际成本</th>
            <th>成交价</th>
            <th>实际卖出价</th>
            <th>输入时间 (UTC)</th>
          </tr>
        </thead>
        <tbody>
          {items.map((it) => (
            <tr key={it.id}>
              <td>{it.id}</td>
              <td>{it.productCategoryName}</td>
              <td>{it.productName}</td>
              <td>{it.officialListingPrice ?? '-'}</td>
              <td>{it.actualCost ?? '-'}</td>
              <td>{it.itemSellingPrice ?? '-'}</td>
              <td>{it.itemActualSellingPrice ?? '-'}</td>
              <td className="small">{formatUtc(it.inputTime)}</td>
            </tr>
          ))}
          {items.length === 0 ? <tr><td colSpan={8} className="text-center text-muted">暂无 items</td></tr> : null}
        </tbody>
      </table>
    </div>
  )
}

