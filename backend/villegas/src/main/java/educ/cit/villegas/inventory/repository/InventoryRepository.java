package educ.cit.villegas.inventory.repository;

import educ.cit.villegas.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
}
