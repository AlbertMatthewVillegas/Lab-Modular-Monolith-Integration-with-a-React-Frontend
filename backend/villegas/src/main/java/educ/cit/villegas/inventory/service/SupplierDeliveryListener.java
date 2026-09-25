package educ.cit.villegas.inventory.service;

import educ.cit.villegas.event.SupplierOrderDelivered;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class SupplierDeliveryListener {

    private final InventoryService inventoryService;

    SupplierDeliveryListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @EventListener
    public void handleSupplierOrderDelivered(SupplierOrderDelivered event) {
        inventoryService.restock(event.productId(), event.units());
    }
}
