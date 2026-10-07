package educ.cit.villegas.shop.events;
import java.util.UUID;
public record OrderPlacedEvent(UUID orderId, int itemCount, int quantity) {}
