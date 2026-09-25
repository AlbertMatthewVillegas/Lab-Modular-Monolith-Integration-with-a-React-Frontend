package educ.cit.villegas.supplier;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Package-private on purpose: nothing outside the supplier module should touch this
 * entity directly. Inventory/Order only ever see SupplierOrderResult via SupplierGateway.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "supplier_orders")
class SupplierOrder {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private UUID productId;

    private String buyerRef;

    /** Persisted so retries - even across app restarts - reuse the same X-Request-Id. */
    private String requestId;

    /** Null until LegacySupply accepts the order. */
    private String poNumber;

    private int cases;
    private int units;

    @Enumerated(EnumType.STRING)
    private SupplierOrderStatus status;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    SupplierOrder(UUID productId, int units, int cases, String buyerRef, String requestId) {
        this.productId = productId;
        this.units = units;
        this.cases = cases;
        this.buyerRef = buyerRef;
        this.requestId = requestId;
        this.status = SupplierOrderStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    void markSubmitted(String poNumber, SupplierOrderStatus status) {
        this.poNumber = poNumber;
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    void markStatus(SupplierOrderStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    SupplierOrderResult toResult() {
        return new SupplierOrderResult(id, buyerRef, poNumber, status);
    }
}
