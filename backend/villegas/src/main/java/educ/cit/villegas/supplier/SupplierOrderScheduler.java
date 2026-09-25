package educ.cit.villegas.supplier;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
class SupplierOrderScheduler {

    private final SupplierOrderRepository repository;
    private final SupplierGatewayImpl gateway;
    private final LegacySupplyXmlClient client;
    private final LegacySupplyTranslator translator;
    private final ApplicationEventPublisher eventPublisher;

    SupplierOrderScheduler(SupplierOrderRepository repository, SupplierGatewayImpl gateway,
                            LegacySupplyXmlClient client, LegacySupplyTranslator translator,
                            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.client = client;
        this.translator = translator;
        this.eventPublisher = eventPublisher;
    }

    /** Never-lose-a-reorder: retries anything still PENDING (e.g. created during an outage). */
    @Scheduled(fixedDelay = 30_000)
    void retryPendingOrders() {
        List<SupplierOrder> pending = repository.findByStatus(SupplierOrderStatus.PENDING);
        for (SupplierOrder order : pending) {
            gateway.attemptSubmit(order);
        }
    }

    /** Delivery tracking: polls open orders, maps status, publishes an event on delivery. */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    void pollOpenOrders() {
        List<SupplierOrder> open = repository.findByStatusIn(List.of(
                SupplierOrderStatus.SUBMITTED,
                SupplierOrderStatus.PICKING,
                SupplierOrderStatus.SHIPPED
        ));

        for (SupplierOrder order : open) {
            if (order.getPoNumber() == null) continue;

            try {
                int statusCode = client.checkStatus(order.getPoNumber());
                SupplierOrderStatus newStatus = translator.mapStatusCode(statusCode);

                if (newStatus != order.getStatus()) {
                    order.markStatus(newStatus);
                    repository.save(order);

                    if (newStatus == SupplierOrderStatus.DELIVERED) {
                        // TODO: replace with your Lab 2 event type, e.g. StockDeliveredEvent
                        eventPublisher.publishEvent(
                                new SupplierOrderDeliveredEvent(order.getProductId(), order.getUnits())
                        );
                    } else if (newStatus == SupplierOrderStatus.UNKNOWN) {
                        // TODO: document this branch's handling in INTEGRATION.md
                    }
                }
            } catch (LegacySupplyXmlClient.SupplierUnavailableException e) {
                // skip this order this cycle, try again next poll
            }
        }
    }

    /**
     * Placeholder event - wire this into your existing Lab 2 notification/event module,
     * or replace with whatever event type Inventory already listens for.
     */
    record SupplierOrderDeliveredEvent(java.util.UUID productId, int units) {
    }
}
