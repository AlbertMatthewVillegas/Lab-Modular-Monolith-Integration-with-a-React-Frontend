package educ.cit.villegas.supplier;

import java.util.UUID;

/**
 * Anti-Corruption Layer boundary. This is the ONLY public type Inventory/Order
 * code may depend on from this module. Everything else (XML classes, SupplierSku,
 * PackSize, session handling, LegacySupply status codes) stays package-private.
 */
public interface SupplierGateway {

    /**
     * Creates a PENDING supplier_orders row and attempts to submit it to LegacySupply.
     * If submission fails (timeout, outage, rate limit), the row stays PENDING and a
     * scheduled job will retry later - this call does not throw for that case.
     *
     * @param productId    our own inventory product id
     * @param unitsNeeded  how many of our units we need restocked
     * @param buyerRef     our own unique reference for this reorder, e.g. "RO-<supplier_orders.id>"
     */
    SupplierOrderResult placeReorder(UUID productId, int unitsNeeded, String buyerRef);

    /**
     * Looks up current status for an already-submitted order, translated into our enum.
     */
    SupplierOrderStatus checkStatus(String poNumber);
}
