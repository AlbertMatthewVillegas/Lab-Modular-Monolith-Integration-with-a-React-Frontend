package educ.cit.villegas.shop.events;
import java.util.UUID;
public record OrderCancelledEvent(UUID orderId, int itemCount, int quantity) {}
