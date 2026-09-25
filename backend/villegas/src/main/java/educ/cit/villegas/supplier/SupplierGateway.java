package educ.cit.villegas.supplier;

public interface SupplierGateway {

    ReorderResult requestReorder(String productId, int unitsNeeded);
}
