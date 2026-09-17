package educ.cit.villegas.event;

import java.util.UUID;

public record LowStock(UUID productId, int remainingStock, int threshold) {
}
