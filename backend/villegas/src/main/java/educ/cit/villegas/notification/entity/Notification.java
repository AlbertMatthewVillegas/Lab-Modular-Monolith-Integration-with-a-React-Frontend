package educ.cit.villegas.notification.entity;

import jakarta.persistence.Entity;
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
    private UUID notificationId;
    private UUID orderId;
    private UUID productId;
    private String message;
    private OffsetDateTime createdAt;

    protected Notification() {
    }

    public Notification(UUID productId, String message) {
        this.productId = productId;
        this.message = message;
        this.createdAt = OffsetDateTime.now();
    }
}