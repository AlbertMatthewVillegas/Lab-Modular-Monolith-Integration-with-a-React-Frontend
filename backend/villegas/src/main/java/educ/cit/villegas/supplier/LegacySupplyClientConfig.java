package educ.cit.villegas.supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;


import java.time.Duration;

/**
 * Shared HTTP client config: <=3s timeouts per the resilience requirement.
 */
@Configuration
class LegacySupplyClientConfig {

    @Bean
    RestClient.Builder legacySupplyRestClientBuilder() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        return RestClient.builder().requestFactory(requestFactory);
    }
}
