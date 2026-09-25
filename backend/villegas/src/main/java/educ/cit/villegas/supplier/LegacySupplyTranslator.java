package educ.cit.villegas.supplier;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Everything LegacySupply-shaped stops here: SupplierSku, PackSize, and their numeric
 * StatusCode never leave this class. Inventory/Order only ever see our own IDs and enum.
 *
 */
@Component
class LegacySupplyTranslator {

    // product_id (ours) -> SupplierSku (theirs)
    private static final Map<UUID, String> PRODUCT_TO_SKU = Map.of(
            UUID.fromString("550e8400-e29b-41d4-a716-446655440100"), "BZZ-4495",
            UUID.fromString("550e8400-e29b-41d4-a716-446655440200"), "BZZ-756",
            UUID.fromString("550e8400-e29b-41d4-a716-446655440300"), "BZZ-1954"
    );

    // SupplierSku (theirs) -> PackSize (units per case, from the catalog)
    private static final Map<String, Integer> SKU_TO_PACK_SIZE = Map.of(
            "BZZ-4495", 12,
            "BZZ-756", 24,
            "BZZ-1954", 10
    );

    String skuFor(UUID productId) {
        String sku = PRODUCT_TO_SKU.get(productId);
        if (sku == null) {
            throw new IllegalArgumentException("No SupplierSku mapping for product " + productId);
        }
        return sku;
    }

    /**
     * Converts units we need into whole cases, rounding up per the assignment spec.
     * e.g. 137 units needed, PackSize 12 -> ceil(137/12) = 12 cases (144 units ordered).
     */
    int unitsToCases(String sku, int unitsNeeded) {
        int packSize = SKU_TO_PACK_SIZE.getOrDefault(sku, 1);
        return (int) Math.ceil((double) unitsNeeded / packSize);
    }

    int casesToUnits(String sku, int cases) {
        int packSize = SKU_TO_PACK_SIZE.getOrDefault(sku, 1);
        return cases * packSize;
    }

    /** Maps LegacySupply's numeric StatusCode to our own enum. */
    SupplierOrderStatus mapStatusCode(int statusCode) {
        return switch (statusCode) {
            case 10 -> SupplierOrderStatus.SUBMITTED;
            case 20 -> SupplierOrderStatus.PICKING;
            case 30 -> SupplierOrderStatus.SHIPPED;
            case 40 -> SupplierOrderStatus.DELIVERED;
            default -> SupplierOrderStatus.UNKNOWN; // document this case in INTEGRATION.md
        };
    }
}
