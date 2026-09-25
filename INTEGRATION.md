
# A mapping table for at least 3 of your Inventory products: your product ID and name, LegacySupply SupplierSku, PackSize. Add products to your Inventory if you need to.

## Product Mapping

| Product ID (yours)                   | Name                | SupplierSku | PackSize |
| ------------------------------------ | ------------------- | ----------- | -------- |
| 550e8400-e29b-41d4-a716-446655440100 | Wireless Mouse      | BZZ-4495    | 12       |
| 550e8400-e29b-41d4-a716-446655440200 | Mechanical Keyboard | BZZ-756         | 24      |
| 550e8400-e29b-41d4-a716-446655440300 | USB-C Hub           | BZZ-1954         | 10      |

# How a LegacySupply session works and how long it actually lasts. Measure it; the manual does not say.

session lasts for approximately around 5 mins. 

# Every error code you received, with what actually caused it.

| Code | HTTP | Message | Reason |
| --- | ---: | --- | --- |
| E-AUTH-01 | 401 | Credentials rejected. | The client ID or API key is invalid. |
| E-AUTH-02 | 401 | Session header missing. | The request did not include `X-LS-Session`. |
| E-AUTH-03 | 401 | Session not recognized. | LegacySupply does not recognize the supplied session token. |
| E-AUTH-07 | 401 | Session not valid. | The session token expired or was revoked. |
| E-FMT-01 | 415 | Unsupported media. | The request did not use the required XML content type. |
| E-FMT-02 | 400 | Malformed document. | The XML body is invalid or does not match the expected structure. |
| E-REF-05 | 400 | BuyerRef invalid. | The buyer reference is missing, too long, or otherwise invalid. |
| E-SKU-02 | 422 | Item not recognized. | The supplier SKU is not in the partner catalog. |
| E-QTY-11 | 422 | Quantity invalid. | The quantity is outside the allowed range or is not a whole positive number. |
| E-IDEM-04 | 409 | Request id reused with different content. | An existing `X-Request-Id` was sent with a different request body. |
| E-PO-04 | 404 | Order not found. | The requested purchase order number does not exist. |
| E-QRY-06 | 400 | Query parameter required. | A required query parameter, such as `buyerRef`, was omitted. |
| E-RATE-03 | 429 | Request quota exceeded. | The partner exceeded LegacySupply's request limit. |
| E-SYS-50 | 503 | Processing error. | outage error |
| E-SYS-99 | 503 | Service unavailable. Try later. | must be a rate limiter |

# What Qty and Uom mean, in your own words, with one worked example.

qty is stock available in legacy supplier, uom is currency used to price

## Unexpected Supplier Statuses

The adapter maps LegacySupply status codes 10, 20, 30, and 40 to our own
`SUBMITTED`, `PICKING`, `SHIPPED`, and `DELIVERED` statuses. Any status code
outside that known set is mapped to our own `UNKNOWN` status and persisted in
`supplier_orders`.

An `UNKNOWN` order is not included in the scheduler's open-order polling list.
The system does not publish a delivery event or restock inventory for it,
because it cannot safely determine whether the supplier delivered the order.
The order remains visible for manual investigation and can be reconciled after
the supplier's new status meaning is understood.
