package educ.cit.villegas.inventory.service;

import educ.cit.villegas.event.SupplierOrderDelivered;
import educ.cit.villegas.inventory.events.StockChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class SupplierDeliveryListener {

    private final InventoryService inventoryService;
    private final ApplicationEventPublisher events;

    SupplierDeliveryListener(InventoryService inventoryService, ApplicationEventPublisher events) {
        this.inventoryService = inventoryService;
        this.events = events;
    }

    @EventListener
    public void handleSupplierOrderDelivered(SupplierOrderDelivered event) {
        var inventory = inventoryService.restock(event.productId(), event.units());
        events.publishEvent(new StockChangedEvent(
                event.productId().toString(),
                event.units(),
                inventory.getStock()));
    }
}
