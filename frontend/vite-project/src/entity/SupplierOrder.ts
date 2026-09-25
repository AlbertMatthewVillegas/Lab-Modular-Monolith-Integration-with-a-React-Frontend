export type SupplierOrderStatus = 'PENDING' | 'SENT' | 'DELIVERED' | 'STOPPED'

export interface SupplierOrder {
  reference: string
  productId: string
  units: number
  status: SupplierOrderStatus
  createdAt: string
  updatedAt: string
}
