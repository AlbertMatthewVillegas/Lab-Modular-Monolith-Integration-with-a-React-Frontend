package educ.cit.villegas.shop.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItemResponse {
    private UUID orderId;
    private String productId;
    private String outcome;

    public OrderItemResponse(String productId, String outcome) {
        this(null, productId, outcome);
    }
}
