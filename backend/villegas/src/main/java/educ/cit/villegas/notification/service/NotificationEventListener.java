package educ.cit.villegas.notification.service;

import educ.cit.villegas.event.LowStock;
import educ.cit.villegas.event.OrderPlaced;
import educ.cit.villegas.event.OrderRejected;
import educ.cit.villegas.notification.entity.Notification;
import educ.cit.villegas.notification.repository.NotificationRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;

    public NotificationEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @EventListener
    public void handleOrderPlaced(OrderPlaced event) {
        notificationRepository.save(new Notification(
                event.order().getOrderId(),
                "Order " + event.order().getOrderId() + " confirmed"));
    }

    @EventListener
    public void handleOrderRejected(OrderRejected event) {
        notificationRepository.save(new Notification(
                event.order().getOrderId(),
                "Order " + event.order().getOrderId() + " rejected"));
    }

    @EventListener
    public void handleLowStock(LowStock event) {
        notificationRepository.save(new Notification(
                event.productId(),
                "Reorder needed: " + event.productId() + " has " + event.remainingStock() + " in stock"));
    }
}