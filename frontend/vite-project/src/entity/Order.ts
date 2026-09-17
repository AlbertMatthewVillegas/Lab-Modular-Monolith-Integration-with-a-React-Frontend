import type { OrderItem } from './OrderItem'

export interface Order {
  orderId: string
  status: string
  reason: string | null
  createdAt: string
  items: OrderItem[]
}
