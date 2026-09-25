package educ.cit.villegas.inventory.repository;

import educ.cit.villegas.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
}
