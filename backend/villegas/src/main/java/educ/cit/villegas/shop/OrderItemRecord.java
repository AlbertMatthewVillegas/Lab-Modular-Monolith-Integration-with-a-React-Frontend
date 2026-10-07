package educ.cit.villegas.shop;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
class OrderItemRecord {

    @EmbeddedId
    private OrderItemRecordId id;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderRecord order;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    protected OrderItemRecord() {
    }

    OrderItemRecord(OrderRecord order, String productId, int quantity) {
        this.order = order;
        this.id = new OrderItemRecordId(null, UUID.fromString(productId));
        this.quantity = quantity;
        this.price = BigDecimal.ZERO;
    }

    String getProductId() {
        return id.productId.toString();
    }

    int getQuantity() {
        return quantity;
    }

    @Embeddable
    static class OrderItemRecordId implements Serializable {
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

        @Column(name = "product_id", nullable = false)
        private UUID productId;

        protected OrderItemRecordId() {
        }

        OrderItemRecordId(UUID orderId, UUID productId) {
            this.orderId = orderId;
            this.productId = productId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof OrderItemRecordId that)) {
                return false;
            }
            return java.util.Objects.equals(orderId, that.orderId)
                    && java.util.Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(orderId, productId);
        }
    }
}
