import type { Notification } from '../entity/Notification'

const apiUrl = 'http://localhost:8080'

export async function getNotifications(): Promise<Notification[]> {
  const response = await fetch(`${apiUrl}/api/notifications`)
  if (!response.ok) {
    throw new Error('The notifications could not be loaded.')
  }
  return response.json() as Promise<Notification[]>
}
