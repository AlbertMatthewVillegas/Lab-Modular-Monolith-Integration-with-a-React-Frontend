import { FormEvent, useEffect, useState } from 'react'

const initialProducts = [
  { id: 'P100', name: 'Wireless Mouse', stock: 25 },
  { id: 'P200', name: 'Mechanical Keyboard', stock: 10 },
  { id: 'P300', name: 'USB-C Hub', stock: 0 },
]

type Product = (typeof initialProducts)[number]

type CartItem = Product & {
  quantity: number
}

type OrderResult = {
  status: 'CONFIRMED' | 'REJECTED' | 'CANCELLED'
  reason: string
  items: { orderId: string | null; productId: string; outcome: string }[]
  inventory: number | null
}

type OrderHistoryEntry = {
  orderId: string
  productId: string
  quantity: number
  status: 'CONFIRMED' | 'REJECTED' | 'CANCELLED'
  reason: string | null
  createdAt: string
}

const apiUrl =  'http://localhost:8080'
const lowStockThreshold = 5

function App() {
  const [products, setProducts] = useState<Product[]>(initialProducts)
  const [cart, setCart] = useState<CartItem[]>([])
  const [result, setResult] = useState<OrderResult | null>(null)
  const [orderHistory, setOrderHistory] = useState<OrderHistoryEntry[]>([])
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [cancellingOrderId, setCancellingOrderId] = useState<string | null>(null)
  const [error, setError] = useState('')

  async function refreshData() {
    const [inventoryResponse, ordersResponse] = await Promise.all([
      fetch(`${apiUrl}/api/inventory`),
      fetch(`${apiUrl}/api/orders`),
    ])
    if (!inventoryResponse.ok || !ordersResponse.ok) {
      throw new Error('The latest inventory and order history could not be loaded.')
    }
    setProducts(await inventoryResponse.json() as Product[])
    setOrderHistory(await ordersResponse.json() as OrderHistoryEntry[])
  }

  useEffect(() => {
    refreshData().catch((requestError) => {
      setError(requestError instanceof Error ? requestError.message : 'The latest inventory and order history could not be loaded.')
    })
  }, [])

  function addToCart(product: Product) {
    setCart((currentCart) => {
      const existingItem = currentCart.find((item) => item.id === product.id)
      if (existingItem) {
        return currentCart.map((item) => item.id === product.id
          ? { ...item, quantity: Math.min(item.quantity + 1, product.stock) }
          : item)
      }
      return [...currentCart, { ...product, quantity: 1 }]
    })
    setResult(null)
    setError('')
  }

  function updateQuantity(productId: string, quantity: number) {
    setCart((currentCart) => currentCart.map((item) => item.id === productId
      ? { ...item, quantity: Math.max(1, Math.min(quantity, item.stock)) }
      : item))
  }

  function removeFromCart(productId: string) {
    setCart((currentCart) => currentCart.filter((item) => item.id !== productId))
  }

  async function submitOrder(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (cart.length === 0) {
      setError('Add at least one product to your cart.')
      return
    }
    setIsSubmitting(true)
    setError('')
    setResult(null)

    try {
      const response = await fetch(`${apiUrl}/api/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ items: cart.map(({ id, quantity }) => ({ productId: id, quantity })) }),
      })

      if (!response.ok) {
        throw new Error('The order could not be submitted.')
      }

      const orderResult = await response.json() as OrderResult
      setResult(orderResult)
      await refreshData()
      setCart([])
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'The order could not be submitted.')
    } finally {
      setIsSubmitting(false)
    }
  }

  async function cancelOrder(orderId: string) {
    setCancellingOrderId(orderId)
    setError('')

    try {
      const response = await fetch(`${apiUrl}/api/orders/${orderId}/cancel`, { method: 'POST' })
      if (!response.ok) {
        throw new Error(response.status === 409 ? 'This order is already cancelled.' : 'The order could not be cancelled.')
      }

      await refreshData()
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'The order could not be cancelled.')
    } finally {
      setCancellingOrderId(null)
    }
  }

  return (
    <main className="min-h-screen bg-stone-100 text-emerald-950 lg:grid lg:grid-cols-[minmax(0,1fr)_34%]">
      <section className="relative overflow-hidden px-6 py-12 sm:px-10 lg:px-[clamp(3rem,8vw,8rem)] lg:py-24" aria-labelledby="page-title">
        <div className="absolute -left-24 -top-24 h-64 w-64 rounded-full bg-orange-200/40 blur-3xl" />
        <div className="relative max-w-2xl">
          <div className="font-mono text-xs font-bold uppercase tracking-[0.16em] text-orange-700">Inventory desk / 01</div>
          <h1 id="page-title" className="mt-6 max-w-xl font-serif text-6xl font-normal leading-[0.9] tracking-tight sm:text-7xl lg:text-[clamp(4rem,8vw,7.5rem)]">Place an order.</h1>
          <p className="mt-5 text-lg text-emerald-800/70">Reserve available stock in the modular shop.</p>

          <div className="mt-12 max-w-xl">
            <div className="flex items-end justify-between border-b border-emerald-900/20 pb-3">
              <h2 className="font-mono text-xs font-bold uppercase tracking-[0.12em] text-emerald-800/70">Available products</h2>
              <span className="font-mono text-xs text-emerald-800/60">{cart.length} in cart</span>
            </div>
            <div className="overflow-x-auto border-b border-emerald-900/20">
              <table className="w-full min-w-120 text-left">
                <thead className="border-b border-emerald-900/15 font-mono text-xs uppercase tracking-[0.08em] text-emerald-800/60">
                  <tr>
                    <th className="py-3 pr-4 font-bold">Product</th>
                    <th className="py-3 pr-4 font-bold">ID</th>
                    <th className="py-3 pr-4 font-bold">Stock</th>
                    <th className="py-3 text-right font-bold">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-emerald-900/15">
              {products.map((product) => {
                const cartItem = cart.find((item) => item.id === product.id)
                const isAtLimit = cartItem?.quantity === product.stock
                const isLowStock = product.stock < lowStockThreshold
                return (
                  <tr className={product.stock === 0 ? 'bg-red-100/70' : isLowStock ? 'bg-amber-100/70' : ''} key={product.id}>
                    <td className="py-4 pr-4 font-serif text-xl">{product.name}</td>
                    <td className="py-4 pr-4 font-mono text-xs text-emerald-800/60">{product.id}</td>
                    <td className={`py-4 pr-4 font-mono text-sm ${isLowStock ? 'font-bold text-red-800' : ''}`}>
                      {product.stock}
                      {isLowStock && <span className="ml-2 text-xs uppercase tracking-[0.08em]">{product.stock === 0 ? 'out' : 'low'}</span>}
                    </td>
                    <td className="py-4 text-right">
                      <button
                        className="border border-orange-700 px-4 py-2 font-mono text-xs font-bold uppercase tracking-[0.08em] text-orange-700 transition hover:bg-orange-700 hover:text-stone-50 disabled:cursor-not-allowed disabled:border-emerald-900/20 disabled:text-emerald-900/30"
                        type="button"
                        onClick={() => addToCart(product)}
                        disabled={product.stock === 0 || isAtLimit}
                      >
                        {product.stock === 0 ? 'Out of stock' : isAtLimit ? 'At limit' : cartItem ? 'Add one' : 'Add'}
                      </button>
                    </td>
                  </tr>
                )
              })}
                </tbody>
              </table>
            </div>
          </div>

          <form onSubmit={submitOrder} className="mt-10 max-w-xl">
            <div className="flex items-end justify-between border-b border-emerald-900/20 pb-3">
              <h2 className="font-mono text-xs font-bold uppercase tracking-[0.12em] text-emerald-800/70">Your cart</h2>
              <span className="font-mono text-xs text-emerald-800/60">{cart.reduce((total, item) => total + item.quantity, 0)} items</span>
            </div>
            {cart.length === 0 ? (
              <p className="border-b border-emerald-900/20 py-6 text-emerald-800/60">Your cart is empty.</p>
            ) : (
              <div className="divide-y divide-emerald-900/15 border-b border-emerald-900/20">
                {cart.map((item) => (
                  <div className="grid grid-cols-[minmax(0,1fr)_auto_auto] items-center gap-3 py-4" key={item.id}>
                    <div>
                      <strong className="block font-serif text-xl font-normal">{item.name}</strong>
                      <span className="font-mono text-xs text-emerald-800/60">{item.id}</span>
                    </div>
                    <div className="flex items-center border border-emerald-900/25" aria-label={`Quantity for ${item.name}`}>
                      <button className="h-9 w-9 font-mono text-lg text-emerald-900 transition hover:bg-emerald-900 hover:text-stone-50" type="button" onClick={() => updateQuantity(item.id, item.quantity - 1)} aria-label={`Decrease ${item.name} quantity`}>−</button>
                      <input className="h-9 w-10 border-x border-emerald-900/25 bg-transparent text-center font-mono text-sm outline-none" type="number" min="1" max={item.stock} value={item.quantity} onChange={(event) => updateQuantity(item.id, Number(event.target.value) || 1)} aria-label={`${item.name} quantity`} />
                      <button className="h-9 w-9 font-mono text-lg text-emerald-900 transition hover:bg-emerald-900 hover:text-stone-50 disabled:opacity-30" type="button" onClick={() => updateQuantity(item.id, item.quantity + 1)} disabled={item.quantity >= item.stock} aria-label={`Increase ${item.name} quantity`}>+</button>
                    </div>
                    <button className="font-mono text-xs uppercase tracking-[0.08em] text-red-700 hover:underline" type="button" onClick={() => removeFromCart(item.id)}>Remove</button>
                  </div>
                ))}
              </div>
            )}
            <button className="mt-5 w-full rounded-none bg-orange-700 px-5 py-3 text-stone-50 transition hover:bg-orange-800 disabled:cursor-not-allowed disabled:opacity-50" type="submit" disabled={isSubmitting || cart.length === 0}>
              {isSubmitting ? 'Submitting...' : 'Submit order'}
            </button>
          </form>

        {result && (
          <div className={`mt-8 grid max-w-md gap-1 border-l-4 bg-stone-50 px-5 py-4 ${result.status === 'CONFIRMED' ? 'border-emerald-700' : 'border-red-700'}`} role="status">
            <span className="font-mono text-lg font-bold">{result.status}</span>
            <span className="text-emerald-950/80">{result.reason}</span>
            <div className="mt-2 grid gap-1 border-t border-emerald-900/10 pt-2">
              {result.items.map((item) => <span className="font-mono text-xs text-emerald-950/60" key={item.productId}>{item.productId} · {item.outcome}</span>)}
            </div>
            {result.inventory !== null && <span className="text-emerald-950/60">{result.inventory} remaining</span>}
          </div>
        )}
        {orderHistory.length > 0 && (
          <section className="mt-12 max-w-xl" aria-labelledby="order-history-title">
            <div className="flex items-end justify-between border-b border-emerald-900/20 pb-3">
              <h2 id="order-history-title" className="font-mono text-xs font-bold uppercase tracking-[0.12em] text-emerald-800/70">Order history</h2>
              <span className="font-mono text-xs text-emerald-800/60">{orderHistory.length} orders</span>
            </div>
            <div className="divide-y divide-emerald-900/15 border-b border-emerald-900/20">
              {orderHistory.map((order) => (
                <div className="flex items-center justify-between gap-4 py-4" key={order.orderId}>
                  <div>
                    <strong className="block font-serif text-xl font-normal">{products.find((product) => product.id === order.productId)?.name ?? order.productId}</strong>
                    <span className="font-mono text-xs text-emerald-800/60">{order.productId} · {order.quantity} · {order.status}</span>
                  </div>
                  <button
                    className="shrink-0 border border-red-700 px-4 py-2 font-mono text-xs font-bold uppercase tracking-[0.08em] text-red-700 transition hover:bg-red-700 hover:text-stone-50 disabled:cursor-not-allowed disabled:border-emerald-900/20 disabled:text-emerald-900/30"
                    type="button"
                    onClick={() => cancelOrder(order.orderId)}
                    disabled={order.status !== 'CONFIRMED' || cancellingOrderId === order.orderId}
                  >
                    {cancellingOrderId === order.orderId ? 'Cancelling...' : order.status === 'CONFIRMED' ? 'Cancel' : order.status}
                  </button>
                </div>
              ))}
            </div>
          </section>
        )}
        {error && <p className="mt-6 max-w-md text-red-700" role="alert">{error}</p>}
        </div>
      </section>
      <aside className="flex min-h-52 flex-col justify-between bg-emerald-900 p-6 text-stone-100 sm:p-10 lg:min-h-screen lg:p-12">
        <span className="font-mono text-xs font-bold uppercase tracking-[0.12em] text-lime-200">Supabase / Postgres</span>
        <strong className="max-w-xs font-serif text-3xl font-normal leading-none sm:text-4xl">Stock moves when the order is confirmed.</strong>
      </aside>
    </main>
  )
}

export default App
