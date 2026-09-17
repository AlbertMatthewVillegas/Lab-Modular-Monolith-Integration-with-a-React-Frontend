package educ.cit.villegas.event;

public record LowStock(String productId, int remainingStock, int threshold) {
}
