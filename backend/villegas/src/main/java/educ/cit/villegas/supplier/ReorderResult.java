package educ.cit.villegas.supplier;

public record ReorderResult(String reference, String productId, int unitsOrdered, SupplierOrderStatus status) {
}
