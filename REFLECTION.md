# Reflection questions
# LAB 3

### These are generated from your own traffic and change as your record grows. Copy the three shown at the end of the lab into REFLECTION.md and answer them there.

### LegacySupply holds more than one order for BuyerRef "albertmatthew": PO-100260 (17:09:37) and PO-100261 (17:09:45). Reconstruct the sequence of events that produced the duplicate, and describe the change you made (or would make) so it cannot happen again.

I do a database lookup before every supply request, checking my supplier_orders table that records each request before it hits the supplier. This lets me cross-reference against existing entries to catch duplicates before they're sent. If a matching record already exists, I skip the request instead of resubmitting.

### PO-100264 (BuyerRef "somedude") ended with StatusCode 90, which is not in the documentation. How did you work out what it means, and what does your system now do with the stock that will never arrive?

StatusCode 90 isn't documented, so I checked /verify to see how LegacySupply decoded that order itself. My mapStatusCode falls through any undocumented code to SupplierOrderStatus.UNKNOWN rather than guessing. Stock for that order stays untouched, flagged for manual review.

### LegacySupply never tells you how long a session lasts. Measure your session lifetime from your own logs, state the number, and explain how your adapter decides when to sign in again.

Repeated calls at fixed intervals show my session stops being accepted around 5 minutes after issuance. My adapter tracks issuedAt and re-authenticates proactively once 5 minutes pass. A 401 mid-request also triggers an invalidate-and-retry.

# LAB 4

### 1. Tiangge order TG-MEY73F (3 x 550e8400-e29b-41d4-a716-446655440100) was accepted at 05:25:16. At that moment your last published stock for 550e8400-e29b-41d4-a716-446655440100 was 0, and the stock Tiangge worked out from your own decisions, cancellations and deliveries was 0. Where did your application's stock figure come from, and why did it disagree?

I decided from my own inventory table. The direct REST path (shop.service.OrderService) reads that row and calls inventoryService.reserve(...). Unlike the channel path, it never published a StockChangedEvent. So the database changed but Tiangge's last published stock stayed at 0. Tiangge's own derived stock was also 0 because it only sees updates sent through the channel outbox.

### 2. Event evt_3941f1fd02fd48b8 (order TG-32Q2ZP) reached your application twice, as seq 80 and seq 87, and you processed it once. Show the code and the stored data that made the second delivery harmless, and explain what would happen if your application restarted between the two.

FeedEventHandler.handle(...) checks processedEvents.existsById(event.eventId()) first. The first time, it saves a row in channel_events with event_id = evt_3941f1fd02fd48b8, seq = 80 and order_id = TG-32Q2ZP. The event ID is the primary key, so seq 87 finds that row and is skipped. The cursor in feed_cursors is updated in the same transaction. After a restart, the poller resumes from the saved cursor, and a crash before commit rolls everything back so the event is simply retried.

### 3. Order TG-DLWKP6 was backordered at 05:23:10 and accepted at 05:25:06, after PO-104224 was delivered at 05:24:54. Trace how the delivery reached your Inventory and what then resumed the backordered order.

DeliveryTracker polled the supplier, and LegacySupplyClient.getOrder(...) returned status code 40. LegacySupplyTranslator.toStatus(40) mapped it to DELIVERED, and DeliveryTracker published SupplierOrderDelivered. SupplierDeliveryListener called InventoryService.restock(...), which published a positive StockChangedEvent. BackorderService picked it up, locked TG-DLWKP6 with findForUpdate(...), set it to CONFIRMED and reserved the units. ChannelEventListener then handled BackorderFilledEvent, and TianggeOutbox sent the ACCEPTED resolution to Tiangge.