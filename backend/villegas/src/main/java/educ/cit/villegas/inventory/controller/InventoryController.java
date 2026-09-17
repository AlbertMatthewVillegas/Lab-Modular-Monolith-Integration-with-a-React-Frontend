package educ.cit.villegas.inventory.controller;

import educ.cit.villegas.entity.Inventory;
import educ.cit.villegas.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "${FRONTEND_ORIGIN:http://localhost:5173}")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Inventory> getInventory() {
        return inventoryService.getAllItems();
    }
}
