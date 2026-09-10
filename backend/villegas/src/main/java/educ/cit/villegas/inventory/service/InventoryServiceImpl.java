package educ.cit.villegas.inventory.service;

import educ.cit.villegas.entity.Inventory;
import educ.cit.villegas.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Inventory getItem(String productId) {
        return inventoryRepository.findById(productId).orElse(null);
    }

    @Override
    @Transactional
    public Inventory reserve(String productId, int quantity) {
        Inventory item = getItem(productId);
        if (item == null || quantity <= 0 || item.getStock() < quantity) {
            return null;
        }

        item.setStock(item.getStock() - quantity);
        return inventoryRepository.save(item);
    }
}