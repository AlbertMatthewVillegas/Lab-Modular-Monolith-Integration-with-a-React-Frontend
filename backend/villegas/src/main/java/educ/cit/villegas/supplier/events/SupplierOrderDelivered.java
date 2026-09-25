package educ.cit.villegas.supplier.events;

public record SupplierOrderDelivered(String reference, String productId, int units) {
}
