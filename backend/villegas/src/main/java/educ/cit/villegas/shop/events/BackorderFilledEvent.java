package educ.cit.villegas.shop.events;

import java.util.UUID;

public record BackorderFilledEvent(UUID orderId) {
}
