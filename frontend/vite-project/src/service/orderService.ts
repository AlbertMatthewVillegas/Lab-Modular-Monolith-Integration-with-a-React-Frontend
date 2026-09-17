import type { OrderRequest } from '../dto/OrderRequest'
import type { OrderResponse } from '../dto/OrderResponse'
import type { Order } from '../entity/Order'

const apiUrl = 'http://localhost:8080'

export async function getOrderHistory(): Promise<Order[]> {
  const response = await fetch(`${apiUrl}/api/orders`)
  if (!response.ok) {
    throw new Error('The order history could not be loaded.')
  }
  return response.json() as Promise<Order[]>
}

export async function placeOrder(request: OrderRequest): Promise<OrderResponse> {
  const response = await fetch(`${apiUrl}/api/orders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!response.ok) {
    throw new Error('The order could not be submitted.')
  }
  return response.json() as Promise<OrderResponse>
}

export async function cancelOrder(orderId: string): Promise<OrderResponse> {
  const response = await fetch(`${apiUrl}/api/orders/${orderId}/cancel`, {
    method: 'POST',
  })
  if (!response.ok) {
    throw new Error(response.status === 409 ? 'This order is already cancelled.' : 'The order could not be cancelled.')
  }
  return response.json() as Promise<OrderResponse>
}
