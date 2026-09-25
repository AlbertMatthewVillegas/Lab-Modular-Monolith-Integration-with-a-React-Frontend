package educ.cit.villegas.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import educ.cit.villegas.event.LowStock;
import educ.cit.villegas.event.OrderPlaced;
import educ.cit.villegas.event.OrderRejected;
import educ.cit.villegas.inventory.entity.Inventory;
import educ.cit.villegas.inventory.service.InventoryService;
import educ.cit.villegas.shop.dto.OrderItemDto;
import educ.cit.villegas.shop.dto.OrderOutcome;
import educ.cit.villegas.shop.dto.OrderRequest;
import educ.cit.villegas.shop.dto.OrderResponse;
import educ.cit.villegas.shop.entity.Order;
import educ.cit.villegas.shop.entity.OrderItem;
import educ.cit.villegas.shop.repository.OrderRepository;

@Service
public class OrderService {

    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
        InventoryService inventoryService, 
        OrderRepository orderRepository,
        ApplicationEventPublisher eventPublisher
    ) {
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
        
        Order order = new Order();

        List<OrderItemDto> items = request.getItems();
        List<OrderOutcome> outcomes = new ArrayList<>();
        String rejectionReason = null;
        
        for(OrderItemDto item : items) {
            Inventory currentItem = inventoryService.getItem(item.getProductId());
            rejectionReason = getRejectionReason(item, currentItem);
            outcomes.add(new OrderOutcome(item.getProductId(), rejectionReason != null ? rejectionReason : "Accepted"));

        }

        if (rejectionReason == null) { // order is not rejected
            for(OrderItemDto item : items) {
                int itemStock = inventoryService.getItem(item.getProductId()).getStock();
                int orderQuantity = item.getQuantity();
                int remainingStock = itemStock - orderQuantity;
                if (remainingStock <= 5) {
                    eventPublisher.publishEvent(new LowStock(item.getProductId(), remainingStock, orderQuantity));
                }
                inventoryService.reserve(item.getProductId(), item.getQuantity());
                order.addItem(new OrderItem(
                    item.getProductId(),
                    inventoryService.getItem(item.getProductId()).getPrice(),
                    item.getQuantity()));
            }
        } 

        order.setStatus(rejectionReason == null ? "CONFIRMED" : "REJECTED");
        order.setReason(rejectionReason);
        orderRepository.save(order);

        if (rejectionReason != null) {
            eventPublisher.publishEvent(new OrderRejected(order));
        } else {
            eventPublisher.publishEvent(new OrderPlaced(order));
        }

        OrderResponse response = new OrderResponse();
        response.setItems(outcomes);
        response.setStatus(rejectionReason == null ? "CONFIRMED" : "REJECTED");
        response.setReason(rejectionReason);
        response.setInventory(inventoryService.getAllItems());
        return response;
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if(order.getStatus().equals("CANCELLED")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is already cancelled");
        }   
        order.setStatus("CANCELLED");
        order.setReason("User requested cancellation");
        orderRepository.save(order);

        for(OrderItem item : order.getItems()) {
            inventoryService.restock(item.getProductId(), item.getQuantity());
        }
        return new OrderResponse(order.getStatus(), order.getReason(), null, inventoryService.getAllItems());
    }

    private String getRejectionReason(OrderItemDto item, Inventory currentItem) {
        String rejectionReason = null;
        if (currentItem == null) {
            rejectionReason = "Product not found: " + item.getProductId();
        } else if (item.getQuantity() <= 0) {
            rejectionReason = "Invalid quantity for product: " + item.getProductId();
        } else if (currentItem.getStock() < item.getQuantity()) {
            rejectionReason = "Insufficient stock for product: " + item.getProductId();
        }
        return rejectionReason;
    }
}
