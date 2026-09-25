package educ.cit.villegas.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "notification_id")
    private UUID notificationId;
    @Column(name = "order_id")
    private UUID orderId;
    @Column(name = "product_id")
    private UUID productId;
    @Column(nullable = false)
    private String message;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Notification() {
    }

    public Notification(UUID orderId, String message) {
        this.orderId = orderId;
        this.message = message;
        this.createdAt = OffsetDateTime.now();
    }

    public Notification(UUID orderId, UUID productId, String message) {
        this.orderId = orderId;
        this.productId = productId;
        this.message = message;
        this.createdAt = OffsetDateTime.now();
    }
}