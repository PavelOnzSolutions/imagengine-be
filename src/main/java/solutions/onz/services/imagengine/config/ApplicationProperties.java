package solutions.onz.services.imagengine.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Data
@ConfigurationProperties(prefix = "imagengine", ignoreUnknownFields = false)
public class ApplicationProperties {
    private Security security = new Security();
    private Storage storage = new Storage();

    @Getter
    @Setter
    public static class Security {
        public String csp;
        public String jwtBase64Secret;
    }

    @Getter
    @Setter
    public static class Storage {
        public String path;
    }

    @Bean
    public WebClient webClient() {
        final int size = (int) DataSize.ofMegabytes(16).toBytes();
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> {
                    codecs.defaultCodecs().maxInMemorySize(size);
                    codecs.defaultCodecs().enableLoggingRequestDetails(true);
                })
                .build();
        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}
