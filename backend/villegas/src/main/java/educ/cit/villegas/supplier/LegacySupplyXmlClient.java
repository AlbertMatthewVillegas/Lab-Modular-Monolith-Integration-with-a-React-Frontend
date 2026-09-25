package educ.cit.villegas.supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Talks XML over HTTP to LegacySupply. Nothing in here is visible outside the module -
 * SupplierGatewayImpl is the only caller.
 */
@Component
class LegacySupplyXmlClient {

    private static final Pattern PO_NUMBER = Pattern.compile("<PoNumber>(.*?)</PoNumber>");
    private static final Pattern STATUS_CODE = Pattern.compile("<StatusCode>(.*?)</StatusCode>");
    private static final Pattern ERROR_CODE = Pattern.compile("<Code>(.*?)</Code>");

    private final RestClient restClient;
    private final LegacySupplySession session;

    LegacySupplyXmlClient(
            RestClient.Builder restClientBuilder,
            @Value("${legacy.supply.api.url}") String baseUrl,
            LegacySupplySession session
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.session = session;
    }

    /**
     * Submits a purchase order. requestId is the persisted, stable id used across retries.
     * Throws SessionExpiredException if the session was rejected (caller should invalidate + retry).
     * Throws SupplierUnavailableException for timeouts/5xx/rate limiting (caller should back off + retry).
     */
    PlaceOrderResponse placeOrder(String sku, int qty, String buyerRef, String requestId) {
        String body = """
                <PurchaseOrder>
                  <SupplierSku>%s</SupplierSku>
                  <Qty>%d</Qty>
                  <BuyerRef>%s</BuyerRef>
                </PurchaseOrder>
                """.formatted(sku, qty, buyerRef);

        try {
            String response = restClient.post()
                    .uri("/purchase-orders")
                    .header("Content-Type", "application/xml")
                    .header("X-LS-Session", session.getValidToken())
                    .header("X-Request-Id", requestId)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            String poNumber = extract(PO_NUMBER, response);
            int statusCode = Integer.parseInt(extract(STATUS_CODE, response));
            return new PlaceOrderResponse(poNumber, statusCode);

        } catch (HttpClientErrorException.Unauthorized e) {
            session.invalidate();
            throw new SessionExpiredException(e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new SupplierUnavailableException("Rate limited (E-RATE-03)", e);
        } catch (HttpServerErrorException e) {
            throw new SupplierUnavailableException("LegacySupply server error", e);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // covers connect/read timeouts
            throw new SupplierUnavailableException("Timeout or connection failure", e);
        }
    }

    /** Returns the current numeric StatusCode for a PO, or throws SupplierUnavailableException. */
    int checkStatus(String poNumber) {
        try {
            String response = restClient.get()
                    .uri("/purchase-orders/{po}", poNumber)
                    .header("X-LS-Session", session.getValidToken())
                    .retrieve()
                    .body(String.class);
            return Integer.parseInt(extract(STATUS_CODE, response));
        } catch (HttpClientErrorException.Unauthorized e) {
            session.invalidate();
            throw new SessionExpiredException(e);
        } catch (HttpServerErrorException | org.springframework.web.client.ResourceAccessException e) {
            throw new SupplierUnavailableException("Failed to check status for " + poNumber, e);
        }
    }

    /** GET /purchase-orders?buyerRef=... - used to check for an already-placed order before retrying. */
    boolean existsForBuyerRef(String buyerRef) {
        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/purchase-orders").queryParam("buyerRef", buyerRef).build())
                    .header("X-LS-Session", session.getValidToken())
                    .retrieve()
                    .body(String.class);
            return response != null && response.contains("<PoNumber>");
        } catch (Exception e) {
            // best-effort pre-check; if it fails, fall through to normal submit+retry logic
            return false;
        }
    }

    private String extract(Pattern pattern, String xml) {
        if (xml == null) return null;
        Matcher m = pattern.matcher(xml);
        return m.find() ? m.group(1) : null;
    }

    record PlaceOrderResponse(String poNumber, int statusCode) {
    }

    static class SessionExpiredException extends RuntimeException {
        SessionExpiredException(Throwable cause) {
            super(cause);
        }
    }

    static class SupplierUnavailableException extends RuntimeException {
        SupplierUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
