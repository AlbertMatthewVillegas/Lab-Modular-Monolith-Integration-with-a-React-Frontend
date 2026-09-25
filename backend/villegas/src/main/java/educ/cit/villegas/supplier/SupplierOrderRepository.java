package educ.cit.villegas.supplier;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SupplierOrderRepository extends JpaRepository<SupplierOrder, UUID> {

    Optional<SupplierOrder> findByBuyerRef(String buyerRef);

    List<SupplierOrder> findByStatus(SupplierOrderStatus status);

    List<SupplierOrder> findByStatusIn(List<SupplierOrderStatus> statuses);
}
