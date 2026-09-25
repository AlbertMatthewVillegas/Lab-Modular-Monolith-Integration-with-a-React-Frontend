import type { SupplierOrder } from '../entity/SupplierOrder'

type SupplierOrdersProps = {
  orders: SupplierOrder[]
  names: Record<string, string>
}

function clock(iso: string) {
  return new Date(iso).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

export function SupplierOrders({ orders, names }: SupplierOrdersProps) {
  return (
    <section className="mt-12 max-w-2xl" aria-labelledby="supplier-orders-title">
      <div className="flex items-end justify-between border-b border-emerald-900/20 pb-3">
        <h2 id="supplier-orders-title" className="font-mono text-xs font-bold uppercase tracking-[0.12em] text-emerald-800/70">
          Supplier reorders
        </h2>
        {orders.length > 0 && <span className="font-mono text-xs text-emerald-800/60">Newest first</span>}
      </div>
      {orders.length === 0 ? (
        <p className="border-b border-emerald-900/20 py-6 text-emerald-800/60">
          Reorders sent for low-stock products will appear here.
        </p>
      ) : (
        <div className="overflow-x-auto border-b border-emerald-900/20">
          <table className="w-full min-w-[38rem] text-left">
            <thead className="border-b border-emerald-900/15 font-mono text-xs uppercase tracking-[0.08em] text-emerald-800/60">
              <tr>
                <th className="py-3 pr-4 font-bold">Reorder</th>
                <th className="py-3 pr-4 font-bold">Product</th>
                <th className="py-3 pr-4 text-right font-bold">Units</th>
                <th className="py-3 pr-4 font-bold">Status</th>
                <th className="py-3 text-right font-bold">Updated</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-emerald-900/15">
              {orders.map((order) => (
                <tr key={order.reference}>
                  <td className="py-4 pr-4 font-mono text-xs">{order.reference}</td>
                  <td className="py-4 pr-4">{names[order.productId] ?? order.productId}</td>
                  <td className="py-4 pr-4 text-right font-mono text-sm">{order.units}</td>
                  <td className="py-4 pr-4 font-mono text-xs uppercase">{order.status}</td>
                  <td className="py-4 text-right font-mono text-xs text-emerald-800/60">
                    <time dateTime={order.updatedAt}>{clock(order.updatedAt)}</time>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
