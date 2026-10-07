package educ.cit.villegas.inventory.events;
public record LowStockEvent(String productId, String name, int stock, int threshold) {}
