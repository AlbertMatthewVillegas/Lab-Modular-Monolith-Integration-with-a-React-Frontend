package educ.cit.villegas.inventory.repository;

import educ.cit.villegas.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Inventory i
               set i.stock = i.stock - :quantity
             where i.productId = :productId
               and i.stock >= :quantity
            """)
    int deductIfAvailable(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Inventory i
               set i.stock = i.stock + :quantity
             where i.productId = :productId
            """)
    int addStock(@Param("productId") UUID productId, @Param("quantity") int quantity);
}
