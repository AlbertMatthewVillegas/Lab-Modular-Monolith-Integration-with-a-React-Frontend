package educ.cit.villegas.shop.dto;

import java.util.List;

import educ.cit.villegas.inventory.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String status;
    private String reason;
    private List<OrderOutcome> items;
    private List<Inventory> inventory;
}