package educ.cit.villegas.inventory;

public record ReservationResult(boolean confirmed, String reason, InventoryView inventory) {
    public static ReservationResult confirmed(InventoryView item) { return new ReservationResult(true, null, item); }
    public static ReservationResult rejected(String reason, InventoryView item) { return new ReservationResult(false, reason, item); }
}
