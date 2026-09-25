package educ.cit.villegas.supplier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SupplierOrderRepository extends JpaRepository<SupplierOrder, Long> {

    Optional<SupplierOrder> findFirstByProductIdAndStatusIn(UUID productId, List<SupplierOrderStatus> statuses);

    List<SupplierOrder> findByStatusOrderByIdAsc(SupplierOrderStatus status);

    List<SupplierOrder> findByStatusInOrderByIdAsc(List<SupplierOrderStatus> statuses);

    List<SupplierOrder> findTop50ByOrderByIdDesc();
}
