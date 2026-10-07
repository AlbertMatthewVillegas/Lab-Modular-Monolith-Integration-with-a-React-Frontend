package educ.cit.villegas.shop;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
public record OrderSummary(UUID orderId, String status, String reason, OffsetDateTime createdAt, List<Line> items) {
    public record Line(String productId, int quantity) {}
    static OrderSummary of(OrderRecord order) {
        return new OrderSummary(order.getOrderId(), order.getStatus(), order.getReason(), order.getCreatedAt(),
                order.getItems().stream().map(i -> new Line(i.getProductId(), i.getQuantity())).toList());
    }
}
