package educ.cit.villegas.shop;
import java.util.List;
import java.util.UUID;
import educ.cit.villegas.inventory.InventoryView;
public record OrderResponse(UUID orderId, String status, String reason, List<ItemOutcome> items, List<InventoryView> inventory) {
    public record ItemOutcome(String productId, int quantity, String status) {
        public static final String RESERVED = "RESERVED", NOT_RESERVED = "NOT_RESERVED", INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    }
}
