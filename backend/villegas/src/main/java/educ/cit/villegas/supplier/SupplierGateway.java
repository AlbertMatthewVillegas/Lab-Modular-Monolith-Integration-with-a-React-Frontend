package educ.cit.villegas.supplier;
import java.util.Optional;
public interface SupplierGateway {

    ReorderResult requestReorder(String productId, int unitsNeeded);

    int unitsOnTheWay(String productId);

    Optional<String> supplierItemFor(String productId);
}
