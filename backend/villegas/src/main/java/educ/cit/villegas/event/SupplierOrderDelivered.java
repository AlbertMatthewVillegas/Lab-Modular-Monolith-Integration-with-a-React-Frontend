package educ.cit.villegas.event;

import java.util.UUID;

public record SupplierOrderDelivered(UUID productId, int units) {
}
