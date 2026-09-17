import type { Inventory } from '../entity/Inventory'

const apiUrl = 'http://localhost:8080'

export async function getInventory(): Promise<Inventory[]> {
  const response = await fetch(`${apiUrl}/api/inventory`)
  if (!response.ok) {
    throw new Error('The inventory could not be loaded.')
  }
  return response.json() as Promise<Inventory[]>
}
