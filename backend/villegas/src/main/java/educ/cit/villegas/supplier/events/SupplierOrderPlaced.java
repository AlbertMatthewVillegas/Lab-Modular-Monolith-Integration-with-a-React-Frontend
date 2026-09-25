package educ.cit.villegas.supplier.events;

public record SupplierOrderPlaced(String reference, String productId, int units) {
}
