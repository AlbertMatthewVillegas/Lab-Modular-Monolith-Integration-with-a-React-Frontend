package educ.cit.villegas.inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import educ.cit.villegas.inventory.entity.Inventory;

@Component("channelInventoryRepository")
public class InventoryRepository {
    private final educ.cit.villegas.inventory.repository.InventoryRepository delegate;
    public InventoryRepository(educ.cit.villegas.inventory.repository.InventoryRepository delegate) { this.delegate = delegate; }
    public List<Inventory> findAll() { return delegate.findAll(); }
    public Optional<Inventory> findById(String id) {
        try { return delegate.findById(UUID.fromString(id)); } catch (IllegalArgumentException e) { return Optional.empty(); }
    }
    public int deductIfAvailable(String id, int quantity) {
        if (quantity < 1) {
            return 0;
        }
        try {
            return delegate.deductIfAvailable(UUID.fromString(id), quantity);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
    public int addStock(String id, int quantity) {
        if (quantity < 1) {
            return 0;
        }
        try {
            return delegate.addStock(UUID.fromString(id), quantity);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
}
