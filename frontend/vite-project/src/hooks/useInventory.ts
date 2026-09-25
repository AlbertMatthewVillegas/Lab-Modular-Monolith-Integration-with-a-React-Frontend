import { useCallback, useEffect, useState } from 'react'
import { getInventory } from '../service/inventoryService'

export type Product = {
  id: string
  name: string
  price: number
  stock: number
}

export function useInventory() {
  const [products, setProducts] = useState<Product[]>([])
  const [error, setError] = useState('')

  const refreshInventory = useCallback(async () => {
    try {
      const inventory = await getInventory()
      setProducts(inventory.map(({ productId, name, price, stock }) => ({ id: productId, name, price, stock })))
      setError('')
    } catch (requestError) {
      const message = requestError instanceof Error ? requestError.message : 'The inventory could not be loaded.'
      setError(message)
      throw requestError
    }
  }, [])

  useEffect(() => {
    refreshInventory().catch(() => undefined)
  }, [refreshInventory])

  return { products, refreshInventory, error }
}
