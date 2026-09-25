package educ.cit.villegas.supplier;

import java.util.UUID;

/**
 * What SupplierGateway hands back to Inventory/Order code.
 * Contains only our own vocabulary - no SupplierSku, no LegacySupply status codes, no XML.
 */
public record SupplierOrderResult(
        UUID supplierOrderId,   // our own supplier_orders.id
        String buyerRef,
        String poNumber,        // null if not yet submitted (still PENDING)
        SupplierOrderStatus status
) {
}
