package educ.cit.villegas.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import educ.cit.villegas.channel.TianggeJson.Decision;
import educ.cit.villegas.channel.TianggeJson.Resolution;
import educ.cit.villegas.channel.TianggeJson.StockEntry;
import educ.cit.villegas.inventory.InventoryService;
import educ.cit.villegas.inventory.InventoryView;
import educ.cit.villegas.shop.OrderService;
import educ.cit.villegas.shop.OrderSummary;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
class TianggeOutbox {

    private static final Logger log = LoggerFactory.getLogger(TianggeOutbox.class);

    private static final long MAX_BACKOFF_MS = 10_000;

    private final ScheduledExecutorService replySender = Executors.newScheduledThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "tiangge-outbox");
        thread.setDaemon(true);
        return thread;
    });
    private final ScheduledExecutorService stockSender = Executors.newScheduledThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "tiangge-stock-outbox");
        thread.setDaemon(true);
        return thread;
    });

    private final TianggeClient client;
    private final TianggeTranslator translator;
    private final ChannelOrderRepository orders;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final TransactionTemplate transaction;

    private final Set<String> queuedReplies = ConcurrentHashMap.newKeySet();
    private final Set<String> listedSkus = ConcurrentHashMap.newKeySet();

    TianggeOutbox(TianggeClient client,
                  TianggeTranslator translator,
                  ChannelOrderRepository orders,
                  InventoryService inventoryService,
                  OrderService orderService,
                  TransactionTemplate transaction) {
        this.client = client;
        this.translator = translator;
        this.orders = orders;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.transaction = transaction;
    }

    @PreDestroy
    void stop() {
        replySender.shutdownNow();
        stockSender.shutdownNow();
    }

    void decisionReady(String tianggeOrderId) {
        queue("decision:" + tianggeOrderId, () -> sendDecision(tianggeOrderId, 1));
    }

    void resolutionReady(String tianggeOrderId) {
        queue("resolution:" + tianggeOrderId, () -> sendResolution(tianggeOrderId, 1));
    }

    void cancellationReady(String tianggeOrderId) {
        queue("cancellation:" + tianggeOrderId, () -> confirmCancellation(tianggeOrderId, 1));
    }

    private void queue(String key, Runnable task) {
        if (queuedReplies.add(key)) {
            replySender.execute(task);
        }
    }

    @Scheduled(initialDelay = 10_000, fixedDelay = 15_000)
    public void resendUnsentReplies() {
        if (listedSkus.isEmpty()) {
            return;
        }
        orders.findUnsentDecisions().forEach(this::decisionReady);
        orders.findUnsentResolutions().forEach(this::resolutionReady);
        orders.findUnconfirmedCancellations().forEach(this::cancellationReady);
    }

    private void sendDecision(String tianggeOrderId, int attempt) {
        ChannelOrder order = orders.findById(tianggeOrderId).orElse(null);
        if (order == null || order.isDecisionSent()) {
            queuedReplies.remove("decision:" + tianggeOrderId);
            return;
        }
        String shopOrderId = order.getShopOrderId() == null ? "NONE" : order.getShopOrderId().toString();
        Decision decision = new Decision(order.getDecision().name(), shopOrderId, order.getReason());
        send("decision:" + tianggeOrderId, "decision " + decision.decision() + " for " + tianggeOrderId, attempt,
                () -> client.decide(tianggeOrderId, decision),
                Set.of("decision_conflict", "order_not_found", "invalid_request"),
                () -> update(tianggeOrderId, ChannelOrder::decisionSent),
                next -> sendDecision(tianggeOrderId, next));
    }

    private void sendResolution(String tianggeOrderId, int attempt) {
        ChannelOrder order = orders.findById(tianggeOrderId).orElse(null);
        if (order == null || order.getResolution() == null || order.isResolutionSent()) {
            queuedReplies.remove("resolution:" + tianggeOrderId);
            return;
        }
        if (!order.isDecisionSent()) {
            replySender.schedule(() -> sendResolution(tianggeOrderId, attempt), 1, TimeUnit.SECONDS);
            return;
        }
        Resolution resolution = new Resolution(order.getResolution().name());
        send("resolution:" + tianggeOrderId, "resolution " + resolution.status() + " for " + tianggeOrderId, attempt,
                () -> client.resolve(tianggeOrderId, resolution),
                Set.of("not_backordered", "order_not_found", "invalid_request"),
                () -> update(tianggeOrderId, ChannelOrder::resolutionSent),
                next -> sendResolution(tianggeOrderId, next));
    }

    private void confirmCancellation(String tianggeOrderId, int attempt) {
        ChannelOrder order = orders.findById(tianggeOrderId).orElse(null);
        if (order == null || !order.isCancelReceived() || order.isCancelConfirmed()) {
            queuedReplies.remove("cancellation:" + tianggeOrderId);
            return;
        }
        send("cancellation:" + tianggeOrderId, "cancellation confirmation for " + tianggeOrderId, attempt,
                () -> client.confirmCancellation(tianggeOrderId),
                Set.of("not_cancelled", "order_not_found", "invalid_request"),
                () -> {
                    update(tianggeOrderId, ChannelOrder::cancelConfirmed);
                    stockChangedFor(order.getShopOrderId());
                },
                next -> confirmCancellation(tianggeOrderId, next));
    }

    // Tiangge expects a stock update after every cancellation, even when our stock did not move
    // (for example a backorder that never reserved anything), so push the order's products anyway.
    private void stockChangedFor(UUID shopOrderId) {
        if (shopOrderId == null) {
            return;
        }
        try {
            orderService.find(shopOrderId).ifPresent(order -> order.items().stream()
                    .map(OrderSummary.Line::productId)
                    .forEach(this::stockChanged));
        } catch (RuntimeException e) {
            log.warn("Could not look up order {} to push its stock: {}", shopOrderId, e.toString());
        }
    }

    private void send(String key, String what, int attempt, Runnable call, Set<String> finalErrors,
                      Runnable markSent, Consumer<Integer> retry) {
        try {
            call.run();
            markSent.run();
            queuedReplies.remove(key);
            log.info("Tiangge received {}", what);
        } catch (TianggeException e) {
            if (!e.isRetryable() || finalErrors.contains(e.code())) {
                log.warn("Tiangge refused {}: {}. Not retrying.", what, e.getMessage());
                markSent.run();
                queuedReplies.remove(key);
                return;
            }
            long backoff = Math.min(MAX_BACKOFF_MS, 500L << Math.min(attempt, 5));
            log.warn("Sending {} failed (attempt {}): {}. Retrying in {} ms", what, attempt, e.getMessage(), backoff);
            replySender.schedule(() -> retry.accept(attempt + 1), backoff, TimeUnit.MILLISECONDS);
        } catch (RuntimeException e) {
            queuedReplies.remove(key);
            log.warn("Could not send {}: {}. The resend job will try again.", what, e.toString());
        }
    }

    private void update(String tianggeOrderId, Consumer<ChannelOrder> change) {
        transaction.executeWithoutResult(status -> orders.findById(tianggeOrderId).ifPresent(order -> {
            change.accept(order);
            orders.save(order);
        }));
    }

    void listed(Set<String> skus) {
        listedSkus.clear();
        listedSkus.addAll(skus);
    }

    Set<String> listedSkus() {
        return Set.copyOf(listedSkus);
    }

    void stockChanged(String productId) {
        if (!listedSkus.contains(productId)) {
            return;
        }
        stockSender.execute(() -> publishChangedStock(productId, 1));
    }

    void publishAllStockNow() {
        List<StockEntry> entries = currentStock(listedSkus);
        client.publishStock(entries);
        log.info("Published stock for {} listings: {}", entries.size(), entries);
    }

    private void publishChangedStock(String productId, int attempt) {
        try {
            List<StockEntry> entries = currentStock(List.of(productId));
            client.publishStock(entries);
            log.info("Published stock {}", entries);
        } catch (RuntimeException e) {
            long backoff = Math.min(MAX_BACKOFF_MS, 500L << Math.min(attempt, 5));
            log.warn("Publishing stock for {} failed (attempt {}): {}. Retrying in {} ms",
                    productId, attempt, e.getMessage(), backoff);
            stockSender.schedule(() -> publishChangedStock(productId, attempt + 1), backoff, TimeUnit.MILLISECONDS);
        }
    }

    private List<StockEntry> currentStock(Iterable<String> skus) {
        List<StockEntry> entries = new ArrayList<>();
        for (String sku : skus) {
            Optional<InventoryView> item = inventoryService.getItem(sku);
            item.ifPresent(view -> entries.add(translator.toStock(view)));
        }
        return entries;
    }
}
