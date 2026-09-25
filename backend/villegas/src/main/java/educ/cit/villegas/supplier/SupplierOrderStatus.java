package educ.cit.villegas.supplier;

/**
 * Our own status vocabulary for a supplier purchase order.
 * Never expose LegacySupply's raw StatusCode outside this module — map into this instead.
 */
public enum SupplierOrderStatus {
    PENDING,      // created locally, not yet successfully submitted to LegacySupply
    SUBMITTED,    // LegacySupply accepted it (their StatusCode 10 - Accepted)
    PICKING,      // StatusCode 20
    SHIPPED,      // StatusCode 30
    DELIVERED,    // StatusCode 40
    FAILED,       // we gave up submitting after retries, or a non-retryable error
    UNKNOWN       // LegacySupply returned a status code we don't recognize - log and review
}
