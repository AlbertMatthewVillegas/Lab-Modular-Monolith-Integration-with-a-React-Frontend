package educ.cit.villegas.inventory;

import java.math.BigDecimal;

public record InventoryView(String productId, String name, BigDecimal price, int stock, boolean lowStock) {}
