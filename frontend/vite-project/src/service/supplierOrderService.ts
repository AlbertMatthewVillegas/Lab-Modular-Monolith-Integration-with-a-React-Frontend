import type { SupplierOrder } from '../entity/SupplierOrder'

const apiUrl = 'http://localhost:8080'

export async function getSupplierOrders(): Promise<SupplierOrder[]> {
  const response = await fetch(`${apiUrl}/api/supplier-orders`)
  if (!response.ok) {
    throw new Error('The supplier orders could not be loaded.')
  }
  return response.json() as Promise<SupplierOrder[]>
}
