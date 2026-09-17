package educ.cit.villegas.shop.controller;

import educ.cit.villegas.shop.dto.OrderRequest;
import educ.cit.villegas.shop.dto.OrderResponse;
import educ.cit.villegas.shop.entity.Order;
import java.util.List;
import educ.cit.villegas.shop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public List<Order> getOrderHistory() {
		return orderService.getOrderHistory();
	}

	@PostMapping
	public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
		return ResponseEntity.ok(orderService.placeOrder(request));
	}

	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
		return ResponseEntity.ok(orderService.cancelOrder(orderId));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<String> handleCancelOrderException(ResponseStatusException exception) {
		return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
	}
}
