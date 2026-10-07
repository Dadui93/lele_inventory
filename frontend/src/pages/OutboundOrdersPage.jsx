import React from 'react'
import { api, apiErrorMessage } from '../api/client'
import { formatUtc, utcLocalInputToIso, utcNowLocalInput } from '../utils/utc'

export default function OutboundOrdersPage() {
  const [orders, setOrders] = React.useState([])
  const [buyers, setBuyers] = React.useState([])
  const [eligibleItems, setEligibleItems] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState(null)

  const [expanded, setExpanded] = React.useState({})
  const [itemsCache, setItemsCache] = React.useState({})

  const [formOpen, setFormOpen] = React.useState(true)
  const [form, setForm] = React.useState({
    outboundOrderId: '',
    buyerId: null,
    isArrived: false,
    note: '',
    inputTimeLocal: utcNowLocalInput()
  })
  const [selected, setSelected] = React.useState([])

  async function refreshAll() {
    setLoading(true)
    try {
      const [oRes, bRes, itRes] = await Promise.all([
        api.get('/outbound-orders'),
        api.get('/buyers'),
        api.get('/items', { params: { status: 'IN_TRANSITION' } })
      ])
      setOrders(oRes.data)
      setBuyers(bRes.data)
      setEligibleItems(itRes.data)
      setSelected([])
      setError(null)

      setForm((f) => ({ ...f, buyerId: f.buyerId ?? (bRes.data[0]?.id ?? null) }))
    } catch (e) {
      setError(apiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  React.useEffect(() => { refreshAll() }, [])

  async function toggleExpanded(orderDbId) {
    setExpanded((m) => ({ ...m, [orderDbId]: !m[orderDbId] }))
    if (!itemsCache[orderDbId]) {
      const res = await api.get(`/outbound-orders/${orderDbId}/items`)
      setItemsCache((c) => ({ ...c, [orderDbId]: res.data }))
    }
  }

  function toggleItem(itId) {
    setSelected((arr) => (arr.includes(itId) ? arr.filter((x) => x !== itId) : [...arr, itId]))
  }

  async function onSubmit(e) {
    e.preventDefault()
    setError(null)
    if (!form.outboundOrderId) {
      setError('请填写出库单号')
      return
    }
    if (!form.buyerId) {
      setError('请选择客户（buyer）')
      return
    }
    if (!selected.length) {
      setError('至少选择 1 个 IN_TRANSITION item')
      return
    }

    const payload = {
      outboundOrderId: form.outboundOrderId,
      buyerId: form.buyerId,
      isArrived: !!form.isArrived,
      note: form.note || null,
      inputTime: utcLocalInputToIso(form.inputTimeLocal),
      itemIds: selected
    }

    setLoading(true)
    try {
      await api.post('/outbound-orders', payload)
      await refreshAll()
      setForm((f) => ({ ...f, outboundOrderId: '', isArrived: false, note: '', inputTimeLocal: utcNowLocalInput() }))
      setExpanded({})
      setItemsCache({})
    } catch (e2) {
      setError(apiErrorMessage(e2, 'create failed'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between mb-2">
        <h3 className="mb-0">出库单</h3>
        <button className="btn btn-outline-primary btn-sm" onClick={() => setFormOpen((v) => !v)}>
          {formOpen ? '收起新增表单' : '展开新增表单'}
        </button>
      </div>

      {error ? <div className="alert alert-danger">{error}</div> : null}

      {formOpen ? (
        <div className="card mb-3">
          <div className="card-header">新增出库单（IN_TRANSITION -> OUTBOUND）</div>
          <div className="card-body">
            <form onSubmit={onSubmit}>
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">出库单号（outbound_order_id）</label>
                  <input className="form-control" value={form.outboundOrderId} onChange={(e) => setForm({ ...form, outboundOrderId: e.target.value })} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label">客户（buyer）</label>
                  <select className="form-select" value={form.buyerId ?? ''} onChange={(e) => setForm({ ...form, buyerId: Number(e.target.value) })} required>
                    {buyers.map((b) => (
                      <option key={b.id} value={b.id}>{b.buyerName} / {b.buyingPlatformName}</option>
                    ))}
                  </select>
                </div>
                <div className="col-md-6">
                  <label className="form-label">是否到仓（is_arrived）</label>
                  <div className="form-check mt-2">
                    <input className="form-check-input" type="checkbox" checked={form.isArrived} onChange={(e) => setForm({ ...form, isArrived: e.target.checked })} id="oArrived" />
                    <label className="form-check-label" htmlFor="oArrived">是</label>
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

              <h5 className="mt-4">选择要出库的 item（当前必须是 IN_TRANSITION）</h5>
              <div className="table-responsive">
                <table className="table table-bordered table-sm align-middle">
                  <thead className="table-light">
                    <tr>
                      <th style={{ width: 70 }}>选</th>
                      <th>转运单</th>
                      <th>入库单</th>
                      <th>品类 / 商品</th>
                      <th style={{ width: 120 }}>官方挂牌价</th>
                      <th style={{ width: 120 }}>实际成本</th>
                      <th style={{ width: 120 }}>成交价</th>
                      <th style={{ width: 140 }}>实际卖出价</th>
                    </tr>
                  </thead>
                  <tbody>
                    {eligibleItems.map((it) => (
                      <tr key={it.id}>
                        <td>
                          <input type="checkbox" checked={selected.includes(it.id)} onChange={() => toggleItem(it.id)} />
                        </td>
                        <td>{it.transitionShipmentId || '-'}</td>
                        <td>{it.inboundOrderId}</td>
                        <td>{it.productCategoryName} / {it.productName}</td>
                        <td>{it.officialListingPrice ?? '-'}</td>
                        <td>{it.actualCost ?? '-'}</td>
                        <td>{it.itemSellingPrice ?? '-'}</td>
                        <td>{it.itemActualSellingPrice ?? '-'}</td>
                      </tr>
                    ))}
                    {eligibleItems.length === 0 ? (
                      <tr><td colSpan={8} className="text-center text-muted">暂无可出库的 IN_TRANSITION items</td></tr>
                    ) : null}
                  </tbody>
                </table>
              </div>

              <div className="d-flex justify-content-end mt-3">
                <button type="submit" className="btn btn-success" disabled={loading}>{loading ? '保存中...' : '保存出库单'}</button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      <div className="card">
        <div className="card-header">出库单列表</div>
        <div className="card-body">
          <div className="table-responsive">
            <table className="table table-striped table-bordered align-middle">
              <thead className="table-light">
                <tr>
                  <th>出库单号</th>
                  <th>客户</th>
                  <th>平台</th>
                  <th>是否到仓</th>
                  <th>输入时间 (UTC)</th>
                  <th style={{ width: 160 }}>操作</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <React.Fragment key={o.id}>
                    <tr>
                      <td>{o.outboundOrderId}</td>
                      <td>{o.buyerName}</td>
                      <td>{o.buyingPlatformName}</td>
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
                        <td colSpan={6}>
                          <ItemsInlineTable items={itemsCache[o.id] || []} />
                        </td>
                      </tr>
                    ) : null}
                  </React.Fragment>
                ))}
                {orders.length === 0 ? (
                  <tr><td colSpan={6} className="text-center text-muted">暂无数据</td></tr>
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

