package educ.cit.villegas.inventory.events;

public record StockChangedEvent(String productId, int change, int stock) {
}
