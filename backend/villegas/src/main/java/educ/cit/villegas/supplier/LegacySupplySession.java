package educ.cit.villegas.supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles LegacySupply auth entirely inside the adapter. No token is ever visible
 * outside this class - callers just ask for a valid one.
 */
@Component
class LegacySupplySession {

    // TODO: set this to the real duration you measured in Part B (Contract discovery)
    private static final Duration SESSION_LIFETIME = Duration.ofMinutes(10);

    private static final Pattern TOKEN_PATTERN = Pattern.compile("<SessionToken>(.*?)</SessionToken>");

    private final RestClient restClient;
    private final String clientId;
    private final String apiKey;

    private volatile String currentToken;
    private volatile Instant issuedAt;

    LegacySupplySession(
            RestClient.Builder restClientBuilder,
            @Value("${legacy.supply.api.url}") String baseUrl,
            @Value("${LS_CLIENT_ID:${LEGACY_SUPPLY_CLIENT_ID:}}") String clientId,
            @Value("${LS_API_KEY:${LEGACY_SUPPLY_API_KEY:}}") String apiKey
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.clientId = clientId;
        this.apiKey = apiKey;
    }

    synchronized String getValidToken() {
        if (currentToken == null || isExpired()) {
            authenticate();
        }
        return currentToken;
    }

    /** Call this when a request comes back with a session-related 401 (E-AUTH-03/07). */
    synchronized void invalidate() {
        currentToken = null;
    }

    private boolean isExpired() {
        return issuedAt == null || Duration.between(issuedAt, Instant.now()).compareTo(SESSION_LIFETIME) >= 0;
    }

    private void authenticate() {
        String body = """
                <AuthRequest>
                  <ClientId>%s</ClientId>
                  <ApiKey>%s</ApiKey>
                </AuthRequest>
                """.formatted(clientId, apiKey);

        String response = restClient.post()
                .uri("/auth/token")
                .header("Content-Type", "application/xml")
                .body(body)
                .retrieve()
                .body(String.class);

        Matcher matcher = TOKEN_PATTERN.matcher(response == null ? "" : response);
        if (!matcher.find()) {
            throw new IllegalStateException("LegacySupply auth response had no SessionToken: " + response);
        }
        currentToken = matcher.group(1);
        issuedAt = Instant.now();
    }
}
