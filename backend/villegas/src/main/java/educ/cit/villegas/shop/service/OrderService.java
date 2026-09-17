package educ.cit.villegas.shop.service;

import educ.cit.villegas.entity.Inventory;
import educ.cit.villegas.entity.Order;
import educ.cit.villegas.event.OrderPlaced;
import educ.cit.villegas.event.OrderRejected;
import educ.cit.villegas.inventory.service.InventoryService;
import educ.cit.villegas.shop.dto.OrderItemRequest;
import educ.cit.villegas.shop.dto.OrderItemResponse;
import educ.cit.villegas.shop.dto.OrderRequest;
import educ.cit.villegas.shop.dto.OrderResponse;
import educ.cit.villegas.shop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(InventoryService inventoryService, OrderRepository orderRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrderHistory() {
        return orderRepository.findAll();
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        List<OrderItemRequest> items = request.getItems();
        Map<String, Integer> requestedQuantities = new HashMap<>();

        for (OrderItemRequest item : items) {
            Inventory currentItem = inventoryService.getItem(item.getProductId());
            String rejectionReason = currentItem == null
                    ? "Product not found"
                    : item.getQuantity() <= 0
                        ? "Quantity must be greater than zero"
                        : "Insufficient stock";
            int totalRequested = requestedQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);

            if (currentItem == null || item.getQuantity() <= 0 || totalRequested > currentItem.getStock()) {
                Integer stock = currentItem == null ? null : currentItem.getStock();
                Order order = orderRepository.save(new Order(item.getProductId(), item.getQuantity(), "REJECTED", rejectionReason));
                eventPublisher.publishEvent(new OrderRejected(order));
                return rejectedResponse(items, rejectionReason, stock);
            }
        }

        Integer remainingInventory = null;
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItemRequest item : items) {
            Inventory reservedItem = inventoryService.reserve(item.getProductId(), item.getQuantity());
            remainingInventory = reservedItem.getStock();
            Order order = orderRepository.save(new Order(item.getProductId(), item.getQuantity(), "CONFIRMED", "Order confirmed"));
            eventPublisher.publishEvent(new OrderPlaced(order));
            itemResponses.add(new OrderItemResponse(order.getOrderId(), item.getProductId(), "CONFIRMED"));
        }
        return new OrderResponse("CONFIRMED", "Order confirmed", itemResponses, remainingInventory);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is already cancelled");
        }

        Inventory inventory = inventoryService.getItem(order.getProductId());
        if ("CONFIRMED".equals(order.getStatus())) {
            inventory = inventoryService.restock(order.getProductId(), order.getQuantity());
            order.setStatus("CANCELLED");
            order.setReason("Order cancelled");
            orderRepository.save(order);
        }

        Integer stock = inventory == null ? null : inventory.getStock();
        return new OrderResponse(order.getStatus(), order.getReason(),
            List.of(new OrderItemResponse(order.getOrderId(), order.getProductId(), order.getStatus())), stock);
    }

    private OrderResponse rejectedResponse(List<OrderItemRequest> items, String reason, Integer inventory) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> new OrderItemResponse(item.getProductId(), "REJECTED"))
                .toList();
        return new OrderResponse("REJECTED", reason, itemResponses, inventory);
    }
}
