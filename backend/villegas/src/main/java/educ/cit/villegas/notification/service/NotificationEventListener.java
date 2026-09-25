package educ.cit.villegas.notification.service;

import educ.cit.villegas.event.LowStock;
import educ.cit.villegas.event.OrderPlaced;
import educ.cit.villegas.event.OrderRejected;
import educ.cit.villegas.notification.entity.Notification;
import educ.cit.villegas.notification.repository.NotificationRepository;
import educ.cit.villegas.supplier.SupplierGateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationEventListener {

    @Value("${reorder.par-level:50}")
    private int parLevel;

    private final NotificationRepository notificationRepository;
    private final SupplierGateway supplierGateway;

    public NotificationEventListener(
        NotificationRepository notificationRepository,
        SupplierGateway supplierGateway
    ) {
        this.notificationRepository = notificationRepository;
        this.supplierGateway = supplierGateway;
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
        int unitsNeeded = parLevel - event.remainingStock();
        if (unitsNeeded <= 0) {
            return; // remainingStock already at/above par - nothing to reorder
        }

        String buyerRef = "RO-" + UUID.randomUUID();
        supplierGateway.placeReorder(event.productId(), unitsNeeded, buyerRef);
    }
}