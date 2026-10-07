package educ.cit.villegas.supplier;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
class ReorderService implements SupplierGateway {

    private static final Logger log = LoggerFactory.getLogger(ReorderService.class);

    private static final List<SupplierOrderStatus> ON_THE_WAY = SupplierOrderStatus.TRACKED;

    private final SupplierOrderRepository repository;
    private final LegacySupplyTranslator translator;
    private final ApplicationEventPublisher events;

    ReorderService(SupplierOrderRepository repository,
                   LegacySupplyTranslator translator,
                   ApplicationEventPublisher events) {
        this.repository = repository;
        this.translator = translator;
        this.events = events;
    }

    @Override
    @Transactional
    public synchronized ReorderResult requestReorder(String productId, int unitsNeeded) {
        if (!translator.knows(productId)) {
            log.warn("No supplier item for product {}, reorder skipped", productId);
            return new ReorderResult(null, productId, 0, SupplierOrderStatus.FAILED);
        }

        Optional<SupplierOrder> openOrder =
                repository.findFirstByProductIdAndStatusIn(UUID.fromString(productId), SupplierOrderStatus.OPEN);
        if (openOrder.isPresent()) {
            return openOrder.get().toResult();
        }

        int cases = translator.casesFor(productId, unitsNeeded);
        SupplierOrder order = repository.save(new SupplierOrder(productId, cases, unitsNeeded));
        order.assignBuyerRef();

        events.publishEvent(new ReorderSaved(order.getId()));
        return order.toResult();
    }

    @Override
    @Transactional(readOnly = true)
    public int unitsOnTheWay(String productId) {
        return repository.findByProductIdAndStatusIn(UUID.fromString(productId), ON_THE_WAY).stream()
                .mapToInt(SupplierOrder::getUnits)
                .sum();
    }

    @Override
    public Optional<String> supplierItemFor(String productId) {
        return translator.knows(productId) ? Optional.of(translator.supplierSku(productId)) : Optional.empty();
    }
}
