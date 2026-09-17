package educ.cit.villegas.inventory.service;

import educ.cit.villegas.inventory.entity.Inventory;
import java.util.List;
import java.util.UUID;

public interface InventoryService {

	Inventory getItem(UUID productId);

	List<Inventory> getAllItems();

	Inventory reserve(UUID productId, int quantity);

	Inventory restock(UUID productId, int quantity);
}
