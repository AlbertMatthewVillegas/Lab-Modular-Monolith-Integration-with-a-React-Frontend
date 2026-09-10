package educ.cit.villegas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter 
@Table(name = "inventory")
public class Inventory {

    @Id
    private String productId;
    private String name;
    private int stock;

    protected Inventory() {
    }

    public Inventory(String productId, String name, int stock) {
        this.productId = productId;
        this.name = name;
        this.stock = stock;
    }
}
