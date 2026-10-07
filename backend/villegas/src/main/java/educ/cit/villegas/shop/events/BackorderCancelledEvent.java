package educ.cit.villegas.shop.events;

import java.util.UUID;

public record BackorderCancelledEvent(UUID orderId, String reason) {
}
