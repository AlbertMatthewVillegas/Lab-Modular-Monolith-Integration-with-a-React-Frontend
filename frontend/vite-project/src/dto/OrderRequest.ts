import type { OrderItemDto } from './OrderItemDto'

export interface OrderRequest {
  items: OrderItemDto[]
}
