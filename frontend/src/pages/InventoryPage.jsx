import React from 'react'
import { api, apiErrorMessage } from '../api/client'
import { formatUtc } from '../utils/utc'

const STATUS_LABEL = {
  INBOUND: '入库中',
  IN_TRANSITION: '转运中',
  OUTBOUND: '已售'
}

function money(v) {
  if (v == null || v === '') return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return n.toFixed(2)
}

export default function InventoryPage() {
  const [rows, setRows] = React.useState([])
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState(null)
  const [q, setQ] = React.useState('')
  const [selected, setSelected] = React.useState(null)
  const [detailItems, setDetailItems] = React.useState([])
  const [detailLoading, setDetailLoading] = React.useState(false)

  async function refresh() {
    setLoading(true)
    try {
      const res = await api.get('/inventory')
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

  async function openDetail(row) {
    setSelected(row)
    setDetailLoading(true)
    try {
      const res = await api.get(`/inventory/${row.id}/items`)
      setDetailItems(res.data || [])
    } catch (e) {
      setError(apiErrorMessage(e))
      setDetailItems([])
    } finally {
      setDetailLoading(false)
    }
  }

  const filtered = rows.filter((r) => {
    const s = q.trim().toLowerCase()
    if (!s) return true
    return (
      String(r.productId || '').toLowerCase().includes(s) ||
      String(r.productName || '').toLowerCase().includes(s) ||
      String(r.productCategoryName || '').toLowerCase().includes(s) ||
      String(r.manufacturingName || '').toLowerCase().includes(s)
    )
  })

  const totals = filtered.reduce(
    (acc, r) => {
      acc.inbound += r.inboundCount
      acc.transit += r.inTransitionCount
      acc.sold += r.soldCount
      acc.onHand += r.onHandCount
      return acc
    },
    { inbound: 0, transit: 0, sold: 0, onHand: 0 }
  )

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between mb-3 gap-2 flex-wrap">
        <div>
          <h4 className="mb-0">库存总览</h4>
          <div className="text-muted small mt-1">
            按商品汇总：当前库存 = 入库中 + 转运中；已售 = 出库完成件数
          </div>
        </div>
        <div className="d-flex gap-2">
          <input
            className="form-control form-control-sm"
            style={{ width: 240 }}
            placeholder="搜索商品/品类..."
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
          <button className="btn btn-outline-secondary btn-sm" onClick={refresh} disabled={loading}>
            刷新
          </button>
        </div>
      </div>

      {error ? <div className="alert alert-danger">{error}</div> : null}

      <div className="table-responsive">
        <table className="table table-sm table-hover align-middle">
          <thead>
            <tr>
              <th>商品编码</th>
              <th>商品名称</th>
              <th>品类</th>
              <th className="text-end">入库中</th>
              <th className="text-end">转运中</th>
              <th className="text-end">当前库存</th>
              <th className="text-end">已售</th>
              <th className="text-end">累计件数</th>
              <th className="text-end">库存成本</th>
              <th className="text-end">已售收入</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((r) => (
              <tr key={r.id} className={selected?.id === r.id ? 'table-active' : ''}>
                <td><code>{r.productId}</code></td>
                <td>{r.productName}</td>
                <td>{r.productCategoryName}</td>
                <td className="text-end">{r.inboundCount}</td>
                <td className="text-end">{r.inTransitionCount}</td>
                <td className="text-end fw-semibold">{r.onHandCount}</td>
                <td className="text-end">{r.soldCount}</td>
                <td className="text-end">{r.totalCount}</td>
                <td className="text-end">{money(r.onHandCost)}</td>
                <td className="text-end">{money(r.soldRevenue)}</td>
                <td className="text-end">
                  <button className="btn btn-link btn-sm p-0" onClick={() => openDetail(r)}>
                    明细
                  </button>
                </td>
              </tr>
            ))}
            {!loading && filtered.length === 0 ? (
              <tr>
                <td colSpan={11} className="text-muted text-center py-4">暂无商品库存数据</td>
              </tr>
            ) : null}
          </tbody>
          {filtered.length > 0 ? (
            <tfoot>
              <tr className="table-light">
                <td colSpan={3} className="fw-semibold">合计（筛选后）</td>
                <td className="text-end fw-semibold">{totals.inbound}</td>
                <td className="text-end fw-semibold">{totals.transit}</td>
                <td className="text-end fw-semibold">{totals.onHand}</td>
                <td className="text-end fw-semibold">{totals.sold}</td>
                <td colSpan={4}></td>
              </tr>
            </tfoot>
          ) : null}
        </table>
      </div>

      {selected ? (
        <div className="mt-4">
          <div className="d-flex align-items-center justify-content-between mb-2">
            <h5 className="mb-0">
              明细：{selected.productName} <span className="text-muted">({selected.productId})</span>
            </h5>
            <button className="btn btn-sm btn-outline-secondary" onClick={() => { setSelected(null); setDetailItems([]) }}>
              关闭
            </button>
          </div>
          {detailLoading ? <div className="text-muted">加载中...</div> : null}
          <div className="table-responsive">
            <table className="table table-sm table-bordered align-middle">
              <thead>
                <tr>
                  <th>Item ID</th>
                  <th>状态</th>
                  <th className="text-end">成本</th>
                  <th className="text-end">标价</th>
                  <th className="text-end">实售价</th>
                  <th>入库单</th>
                  <th>转运单</th>
                  <th>出库单</th>
                  <th>输入时间 (UTC)</th>
                  <th>备注</th>
                </tr>
              </thead>
              <tbody>
                {detailItems.map((it) => (
                  <tr key={it.id}>
                    <td>{it.id}</td>
                    <td>{STATUS_LABEL[it.itemStatus] || it.itemStatus}</td>
                    <td className="text-end">{money(it.actualCost)}</td>
                    <td className="text-end">{money(it.itemSellingPrice)}</td>
                    <td className="text-end">{money(it.itemActualSellingPrice)}</td>
                    <td>{it.inboundOrderId ?? '-'}</td>
                    <td>{it.transitionShipmentId ?? '-'}</td>
                    <td>{it.outboundOrderId ?? '-'}</td>
                    <td className="small">{formatUtc(it.inputTime)}</td>
                    <td style={{ maxWidth: 220 }}>{it.note || '-'}</td>
                  </tr>
                ))}
                {!detailLoading && detailItems.length === 0 ? (
                  <tr>
                    <td colSpan={10} className="text-muted text-center">该商品还没有 item 记录</td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        </div>
      ) : null}
    </div>
  )
}
