import { type FormEvent, useCallback, useEffect, useState } from 'react'
import { useInventory, type Product } from './useInventory'
import { cancelOrder as cancelOrderRequest, getOrderHistory, placeOrder } from '../service/orderService'
import type { Inventory } from '../entity/Inventory'
import { getNotifications } from '../service/notificationService'
import type { Notification } from '../entity/Notification'
import { getSupplierOrders } from '../service/supplierOrderService'
import type { SupplierOrder } from '../entity/SupplierOrder'

type CartItem = Product & {
  quantity: number
}

type OrderResult = {
  status: 'CONFIRMED' | 'REJECTED' | 'CANCELLED'
  reason: string
  items: { orderId: string | null; productId: string; outcome: string }[]
  inventory: Inventory[]
}

type OrderHistoryEntry = {
  orderId: string
  status: 'CONFIRMED' | 'REJECTED' | 'CANCELLED'
  reason: string | null
  createdAt: string
  items: { productId: string; quantity: number }[]
}

export function useShop() {
  const { products, refreshInventory, error: inventoryError } = useInventory()
  const [cart, setCart] = useState<CartItem[]>([])
  const [result, setResult] = useState<OrderResult | null>(null)
  const [orderHistory, setOrderHistory] = useState<OrderHistoryEntry[]>([])
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [supplierOrders, setSupplierOrders] = useState<SupplierOrder[]>([])
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [cancellingOrderId, setCancellingOrderId] = useState<string | null>(null)
  const [error, setError] = useState('')

  const refreshData = useCallback(async () => {
    const [, orders, activity, reorders] = await Promise.all([
      refreshInventory(),
      getOrderHistory(),
      getNotifications(),
      getSupplierOrders(),
    ])
    setOrderHistory(orders as OrderHistoryEntry[])
    setNotifications(activity)
    setSupplierOrders(reorders)
  }, [refreshInventory])

  useEffect(() => {
    refreshData().catch((requestError) => {
      setError(requestError instanceof Error ? requestError.message : 'The latest inventory and order history could not be loaded.')
    })
  }, [refreshData])

  useEffect(() => {
    if (inventoryError) {
      setError(inventoryError)
    }
  }, [inventoryError])

  function addToCart(product: Product) {
    setCart((currentCart) => {
      const existingItem = currentCart.find((item) => item.id === product.id)
      if (existingItem) {
        return currentCart.map((item) => item.id === product.id
          ? { ...item, quantity: Math.min(item.quantity + 1, Math.max(product.stock, 1)) }
          : item)
      }
      return [...currentCart, { ...product, quantity: 1 }]
    })
    setResult(null)
    setError('')
  }

  function updateQuantity(productId: string, quantity: number) {
    setCart((currentCart) => currentCart.map((item) => item.id === productId
      ? { ...item, quantity: Math.max(1, Math.min(quantity, Math.max(item.stock, 1))) }
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
      const orderResult = await placeOrder({
        items: cart.map(({ id, name, quantity }) => ({ productId: id, name, quantity })),
      })
      setResult(orderResult as unknown as OrderResult)
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
      await cancelOrderRequest(orderId)
      await refreshData()
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'The order could not be cancelled.')
    } finally {
      setCancellingOrderId(null)
    }
  }

  return {
    products,
    cart,
    result,
    orderHistory,
    notifications,
    supplierOrders,
    isSubmitting,
    cancellingOrderId,
    error,
    addToCart,
    updateQuantity,
    removeFromCart,
    submitOrder,
    cancelOrder,
  }
}
