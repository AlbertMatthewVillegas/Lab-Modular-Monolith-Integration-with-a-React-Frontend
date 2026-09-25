export interface Notification {
  notificationId: string
  orderId: string | null
  productId: string | null
  message: string
  createdAt: string
}
