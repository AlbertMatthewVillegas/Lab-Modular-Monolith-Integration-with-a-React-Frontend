package educ.cit.villegas.supplier;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
class LegacySupplyTranslator {

    private static final int MAX_CASES = 99;

    private record CatalogItem(String supplierSku, int packSize) {
    }

    private static final Map<String, CatalogItem> CATALOG = Map.of(
            "550e8400-e29b-41d4-a716-446655440100", new CatalogItem("BZZ-4495", 12),
            "550e8400-e29b-41d4-a716-446655440200", new CatalogItem("BZZ-7561", 24),
            "550e8400-e29b-41d4-a716-446655440300", new CatalogItem("BZZ-1954", 10));

    boolean knows(String productId) {
        return CATALOG.containsKey(productId);
    }

    String supplierSku(String productId) {
        return CATALOG.get(productId).supplierSku();
    }

    int casesFor(String productId, int unitsNeeded) {
        int packSize = CATALOG.get(productId).packSize();
        int cases = (unitsNeeded + packSize - 1) / packSize;
        return Math.max(1, Math.min(cases, MAX_CASES));
    }

    SupplierOrderStatus toStatus(int statusCode) {
        return switch (statusCode) {
            case 10 -> SupplierOrderStatus.PLACED;
            case 20 -> SupplierOrderStatus.PICKING;
            case 30 -> SupplierOrderStatus.SHIPPED;
            case 40 -> SupplierOrderStatus.DELIVERED;
            default -> SupplierOrderStatus.NEEDS_REVIEW;
        };
    }
}
