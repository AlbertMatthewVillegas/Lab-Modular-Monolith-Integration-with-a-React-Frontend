package educ.cit.villegas.inventory.service;

import educ.cit.villegas.entity.Inventory;
import educ.cit.villegas.inventory.repository.InventoryRepository;
import educ.cit.villegas.event.LowStock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final int lowStockThreshold;

    InventoryServiceImpl(InventoryRepository inventoryRepository,
                         ApplicationEventPublisher eventPublisher,
                         @Value("${inventory.low-stock-threshold:5}") int lowStockThreshold) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublisher = eventPublisher;
        this.lowStockThreshold = lowStockThreshold;
    }

    @Override
    public Inventory getItem(String productId) {
        return inventoryRepository.findById(productId).orElse(null);
    }

    @Override
    public List<Inventory> getAllItems() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional
    public Inventory reserve(String productId, int quantity) {
        Inventory item = getItem(productId);
        if (item == null || quantity <= 0 || item.getStock() < quantity) {
            return null;
        }

        item.setStock(item.getStock() - quantity);
        Inventory savedItem = inventoryRepository.save(item);
        if (savedItem.getStock() < lowStockThreshold) {
            eventPublisher.publishEvent(new LowStock(savedItem.getProductId(), savedItem.getStock(), lowStockThreshold));
        }
        return savedItem;
    }

    @Override
    @Transactional
    public Inventory restock(String productId, int quantity) {
        Inventory item = getItem(productId);
        if (item == null || quantity <= 0) {
            return null;
        }

        item.setStock(item.getStock() + quantity);
        return inventoryRepository.save(item);
    }
}
