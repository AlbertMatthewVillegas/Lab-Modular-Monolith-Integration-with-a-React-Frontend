package educ.cit.villegas.inventory;

import java.util.List;
import java.util.Optional;
import educ.cit.villegas.supplier.ReorderResult;

public interface InventoryService {

    List<InventoryView> listItems();

    Optional<InventoryView> getItem(String productId);

    ReservationResult reserve(String productId, int quantity);

    InventoryView restock(String productId, int quantity);

    int incomingUnits(String productId);

    ReorderResult reportShortage(String productId, int quantityWanted);
}
