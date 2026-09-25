import type { Order } from './Order'
import type { OrderItemId } from './OrderItemId'

export interface OrderItem {
  id: OrderItemId
  order?: Order
  productId: string
  price: number
  quantity: number
}
