package educ.cit.villegas.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "inventory")
public class Inventory {

    @Id
    private UUID productId;
    private String name;
    private BigDecimal price;
    private int stock;

    protected Inventory() {
    }

    public Inventory(UUID productId, String name, BigDecimal price, int stock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}