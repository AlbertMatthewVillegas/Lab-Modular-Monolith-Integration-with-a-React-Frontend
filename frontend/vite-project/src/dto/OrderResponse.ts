import type { Inventory } from '../entity/Inventory'
import type { OrderOutcome } from './OrderOutcome'

export interface OrderResponse {
  status: string
  reason: string | null
  items: OrderOutcome[]
  inventory: Inventory[]
}
