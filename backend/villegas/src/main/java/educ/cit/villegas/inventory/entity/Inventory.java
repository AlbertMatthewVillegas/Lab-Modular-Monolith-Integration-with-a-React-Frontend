package educ.cit.villegas.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;
import educ.cit.villegas.inventory.InventoryView;

@Entity
@Getter
@Setter
@Table(name = "inventory")
public class Inventory {

    @Id
    @Column(name = "product_id")
    private UUID productId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private int stock;

    protected Inventory() {
    }

    public Inventory(UUID productId, String name, BigDecimal price, int stock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public InventoryView toView(int threshold) {
        return new InventoryView(productId.toString(), name, price, stock, stock < threshold);
    }
}