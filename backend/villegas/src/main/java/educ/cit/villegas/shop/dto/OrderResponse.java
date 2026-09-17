package educ.cit.villegas.shop.dto;

import java.util.List;
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
    private List<OrderItemResponse> items;
    private Integer inventory;
}
