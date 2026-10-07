package educ.cit.villegas.shop.events;
import java.util.UUID;
public record OrderRejectedEvent(UUID orderId, String reason) {}
