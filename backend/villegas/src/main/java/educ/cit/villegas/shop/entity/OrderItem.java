package educ.cit.villegas.shop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "order_items")
public class OrderItem {

    @EmbeddedId
    private OrderItemId id = new OrderItemId();

    @MapsId("orderId")
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    private BigDecimal price;
    private int quantity;

    protected OrderItem() {
    }

    public OrderItem(UUID productId, BigDecimal price, int quantity) {
        this.id.setProductId(productId);
        this.price = price;
        this.quantity = quantity;
    }

    @Transient
    public UUID getProductId() {
        return id.getProductId();
    }

    public void setProductId(UUID productId) {
        id.setProductId(productId);
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null && order.getOrderId() != null) {
            id.setOrderId(order.getOrderId());
        }
    }
}