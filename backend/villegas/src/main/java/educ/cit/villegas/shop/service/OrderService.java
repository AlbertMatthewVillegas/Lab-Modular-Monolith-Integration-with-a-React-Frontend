package educ.cit.villegas.shop.service;

import educ.cit.villegas.entity.Inventory;
import educ.cit.villegas.entity.Order;
import educ.cit.villegas.inventory.service.InventoryService;
import educ.cit.villegas.shop.dto.OrderRequest;
import educ.cit.villegas.shop.dto.OrderResponse;
import educ.cit.villegas.shop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;

    public OrderService(InventoryService inventoryService, OrderRepository orderRepository) {
        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        Inventory currentItem = inventoryService.getItem(request.getProductId());
        String rejectionReason = currentItem == null
                ? "Product not found"
                : request.getQuantity() <= 0
                    ? "Quantity must be greater than zero"
                    : "Insufficient stock";

        Inventory reservedItem = inventoryService.reserve(request.getProductId(), request.getQuantity());
        if (reservedItem == null) {
            Integer stock = currentItem == null ? null : currentItem.getStock();
            orderRepository.save(new Order(request.getProductId(), request.getQuantity(), "REJECTED", rejectionReason));
            return new OrderResponse("REJECTED", rejectionReason, stock);
        }

        orderRepository.save(new Order(request.getProductId(), request.getQuantity(), "CONFIRMED", "Order confirmed"));
        return new OrderResponse("CONFIRMED", "Order confirmed", reservedItem.getStock());
    }
}
