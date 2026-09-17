package educ.cit.villegas.inventory.service;

import educ.cit.villegas.entity.Inventory;
import java.util.List;

public interface InventoryService {

	Inventory getItem(String productId);

	List<Inventory> getAllItems();

	Inventory reserve(String productId, int quantity);

	Inventory restock(String productId, int quantity);
}
