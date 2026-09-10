package educ.cit.villegas.inventory.service;

import educ.cit.villegas.entity.Inventory;

public interface InventoryService {

	Inventory getItem(String productId);

	Inventory reserve(String productId, int quantity);
}
