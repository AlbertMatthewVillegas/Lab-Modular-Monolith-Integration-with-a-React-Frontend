package educ.cit.villegas.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import educ.cit.villegas.channel.TianggeJson.FeedEvent;
import educ.cit.villegas.channel.TianggeJson.Line;
import educ.cit.villegas.channel.TianggeJson.Listing;
import educ.cit.villegas.channel.TianggeJson.StockEntry;
import educ.cit.villegas.inventory.InventoryView;
import educ.cit.villegas.shop.OrderRequest;
import org.springframework.stereotype.Component;

@Component
class TianggeTranslator {

    static final String ORDER_PLACED = "ORDER_PLACED";
    static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    static final String ORDER_CANCELED = "ORDER_CANCELED";

    private static final int MAX_REASON = 200;

    Listing toListing(InventoryView item, String supplierItem) {
        return new Listing(item.productId(), item.name(), supplierItem);
    }

    StockEntry toStock(InventoryView item) {
        return new StockEntry(item.productId(), Math.max(0, item.stock()));
    }

    Optional<OrderRequest> toOrderRequest(FeedEvent event, Set<String> listedSkus) {
        if (event.lines() == null || event.lines().isEmpty()) {
            return Optional.empty();
        }
        List<OrderRequest.Item> items = new ArrayList<>();
        for (Line line : event.lines()) {
            if (line == null || line.sellerSku() == null || !listedSkus.contains(line.sellerSku())
                    || line.qty() == null || line.qty() < 1) {
                return Optional.empty();
            }
            items.add(new OrderRequest.Item(line.sellerSku(), line.qty()));
        }
        return Optional.of(new OrderRequest(items));
    }

    ChannelDecision toDecision(String shopOrderStatus) {
        return switch (shopOrderStatus) {
            case "CONFIRMED" -> ChannelDecision.ACCEPTED;
            case "BACKORDERED" -> ChannelDecision.BACKORDERED;
            default -> ChannelDecision.REJECTED;
        };
    }

    String toReason(String reason) {
        if (reason == null) {
            return null;
        }
        return reason.length() <= MAX_REASON ? reason : reason.substring(0, MAX_REASON - 3) + "...";
    }
}
