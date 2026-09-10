import { FormEvent, useState } from 'react'

const products = [
  { id: 'P100', name: 'Wireless Mouse', stock: 25 },
  { id: 'P200', name: 'Mechanical Keyboard', stock: 10 },
  { id: 'P300', name: 'USB-C Hub', stock: 0 },
]

type OrderResult = {
  status: 'CONFIRMED' | 'REJECTED'
  reason: string
  inventory: number | null
}

const apiUrl =  'http://localhost:8080'

function App() {
  const [productId, setProductId] = useState(products[0].id)
  const [quantity, setQuantity] = useState(1)
  const [result, setResult] = useState<OrderResult | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')

  async function submitOrder(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSubmitting(true)
    setError('')
    setResult(null)

    try {
      const response = await fetch(`${apiUrl}/api/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId, quantity }),
      })

      if (!response.ok) {
        throw new Error('The order could not be submitted.')
      }

      setResult(await response.json() as OrderResult)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'The order could not be submitted.')
    } finally {
      setIsSubmitting(false)
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

          <form onSubmit={submitOrder} className="mt-12 grid max-w-md gap-2">
          <label className="mt-4 font-mono text-xs font-bold uppercase tracking-[0.12em] text-emerald-800/70" htmlFor="product">Product</label>
          <select className="w-full rounded-none border border-emerald-900/25 bg-stone-50 px-4 py-3 text-emerald-950 outline-none transition focus:border-orange-600 focus:ring-2 focus:ring-orange-300" id="product" value={productId} onChange={(event) => setProductId(event.target.value)}>
            {products.map((product) => (
              <option key={product.id} value={product.id}>
                {product.name} · {product.stock} available
              </option>
            ))}
          </select>

          <label className="mt-4 font-mono text-xs font-bold uppercase tracking-[0.12em] text-emerald-800/70" htmlFor="quantity">Quantity</label>
          <input
            className="w-full rounded-none border border-emerald-900/25 bg-stone-50 px-4 py-3 text-emerald-950 outline-none transition focus:border-orange-600 focus:ring-2 focus:ring-orange-300"
            id="quantity"
            type="number"
            min="1"
            value={quantity}
            onChange={(event) => setQuantity(Number(event.target.value))}
          />

          <button className="mt-5 rounded-none bg-orange-700 px-5 py-3 text-stone-50 transition hover:bg-orange-800 disabled:cursor-wait disabled:opacity-60" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Submitting...' : 'Submit order'}
          </button>
        </form>

        {result && (
          <div className={`mt-8 grid max-w-md gap-1 border-l-4 bg-stone-50 px-5 py-4 ${result.status === 'CONFIRMED' ? 'border-emerald-700' : 'border-red-700'}`} role="status">
            <span className="font-mono text-lg font-bold">{result.status}</span>
            <span className="text-emerald-950/80">{result.reason}</span>
            {result.inventory !== null && <span className="text-emerald-950/60">{result.inventory} remaining</span>}
          </div>
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
