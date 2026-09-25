package educ.cit.villegas.supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class SupplierGatewayImpl implements SupplierGateway {

    private static final int MAX_ATTEMPTS = 3;
    private static final long[] BACKOFF_MILLIS = {500, 1500, 3000};

    private final SupplierOrderRepository repository;
    private final LegacySupplyXmlClient client;
    private final LegacySupplyTranslator translator;

    SupplierGatewayImpl(SupplierOrderRepository repository, LegacySupplyXmlClient client,
                         LegacySupplyTranslator translator) {
        this.repository = repository;
        this.client = client;
        this.translator = translator;
    }

    @Override
    @Transactional
    public SupplierOrderResult placeReorder(UUID productId, int unitsNeeded, String buyerRef) {
        // Idempotency guard: if we've already created a row for this buyerRef, reuse it
        // instead of creating a duplicate.
        SupplierOrder order = repository.findByBuyerRef(buyerRef).orElseGet(() -> {
            String sku = translator.skuFor(productId);
            int cases = translator.unitsToCases(sku, unitsNeeded);
            int actualUnits = translator.casesToUnits(sku, cases);
            String requestId = "REQ-" + UUID.randomUUID();
            SupplierOrder created = new SupplierOrder(productId, actualUnits, cases, buyerRef, requestId);
            return repository.save(created);
        });

        attemptSubmit(order);
        return order.toResult();
    }

    @Override
    public SupplierOrderStatus checkStatus(String poNumber) {
        int statusCode = client.checkStatus(poNumber);
        return translator.mapStatusCode(statusCode);
    }

    /**
     * Called both from placeReorder (first attempt) and from the scheduled retry job
     * (for orders still PENDING after an earlier failure/outage).
     */
    @Transactional
    void attemptSubmit(SupplierOrder order) {
        if (order.getStatus() != SupplierOrderStatus.PENDING) {
            return; // already submitted or terminal - nothing to do
        }

        // Pre-check: maybe a previous attempt actually succeeded server-side even though
        // we never recorded the response (e.g. crashed after LegacySupply accepted it).
        if (client.existsForBuyerRef(order.getBuyerRef())) {
            // We know it exists but not necessarily the PoNumber from this cheap check;
            // a fuller implementation would parse it out of existsForBuyerRef's response.
            // For now, leave PENDING for the next scheduled reconciliation pass if unsure.
        }

        String sku = translator.skuFor(order.getProductId());

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                var response = client.placeOrder(sku, order.getCases(), order.getBuyerRef(), order.getRequestId());
                SupplierOrderStatus status = translator.mapStatusCode(response.statusCode());
                order.markSubmitted(response.poNumber(), status);
                repository.save(order);
                return;

            } catch (LegacySupplyXmlClient.SessionExpiredException e) {
                // session was refreshed inside the client; just retry immediately, doesn't count
                // against the outage backoff budget as heavily, but still respects MAX_ATTEMPTS
                if (attempt == MAX_ATTEMPTS) {
                    order.markStatus(SupplierOrderStatus.FAILED);
                    repository.save(order);
                }
            } catch (LegacySupplyXmlClient.SupplierUnavailableException e) {
                if (attempt == MAX_ATTEMPTS) {
                    // leave as PENDING (not FAILED) - the scheduled job will keep trying;
                    // this is what satisfies "never lose a reorder" during an outage
                    return;
                }
                sleep(BACKOFF_MILLIS[attempt - 1]);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
