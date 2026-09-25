# Reflection questions

### These are generated from your own traffic and change as your record grows. Copy the three shown at the end of the lab into REFLECTION.md and answer them there.

### LegacySupply holds more than one order for BuyerRef "albertmatthew": PO-100260 (17:09:37) and PO-100261 (17:09:45). Reconstruct the sequence of events that produced the duplicate, and describe the change you made (or would make) so it cannot happen again.

I do a database lookup before every supply request, checking my supplier_orders table that records each request before it hits the supplier. This lets me cross-reference against existing entries to catch duplicates before they're sent. If a matching record already exists, I skip the request instead of resubmitting.

### PO-100264 (BuyerRef "somedude") ended with StatusCode 90, which is not in the documentation. How did you work out what it means, and what does your system now do with the stock that will never arrive?

StatusCode 90 isn't documented, so I checked /verify to see how LegacySupply decoded that order itself. My mapStatusCode falls through any undocumented code to SupplierOrderStatus.UNKNOWN rather than guessing. Stock for that order stays untouched, flagged for manual review.

### LegacySupply never tells you how long a session lasts. Measure your session lifetime from your own logs, state the number, and explain how your adapter decides when to sign in again.

Repeated calls at fixed intervals show my session stops being accepted around 5 minutes after issuance. My adapter tracks issuedAt and re-authenticates proactively once 5 minutes pass. A 401 mid-request also triggers an invalidate-and-retry.
